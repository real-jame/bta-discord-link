package realjame.discordlink.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetLoginHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import realjame.discordlink.DiscordLink;

@Mixin(value = NetLoginHandler.class, remap = false)
public abstract class NetLoginHandlerMixin {
	@Inject(method = "doLogin", at = @At("TAIL"))
	public void doLogin(CallbackInfo ci, @Local EntityPlayerMP entityplayermp) {
		if (entityplayermp != null) {
			DiscordLink.sendJoinMessage(entityplayermp);
		}
	}
}
