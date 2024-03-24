package realjame.serverstatus;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import static realjame.serverstatus.Config.getDefaultYaml;

public class ServerStatus implements ModInitializer {
	public static final String MOD_ID = "serverstatus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private ScheduledExecutorService scheduler;
	private long startTime = Instant.now().getEpochSecond();

	@Override
	public void onInitialize() {
		LOGGER.info("ServerStatus started.");

		Config config = loadConfig();
		if (config == null) {
			logError("serverstatus.yaml file not found in the server's config directory.");
		}

//		System.out.println("The Discord token is " + config.token);
//		System.out.println("The guild ID is " + config.guildId);
//		System.out.println("The category ID is " + config.categoryId);

		if (config.worldName == null) {
			logError("Please specify the world name (at worldName) in serverstatus.yaml.");
			return;
		}
		Path worldDir = Paths.get(FabricLoader.getInstance().getGameDir().toString() + "/" + config.worldName);

		JDA jda = JDABuilder.createDefault(config.token).setActivity(Activity.playing("JameSMP")).build();
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		Guild guild = jda.getGuildById(config.guildId);
		if (guild == null) {
			logError("Please specify a server ID (at guildId) in serverstatus.yaml.");
			return;
		}
//		System.out.println("The guild is " + guild.getName());
		Category category = guild.getCategoryById(config.categoryId);
		if (category == null) {
			logError("Please specify a category ID (at categoryId) in serverstatus.yaml.");
			return;
		}
//		System.out.println("The category is " + category.getName());

		// Clear existing channels in category
		List<GuildChannel> channels = category.getChannels();
		for (GuildChannel channel : channels) {
//			System.out.println("The channel is " + channel.getName());
			try {
				channel.delete().queue();
			} catch (PermissionException e) {
				logError("Cannot delete channel: " + e.getMessage());
				return;
			}
		}

		// Make channels
		VoiceChannel uptime = (VoiceChannel) category.createVoiceChannel("🟢 Online for 0h 1m").complete();
		VoiceChannel playerCount = (VoiceChannel) category.createVoiceChannel("👥 Players online: 1").complete();
		VoiceChannel worldSize = (VoiceChannel) category.createVoiceChannel("💾 World size: 1 gazillion TB").complete();

		// Start listening for stats
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> updateUptime(uptime), 0, 5, TimeUnit.MINUTES);

		// TODO: This one is delayed to not cause issues with the Minecraft server - without it ConfigManager would return null in updatePlayerCount.
		// I'm sure there's a better way to wait for the MC server to start up.
		scheduler.scheduleAtFixedRate(() -> updatePlayerCount(playerCount), 5, 5 * 60, TimeUnit.SECONDS);

		scheduler.scheduleAtFixedRate(() -> updateWorldSize(worldSize, worldDir), 0, 5, TimeUnit.MINUTES);
	}

	private Config loadConfig() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path configFile = configDir.resolve("serverstatus.yaml").normalize();

		// Create config file if it doesn't already exist
		if (!Files.exists(configFile)) {
			try {
				String defaultConfig = getDefaultYaml();
				Files.write(configFile, defaultConfig.getBytes());

				String createdLog = "Config file template created at " + configFile.toAbsolutePath() + ", go fill it out according to the README instructions or this mod will not work! Now get ready for a random crash lol 👍";
				LOGGER.info(createdLog);
				System.out.println(createdLog);
			} catch (IOException e) {
				logError("Error creating config file template: " + e);
			}
		}

		Yaml yaml = new Yaml();
		try (InputStream inputStream = Files.newInputStream(configFile)) {
			return yaml.loadAs(inputStream, Config.class);
		} catch (Exception e) {
			logError("Error loading config file: " + e);
			return null;
		}
	}

	private static void logError(String errorMessage, boolean... noFunnyMessage) {
		LOGGER.error(errorMessage);
		System.out.println(errorMessage + "\nThe mod will crash now😭");//+ (noFunnyMessage == null ? "\nThe mod will crash now😭" : ""));
	}

	private void updateUptime(VoiceChannel channel) {
		try {
			long currentTime = Instant.now().getEpochSecond();
			long uptimeSeconds = currentTime - startTime;
			long hours = uptimeSeconds / 3600;
			long minutes = (uptimeSeconds % 3600) / 60;
//			long seconds = uptimeSeconds % 60;
			String newStatus = hours + "h " + minutes + "m";//"m " + seconds + "s";
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
			logError("Error updating player count: " + e, true);
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
