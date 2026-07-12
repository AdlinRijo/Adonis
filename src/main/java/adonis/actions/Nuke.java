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
		spawnRing(serverLevel, center, 8, 15);
		spawnRing(serverLevel, center, 14, 10);
		spawnRing(serverLevel, center, 20, 7);

		return InteractionResult.SUCCESS;
	}

	private static void spawnRing(ServerLevel level, BlockPos center, int radius, int spawnHeight) {
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (x * x + z * z > radius * radius) {
					continue;
				}

				BlockPos spawnPos = center.offset(x, spawnHeight, z);
				PrimedTnt tnt = EntityTypes.TNT.create(level, EntitySpawnReason.TRIGGERED);
				if (tnt == null) {
					continue;
				}

				tnt.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
				level.addFreshEntity(tnt);
			}
		}
	}
}
