package adonis.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Spell {
    public static InteractionResult cast(Level level, Player player){
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }
}
