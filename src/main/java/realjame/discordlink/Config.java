package realjame.discordlink;

// How chats should be displayed on Discord.
// Webhook: Creates a fake user out of the BTA username using webhooks, displaying the player's head as the PFP.
// Compact: Bot sends the messages itself, making it appear like a BTA chat message using angle brackets to display the username.

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

	public boolean disableChatBridge;

	public ServerStat[] statsDisplayed;
	public String token;
	public long guildId;
	public long categoryId;
	public long channelId;
	public String worldName;
	public String playingStatus;
	public DiscordChatDisplayType discordChatDisplay;

	// TODO: move this to a file in the project if it keeps getting longer?
	// TODO: description comment for statsDisplayed options
	public static String getDefaultConfig() {
		return "#Feature toggles\n" +
			"disableChatBridge = false\n" +
			"statsDisplayed = [\"UPTIME\", \"PLAYERS_ONLINE\", \"WORLD_SIZE\"]\n\n" +
			"# Bot setup\n" +
			"token = \"your_discord_bot_token_here\"\n" +
			"guildId = \"123456789012345678\"\n" +
			"categoryId = \"123456789012345678\"\n" +
			"channelId = \"123456789012345678\"\n" +
			"worldName = \"world\"\n\n" +
			"# Display options\n" +
			"playingStatus = \"testing\"\n" +
			"# Customize how BTA chats are displayed on Discord: webhook or compact.\n" +
			"# WEBHOOK: Creates a fake user out of the BTA username using webhooks, displaying the player's head as the PFP.\n" +
			"# COMPACT: Bot sends the messages itself, making it appear like a BTA chat message using angle brackets to display the username.\n" +
			"discordChatDisplay = \"WEBHOOK\"";
	}
}
