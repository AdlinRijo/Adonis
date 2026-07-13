package adonis.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;


public final class Stab {


    public static InteractionResult cast(Level level, Player player) {

        if(level.isClientSide()){
            return InteractionResult.PASS;
        }
        if(!(level instanceof ServerLevel serverLevel)){
            return InteractionResult.PASS;
        }

        HitResult hit = player.pick(100, 0.00F,false);

        if (!(hit instanceof BlockHitResult blockHit)){
            return InteractionResult.PASS;
        }

        BlockPos center = blockHit.getBlockPos();

        spawnStab(serverLevel, center, 50);

        return InteractionResult.SUCCESS;
    }

    public static void spawnStab(ServerLevel level, BlockPos center, int dept) {

        for (int i=0;i<dept;i++){

            BlockPos spawnpos = center.below(i);

            PrimedTnt tnt = EntityTypes.TNT.create(level, EntitySpawnReason.TRIGGERED);
            if(tnt == null){
                continue;
            }
            tnt.setFuse(0);
            tnt.setPos(spawnpos.getX()+0.5, spawnpos.getY(), spawnpos.getZ()+0.5);
            level.addFreshEntity(tnt);
        }

    }
}
