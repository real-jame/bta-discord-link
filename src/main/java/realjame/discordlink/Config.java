package realjame.discordlink;

// How chats should be displayed on Discord.
// Webhook: Creates a fake user out of the BTA username using webhooks, displaying the player's head as the PFP.
// Compact: Bot sends the messages itself, making it appear like a BTA chat message using angle brackets to display the username.

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public final class Config {
	public enum ServerStat {
		UPTIME,
		PLAYERS_ONLINE,
		WORLD_SIZE,

	}
	public enum DiscordChatDisplayType {
		WEBHOOK,
		COMPACT
	}
	public enum WebhookPFPStyle {
		HEAD_2D,
		HEAD_3D,
		BODY_2D,
		BODY_3D,
		COMBO
	}

	public boolean disableChatBridge;

	public ServerStat[] statsDisplayed;
	public String token;
	public long guildId;
	public long categoryId;
	public long channelId;
	public String worldName;
	public String playingStatus;
	public DiscordChatDisplayType discordChatDisplay;
	public WebhookPFPStyle webhookPFPStyle;

	public static String getDefaultConfig() throws IOException {
		try (InputStream inputStream =  Config.class.getResourceAsStream("/defaultconfig.toml");
			 Scanner scanner = new Scanner(inputStream)) {
			StringBuilder content = new StringBuilder();
			while (scanner.hasNextLine()) {
				content.append(scanner.nextLine()).append("\n");
			}
			return content.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null; // Handle file reading error
		}
	}
}
