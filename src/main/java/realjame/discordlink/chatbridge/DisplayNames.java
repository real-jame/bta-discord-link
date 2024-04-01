package realjame.discordlink.chatbridge;

import net.dv8tion.jda.api.entities.Member;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.EntityPlayerMP;

// Given a Discord/BTA member/player object, find either their nickname or username to use.
public class DisplayNames {

	static String getDiscord(Member member) {
		String name = member.getNickname();
		if (name == null) {
			name = member.getUser().getName();
		} else {
			name = TextFormatting.ITALIC + name;
		}
		return name;
	}

	// Same as the one provided by EntityPlayer.class but without adding formatting codes and using Discord markdown
	static String getBTA(EntityPlayerMP playerEntity) {
		String name = playerEntity.nickname;
		if (name.isEmpty()) {
			name = playerEntity.username;
		} else {
			name = "*" + name + "*";
		}
		return name;
	}
}
