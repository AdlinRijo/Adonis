package adonis.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world	.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Function;

import adonis.Adonis;

public class GenesisPen {
	public static final Item GENESIS_PEN = register("genesis_pen", GenesisPenItem::new, new Item.Properties());

	public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
		// Create the item key.
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Adonis.id(name));

		// Create the item instance.
		T item = itemFactory.apply(settings.setId(itemKey));

		// Register the item.
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		return item;
	}

	public static void initialize() {
		// Items are registered via static fields above
	}

	public static class GenesisPenItem extends Item {
		private static final int COOLDOWN_TICKS = 100; // 10 seconds (20 ticks per second)

		public GenesisPenItem(Item.Properties properties) {
			super(properties);
		}

		@Override
		public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
			ItemStack itemStack = player.getItemInHand(hand);

			if (player.isLocalPlayer()) {
				return InteractionResult.PASS;
			}

			if (!player.getCooldowns().isOnCooldown(itemStack)) {
				player.getCooldowns().addCooldown(itemStack, COOLDOWN_TICKS);
				Adonis.LOGGER.info("Genesis Pen used by {}", player.getName().getString());
				return InteractionResult.SUCCESS;
			}

			return InteractionResult.FAIL;
		}
	}
}
