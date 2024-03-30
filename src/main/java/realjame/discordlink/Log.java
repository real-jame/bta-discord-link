package realjame.discordlink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {

	public static final String MOD_ID = "discordlink";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static void logError(String errorMessage, boolean... noCrashWarning) {
		LOGGER.error(errorMessage);
		System.out.println(errorMessage + (noCrashWarning.length == 0 ? "\nThe mod will crash now :'(" : ""));
	}

	public static void logInfo(String infoMessage) {
		LOGGER.info(infoMessage);
		System.out.println(infoMessage);
	}
}
