package realjame.discordlink;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
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

import static realjame.discordlink.Config.getDefaultConfig;

public class DiscordLink implements ModInitializer {
	public static final String MOD_ID = "serverstatus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private final long startTime = Instant.now().getEpochSecond();

	@Override
	public void onInitialize() {
		logInfo("DiscordLink started.");

		Config config = loadConfig();
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
			jda = JDABuilder.createDefault(config.token).setActivity(Activity.playing(config.playingStatus != null ? config.playingStatus : "A Minecraft: Better Than Adventure server!")).setCallbackPool(Executors.newCachedThreadPool(), true).build();
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
				channel.delete().queue();
			} catch (PermissionException e) {
				logError("Cannot delete channel: " + e.getMessage());
				return;
			}
		}

		// Make channels
		VoiceChannel uptime = (VoiceChannel) category.createVoiceChannel("🟢 Online for ?").complete();
		VoiceChannel playerCount = (VoiceChannel) category.createVoiceChannel("👥 Players online: ?").complete();
		VoiceChannel worldSize = (VoiceChannel) category.createVoiceChannel("💾 World size: ?").complete();

		// Start listening for stats
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> updateUptime(uptime), 0, 5, TimeUnit.MINUTES);

		// This one is delayed to not cause issues with the Minecraft server - without it ConfigManager would return null in updatePlayerCount.
		// TODO: I'm sure there's a better way to wait for the MC server to start up.
		scheduler.scheduleAtFixedRate(() -> updatePlayerCount(playerCount), 10, 5 * 60, TimeUnit.SECONDS);

		scheduler.scheduleAtFixedRate(() -> updateWorldSize(worldSize, worldDir), 0, 5, TimeUnit.MINUTES);

		logInfo("Discord bot is now active and listening!");
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

	private static void logError(String errorMessage, boolean... noCrashWarning) {
		LOGGER.error(errorMessage);
		System.out.println(errorMessage + (noCrashWarning.length == 0 ? "\nThe mod will crash now :'(" : ""));
	}

	private static void logInfo(String infoMessage) {
		LOGGER.info(infoMessage);
		System.out.println(infoMessage);
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
}
