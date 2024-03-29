package realjame.discordlink;

public final class Config {
	public String token;
	public long guildId;
	public long categoryId;
	public String worldName;
	public String playingStatus;

	// TODO: move this to a file in the project if it keeps getting longer?
	public static String getDefaultConfig() {
		return "# Bot setup\n"
			+ "token = \"your_discord_bot_token_here\"\n"
			+ "guildId = \"123456789012345678\"\n"
			+ "categoryId = \"123456789012345678\"\n"
			+ "worldName = \"world\"\n\n"
			+ "# Display options\n"
			+ "playingStatus = \"testing\"\n";
	}
}
