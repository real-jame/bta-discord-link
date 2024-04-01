package realjame.discordlink.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import realjame.discordlink.DiscordLink;

@Mixin(value = NetServerHandler.class, remap = false)
public abstract class NetServerHandlerMixin {
	@Shadow
	private EntityPlayerMP playerEntity;
	@Inject(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/net/ChatEmotes;process(Ljava/lang/String;)Ljava/lang/String;", shift = At.Shift.AFTER))
	public void handleChat(CallbackInfo ci, @Local String s) {
		if (playerEntity != null) {
            System.out.println(s);
			DiscordLink.relayBTAChatMessage(playerEntity, s, playerEntity.chatColor);
        }
	}
}
