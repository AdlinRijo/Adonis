package adonis.actions;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class MakeBig {
    public static InteractionResult cast(Level level, Player player, Entity entity){
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        if(Entity instanceof LivingEntity){
            return InteractionResult.PASS;
        }


        EntityHitResult tar =


        alterSize(player,level,entity);
        return InteractionResult.SUCCESS;
    }

    public static EntityHitResult alterSize(Player player, Level level, Entity entity){
        entity.setNoGravity(false);
        entity.setPos(0,100,0);

        return null;
    }



}
