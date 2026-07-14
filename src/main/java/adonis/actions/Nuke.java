package adonis.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class Nuke {
	private Nuke() {
	}

	public static InteractionResult cast(Level level, Player player) {
		if (level.isClientSide()) {
			return InteractionResult.PASS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}

		HitResult hit = player.pick(200.0D, 0.0F, false);
		if (!(hit instanceof BlockHitResult blockHit)) {
			return InteractionResult.PASS;
		}

		BlockPos center = blockHit.getBlockPos();
		spawnNuke(serverLevel, center, 0, 6);
		spawnNuke(serverLevel, center, 4, 6);
		spawnNuke(serverLevel, center, 8, 6);
		spawnNuke(serverLevel, center, 12, 6);
		spawnNuke(serverLevel, center, 16, 6);
		spawnNuke(serverLevel, center, 20, 6);
		spawnNuke(serverLevel, center, 24, 6);
		spawnNuke(serverLevel, center, 28, 6);
		spawnNuke(serverLevel, center, 32, 6);

		return InteractionResult.SUCCESS;
	}

	private static void spawnNuke(ServerLevel level, BlockPos center, int radius, int spawnHeight) {

		for (int angle = 0; angle < 360; angle += 10) {

			double radians = Math.toRadians(angle);

			int x = (int) Math.round(radius * Math.cos(radians));
			int z = (int) Math.round(radius * Math.sin(radians));


			BlockPos spawnPos = center.offset(x, spawnHeight, z);
			PrimedTnt tnt = EntityTypes.TNT.create(level, EntitySpawnReason.TRIGGERED);

			if (tnt == null) {
				continue;
			}

			tnt.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
			tnt.setFuse(40);
			level.addFreshEntity(tnt);
		}

	}
}
