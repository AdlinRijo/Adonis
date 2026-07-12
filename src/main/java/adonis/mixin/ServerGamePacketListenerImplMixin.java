package adonis.mixin;

import adonis.actions.Nuke;
import adonis.item.GenesisPen;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;)V", at = @At("HEAD"), cancellable = true)
	private void adonis$handleChat(ServerboundChatPacket packet, CallbackInfo ci) {
		String message = packet.message().trim();
		if (!"nuke".equalsIgnoreCase(message)) {
			return;
		}

		ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
		boolean holdingPen = mainHand.is(GenesisPen.GENESIS_PEN) || offHand.is(GenesisPen.GENESIS_PEN);
		if (!holdingPen) {
			return;
		}

		Nuke.cast(player.level(), player);
		ci.cancel();
	}
}
