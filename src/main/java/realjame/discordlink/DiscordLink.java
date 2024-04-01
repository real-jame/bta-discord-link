package realjame.discordlink;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import realjame.discordlink.chatbridge.DiscordRelayer;

import static realjame.discordlink.Config.getDefaultConfig;
import static realjame.discordlink.Log.logError;
import static realjame.discordlink.Log.logInfo;

public class DiscordLink implements ModInitializer {
	private final long startTime = Instant.now().getEpochSecond();
	public static TextChannel bridgeChannel;
	private static Config config;
	private static JDAWebhookClient webhookClient;
	// TODO: split discord bot client into a class?

	@Override
	public void onInitialize() {
		logInfo("DiscordLink started.");

		config = loadConfig();
		if (config == null) {
			logError("A valid discordlink.toml file was not found in the server's config directory.");
			return;
		}
		logInfo("Got config file.");

		// TODO: Get world name from server.properties instead of a manually set config option
		if (config.worldName == null) {
			logError("Please specify the world name (at worldName) in discordlink.toml.");
			return;
		}
		Path worldDir = Paths.get(FabricLoader.getInstance().getGameDir().toString() + "/" + config.worldName);
		logInfo("Got world dir.");

		JDA jda;
		try {
			jda = JDABuilder.createDefault(config.token).enableIntents(GatewayIntent.MESSAGE_CONTENT).setActivity(Activity.playing(config.playingStatus != null ? config.playingStatus : "A Minecraft: Better Than Adventure server!")).setCallbackPool(Executors.newCachedThreadPool(), true).build();
		} catch (Exception e) {
			logError("Failed to initialize Discord bot: " + e.getMessage());
			return;
		}
		logInfo("Got Discord bot: " + jda.getStatus());
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			logError("Discord bot failed to load: " + e.getMessage());
			return;
		}
		Guild guild = jda.getGuildById(config.guildId);
		if (guild == null) {
			logError("Please specify a valid server ID (at guildId) in discordlink.toml.");
			return;
		}
		logInfo("Got guild " + guild.getName());
		bridgeChannel = guild.getTextChannelById(config.channelId);
		if (bridgeChannel == null) {
			logError("Please specify a valid channel ID (at channelId) in discordlink.toml.");
			return;
		}
		logInfo("Got channel " + bridgeChannel.getName());
		Category category = guild.getCategoryById(config.categoryId);
		if (category == null) {
			logError("Please specify a valid category ID (at categoryId) in discordlink.toml.");
			return;
		}
		logInfo("Got category " + category.getName());

		// Clear existing channels in category
		List<GuildChannel> channels = category.getChannels();
		for (GuildChannel channel : channels) {
			try {
				if (channel != bridgeChannel) {
					channel.delete().queue();
				}
			} catch (PermissionException e) {
				logError("Cannot delete channel: " + e.getMessage());
				return;
			}
		}

		// Make channels
		VoiceChannel uptime = (VoiceChannel) category.createVoiceChannel("🟢 Online for ?").complete();
		VoiceChannel playerCount = (VoiceChannel) category.createVoiceChannel("👥 Players online: ?").complete();
		VoiceChannel worldSize = (VoiceChannel) category.createVoiceChannel("💾 World size: ?").complete();

		// TODO: wait for MinecraftServer configManager to exist before continuing. the mod will have to be initialized on a separate thread.

		// Start listening for stats
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> updateUptime(uptime), 0, 5, TimeUnit.MINUTES);

		// This one is delayed to not cause issues with the Minecraft server - without it ConfigManager would return null in updatePlayerCount.
		// TODO: I'm sure there's a better way to wait for the MC server to start up.
		scheduler.scheduleAtFixedRate(() -> updatePlayerCount(playerCount), 10, 5 * 60, TimeUnit.SECONDS);

		scheduler.scheduleAtFixedRate(() -> updateWorldSize(worldSize, worldDir), 0, 5, TimeUnit.MINUTES);

		logInfo("Discord bot is now active and listening!");

		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Server is online!", null);
		eb.setColor(Color.green);
		eb.setDescription("hi");
