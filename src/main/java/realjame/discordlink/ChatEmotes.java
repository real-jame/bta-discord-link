package realjame.discordlink;

import java.util.HashMap;
import java.util.Map;

// Maps BTA's chat emotes (which mostly use non-emoji Unicode characters) to emoji
public abstract class ChatEmotes {
	public static final Map<String, String> emotes = new HashMap<>();

	public static String toEmoji(String input) {
		for (Map.Entry<String, String> entry : emotes.entrySet()) {
			input = input.replaceAll(entry.getKey(), entry.getValue());
		}
		return input;
	}

	public static String toEmote(String input) {
		for (Map.Entry<String, String> entry : emotes.entrySet()) {
			input = input.replaceAll(entry.getValue(), entry.getKey());
		}
		return input;
	}

	static {
		emotes.put("☠", "💀"); // skull
		emotes.put("☺", "🙂"); // smile
		emotes.put("☻", "🙂"); // smile2
		emotes.put("❤", "♥️"); // heart
		emotes.put("♦", "♦️"); // diamond
		emotes.put("♣", "♣️"); // club
		emotes.put("♠", "♠️"); // spade
		emotes.put("♂", "♂️"); // male
		emotes.put("♀", "♀️"); // female
		emotes.put("♪", "🎵"); // note
		emotes.put("♫", "🎶"); // note2
		emotes.put("☀", "☀️"); // sun
		emotes.put("↑", "⬆️"); // up
		emotes.put("↓", "⬇️"); // down
		emotes.put("→", "➡️"); // right
		emotes.put("←", "⬅️"); // left
		emotes.put("☁", "☁️"); // cloud
		emotes.put("☽", "🌙"); // moon
		emotes.put("✉", "✉️"); // letter
		emotes.put("☂", "☂️"); // umbrella
		emotes.put("⛄", "☃️"); // snowman
		emotes.put("⌛", "⌛"); // hourglass
		emotes.put("⌚", "⌚"); // time
		emotes.put("⚐", "🏳️"); // flag
		emotes.put("⚡", "⚡"); // electric
		emotes.put("⛏", "⛏️"); // pickaxe
		emotes.put("✔", "✅"); // tick
		emotes.put("❄", "❄️"); // snowflake
		emotes.put("❌", "❌"); // cross
		emotes.put("⭐", "⭐"); // star
	}
}

