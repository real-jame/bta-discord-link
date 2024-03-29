package realjame.discordlink;

public final class Config {
	public String token;
	public long guildId;
	public long categoryId;
	public String worldName;
	public String playingStatus;

	public static String getDefaultYaml() {
		return "token: your_discord_bot_token_here\n"
			+ "guildId: 123456789012345678\n"
			+ "categoryId: 123456789012345678\n"
			+ "worldName: world\n"
			+ "playingStatus: 'A Minecraft: Better Than Adventure server!'";
	}
}