//		bridgeChannel.sendMessageEmbeds(eb.build()).complete();

		DiscordRelayer DiscordRelayer = new DiscordRelayer();
		jda.addEventListener(DiscordRelayer);

		// Thanks <3 https://github.com/Melon-Modding/bta-discord-integration/blob/2e2172b5b4fe164bb8d148fd7eff68e5a0f54a6f/src/main/java/de/olivermakesco/bta_discord_integration/server/DiscordClient.java#L70
		if (config.discordChatDisplay == Config.DiscordChatDisplayType.WEBHOOK) {
			Optional<Webhook> optionalWebhook = bridgeChannel.retrieveWebhooks().complete().stream().filter((it) -> {
				User owner = it.getOwnerAsUser();
				if (owner == null) {
					return false;
				}
				return owner.getId().equals(jda.getSelfUser().getId());
			}).findFirst();

			webhookClient = JDAWebhookClient.from(optionalWebhook.orElseGet(() -> bridgeChannel.createWebhook("BTA D Link Chat Bridge").complete()));
		}
		System.out.println("Got webhook!");
	}

	private Config loadConfig() {
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve("discordlink.toml").normalize();

		// Create config file if it doesn't already exist
		if (!Files.exists(configFile)) {
			try {
				String defaultConfig = getDefaultConfig();
				Files.write(configFile, defaultConfig.getBytes());

				logInfo("Config file template created at " + configFile.toAbsolutePath() + ", go fill it out according to the README instructions or this mod will not work!");
			} catch (IOException e) {
				logError("Error creating config file template: " + e, true);
				return null;
			}
		}

		try (InputStream inputStream = Files.newInputStream(configFile)) {
			TomlMapper mapper = new TomlMapper();
			return mapper.readValue(inputStream, Config.class);
		} catch (Exception e) {
			logError("Error loading config file: " + e, true);
			return null;
		}
	}

	private void updateUptime(VoiceChannel channel) {
		try {
			long currentTime = Instant.now().getEpochSecond();
			long uptimeSeconds = currentTime - startTime;

			long days = uptimeSeconds / 86400;
			long hours = uptimeSeconds / 3600;
			long minutes = (uptimeSeconds % 3600) / 60;
//			long seconds = uptimeSeconds % 60;
			String newStatus = hours + "h " + minutes + "m";//"m " + seconds + "s";
			if (hours >= 100) {
				newStatus = days + "d " + newStatus;
			}
			channel.getManager().setName("🟢 Online for " + newStatus).complete();
		} catch (Exception e) {
			logError("Error updating uptime: " + e, true);
		}
	}

	private void updatePlayerCount(VoiceChannel channel) {
		try {
			List<EntityPlayerMP> playerList = MinecraftServer.getInstance().configManager.playerEntities;
			String newStatus = String.valueOf(playerList.size());
			channel.getManager().setName("👥 Players online: " + newStatus).complete();
		} catch (Exception e) {
			logError("Error updating player count (if the server just started, please wait for the mod to check again): " + e, true);
		}
	}

	private void updateWorldSize(VoiceChannel channel, Path worldDir) {
		try {
			long size = Files.walk(worldDir).filter(Files::isRegularFile).mapToLong(p -> {
				try {
					return Files.size(p);
				} catch (IOException e) {
					return 0L;
				}
			}).sum();
			// Convert to GB
			DecimalFormat df = new DecimalFormat("#.##");
			String newStatus = df.format((double) size / (1024 * 1024 * 1024)) + " GB";
			channel.getManager().setName("💾 World size: " + newStatus).complete();
		} catch (Exception e) {
			logError("Error updating world size: " + e, true);
		}
	}

	//	TODO: handle nicknames
	public static void sendJoinMessage(EntityPlayerMP player) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(player.username + " has joined the server", null);
//		eb.setColor(Color.green);
		eb.setDescription(player.username);
		bridgeChannel.sendMessageEmbeds(eb.build()).complete();
	}

	public static void relayBTAChatMessage(EntityPlayerMP player, String chatMessage, byte chatColor) {
//		EmbedBuilder eb = new EmbedBuilder();
//		eb.setTitle("Chat message from " + player.username, null);
//		eb.setColor(Color.white);
//		eb.setDescription(chatMessage);
//		bridgeChannel.sendMessageEmbeds(eb.build()).complete();
		String name = player.nickname;
		boolean isNickname = true;
		if (name.isEmpty()) {
			name = player.username;
			isNickname = false;
		}
		logInfo("Relaying BTA message from " + name + ": " + chatMessage);
		if (webhookClient != null && config.discordChatDisplay == Config.DiscordChatDisplayType.WEBHOOK) {
			if (isNickname) {
				name = name + "*";
			}
			String avatarUrl = "https://mc-heads.net/avatar/" + player.username;
			WebhookMessage message = new WebhookMessageBuilder().setUsername(name).setAvatarUrl(avatarUrl).setContent(chatMessage).build();
			webhookClient.send(message);
		} else {
			String message;
			if (isNickname) {
				name = "*" + name + "*";
			}
			message = "<" + name + "> " + chatMessage;
			bridgeChannel.sendMessage(message).complete();
		}
	}
}
