package adonis.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;

public final class LightningStick {
	private LightningStick() {
	}

	public static InteractionResult strike(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			return InteractionResult.PASS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}

		BlockPos targetPos = player.blockPosition().relative(player.getDirection(), 10);
		@SuppressWarnings("unchecked")
		EntityType<? extends LightningBolt> lightningType = (EntityType<? extends LightningBolt>) BuiltInRegistries.ENTITY_TYPE
			.getOptional(Identifier.fromNamespaceAndPath("minecraft", "lightning_bolt"))
			.orElseThrow(() -> new IllegalStateException("Missing lightning bolt entity type"));
		LightningBolt lightningBolt = new LightningBolt(lightningType, serverLevel);
		lightningBolt.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
		serverLevel.addFreshEntity(lightningBolt);

		return InteractionResult.SUCCESS;
	}
}
