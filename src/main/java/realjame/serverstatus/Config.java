package realjame.serverstatus;

public final class Config {
	public String token;
	public long guildId;
	public long categoryId;
	public String worldName;

	public static String getDefaultYaml() {
		return "token: your_discord_bot_token_here\n"
			+ "guildId: 123456789012345678\n"
			+ "categoryId: 123456789012345678\n"
			+ "worldName: world";
	}
}
