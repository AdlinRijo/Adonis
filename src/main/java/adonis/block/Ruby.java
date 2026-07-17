package adonis.block;

import java.util.function.Function;

import adonis.Adonis;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Ruby {
	public static final Block TEST_BLOCK = register(
		"test_block",
		Block::new,
		BlockBehaviour.Properties.of().strength(1.0F),
		true
	);

	private static <T extends Block> T register(
		String name,
		Function<BlockBehaviour.Properties, T> blockFactory,
		BlockBehaviour.Properties properties,
		boolean shouldRegisterItem
	) {
		ResourceKey<Block> blockKey = keyOfBlock(name);
		T block = blockFactory.apply(properties.setId(blockKey));

		if (shouldRegisterItem) {
			ResourceKey<Item> itemKey = keyOfItem(name);
			BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
			Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		}

		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	private static ResourceKey<Block> keyOfBlock(String name) {
		return ResourceKey.create(Registries.BLOCK, Adonis.id(name));
	}

	private static ResourceKey<Item> keyOfItem(String name) {
		return ResourceKey.create(Registries.ITEM, Adonis.id(name));
	}

	public static void initialize() {
		// Blocks are registered via static fields above.
	}
}
