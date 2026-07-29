package adonis.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

import java.util.Random;


public final class Stab {


    public static InteractionResult cast(Level level, Player player) {

        Spell.cast(level, player);

        HitResult hit = player.pick(100, 0.00F,false);

        ServerLevel serverLevel = (ServerLevel) level;
        BlockHitResult blockHit = (BlockHitResult) hit;

        BlockPos center = blockHit.getBlockPos();

        spawnStab(serverLevel, center, 100);

        return InteractionResult.SUCCESS;
    }

    public static void spawnStab(ServerLevel level, BlockPos center, int dept) {

        for (int i=0;i<dept;i++){

            BlockPos spawnpos = center.below(i);

            PrimedTnt tnt = EntityTypes.TNT.create(level, EntitySpawnReason.TRIGGERED);
            if(tnt == null){
                continue;
            }
            Random random = new Random();
            int fuse = random.nextInt(0,20);
            tnt.setFuse(fuse);
            tnt.setPos(spawnpos.getX()+0.5, spawnpos.getY(), spawnpos.getZ()+0.5);
            level.addFreshEntity(tnt);
        }

    }
}
