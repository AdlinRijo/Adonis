package adonis.entity;

import adonis.Adonis;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class MiniGolemEntity extends PathfinderMob {
    public MiniGolemEntity(Level world) {
        this(ModEntityTypes.MINI_GOLEM, world);
    }

    public MiniGolemEntity(EntityType<? extends MiniGolemEntity> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createCubeAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5)
                .add(Attributes.TEMPT_RANGE, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public class ModEntityTypes {
        public static final EntityType<MiniGolemEntity> MINI_GOLEM = register(
                "mini_golem", EntityType.Builder.<MiniGolemEntity>of(MiniGolemEntity::new, MobCategory.MISC)
                        .sized(0.75f, 1.75f));

        private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Adonis.MOD_ID, name));
            return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
        }

        public static void registerModEntityTypes() {
            Adonis.LOGGER.info("Registering EntityTypes for " + Adonis.MOD_ID);
        }

        public static void registerAttributes() {
            FabricDefaultAttributeRegistry.register(MINI_GOLEM, MiniGolemEntity.createCubeAttributes());
        }
    }
}

