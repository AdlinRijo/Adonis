package adonis.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Random;

public final class Nuke {
	private Nuke() {
	}

	public static void cnfNuke(Level level, Player player) {

        Spell.cast(level, player);

		HitResult hit = player.pick(200.0D, 0.0F, false);
		BlockHitResult blockHit = (BlockHitResult) hit;
		BlockPos center = blockHit.getBlockPos();
		ServerLevel sl = (ServerLevel) level;
		spawnNuke(sl, center, 0);
		spawnNuke(sl, center, 4);
		spawnNuke(sl, center, 8);
		spawnNuke(sl, center, 12);
		spawnNuke(sl, center, 16);
		spawnNuke(sl, center, 20);
		spawnNuke(sl, center, 24);
		spawnNuke(sl, center, 28);
		spawnNuke(sl, center, 32);

	}

	private static void spawnNuke(ServerLevel level, BlockPos center, int radius) {
		int spawnHeight = 6;


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


			level.sendParticles(ParticleTypes.SOUL,spawnPos.getX(),spawnPos.getY(),spawnPos.getZ(),100,0	,0,0,0.1);

			Random random = new Random();
			int fuse = random.nextInt(40,120);
			tnt.setFuse(fuse);
			level.addFreshEntity(tnt);
		}

	}
}
