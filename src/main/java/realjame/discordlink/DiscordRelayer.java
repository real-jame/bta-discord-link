package realjame.discordlink;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;

import static realjame.discordlink.DiscordLink.bridgeChannel;
import static realjame.discordlink.Log.logError;

public class DiscordRelayer extends ListenerAdapter {
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;
		if (event.getChannel() != bridgeChannel) return;
		String messageContent = event.getMessage().getContentRaw();
		// TODO: convert markdown formatting when possible (bold, italic, underline)
		messageContent = ChatEmotes.toEmote(messageContent);
//		System.out.println("User sent message: " + messageContent);
		try {
			MinecraftServer.getInstance().configManager.sendEncryptedChatToAllPlayers("[" + getDisplayName(event.getMember()) + TextFormatting.RESET + "] " + TextFormatting.RESET + TextFormatting.LIGHT_GRAY + messageContent);
		} catch (Exception e) {
			logError("Error relaying Discord message to Minecraft: " + e.getMessage());
		}
	}

	private static String getDisplayName(Member member) {
		String name = member.getNickname();
		if (name == null) {
			name = member.getUser().getName();
		} else {
			name = TextFormatting.ITALIC + name;
		}
		return name;
	}
}
