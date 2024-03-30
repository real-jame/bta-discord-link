package realjame.discordlink.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import realjame.discordlink.ChatEmotes;
import realjame.discordlink.DiscordLink;

@Mixin(value = NetServerHandler.class, remap = false)
public abstract class NetServerHandlerMixin {
	@Shadow
	private EntityPlayerMP playerEntity;
	@Inject(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/net/ChatEmotes;process(Ljava/lang/String;)Ljava/lang/String;", shift = At.Shift.AFTER))
	public void handleChat(CallbackInfo ci, @Local String s) {
		if (playerEntity != null) {
			String chatMessage = s;
			chatMessage = "<" + getDisplayName(playerEntity) + "> " + chatMessage;
			chatMessage = ChatEmotes.toEmoji(chatMessage);
			System.out.println(chatMessage);
			DiscordLink.relayChatMessage(playerEntity, chatMessage, playerEntity.chatColor);
        }
	}

	// Same as the one provided by EntityPlayer.class but without adding formatting codes and using Discord markdown
	@Unique
	private static String getDisplayName(EntityPlayerMP playerEntity) {
		String name = playerEntity.nickname;
		if (name.isEmpty()) {
			name = playerEntity.username;
		} else {
			name = "*" + name + "*";
		}
		return name;
	}
}
