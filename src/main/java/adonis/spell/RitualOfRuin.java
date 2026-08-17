package adonis.spell;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class RitualOfRuin {

    private static final int TOTAL_TICKS = 150;

    private static final int PHASE_CHANNELING_END = 30;
    private static final int PHASE_SKY_CIRCLE_END = 60;
    private static final int PHASE_ENERGY_END = 100;
    private static final int PHASE_BEAM_END = 110;
    private static final int PHASE_BLAST_TICK = 110;

    private static final int BLAST_RADIUS = 50;
    private static final float BLAST_DAMAGE = 40.0F;
    private static final int FREEZE_TICKS = 400;

    private static final int CIRCLE_RADIUS = 50;

    private static final int ICE_DUST_COLOR = 0xA6D9FF;
    private static final int SOUL_DUST_COLOR = 0x408CE6;

    private static final DustParticleOptions ICE_DUST =
            new DustParticleOptions(ICE_DUST_COLOR, 1.4F);

    private static final DustParticleOptions SOUL_DUST =
            new DustParticleOptions(SOUL_DUST_COLOR, 1.0F);

    private static final List<RitualInstance> ACTIVE_RITUALS = new ArrayList<>();

    private RitualOfRuin() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (int i = ACTIVE_RITUALS.size() - 1; i >= 0; i--) {
                RitualInstance ritual = ACTIVE_RITUALS.get(i);

                if (ritual.tick()) {
                    ACTIVE_RITUALS.remove(i);
                }
            }
        });
    }

    public static InteractionResult cast(Level level, Player player) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        RitualOrigin origin = new RitualOrigin(
                player.getX(),
                player.getY(),
                player.getZ()
        );

        ACTIVE_RITUALS.add(
                new RitualInstance(
                        serverLevel,
                        player,
                        origin
                )
        );

        return InteractionResult.SUCCESS;
    }

    private record RitualOrigin(double x, double y, double z) {
    }

    private static class RitualInstance {

        private final ServerLevel level;
        private final Player player;
        private final RitualOrigin origin;

        private int tick;

        private RitualInstance(
                ServerLevel level,
                Player player,
                RitualOrigin origin
        ) {
            this.level = level;
            this.player = player;
            this.origin = origin;
            this.tick = 0;
        }

        private boolean tick() {
            if (player.isRemoved() || !player.isAlive()) {
                return true;
            }

            runTick(
                    level,
                    player,
                    origin,
                    tick
            );

            tick++;

            return tick >= TOTAL_TICKS;
        }
    }

    private static void runTick(
            ServerLevel level,
            Player player,
            RitualOrigin origin,
            int tick
    ) {
        if (tick < PHASE_CHANNELING_END) {
            phaseChanneling(level, origin, tick);
        } else if (tick < PHASE_SKY_CIRCLE_END) {
            phaseSkyCircleFormation(level, origin, tick);
        } else if (tick < PHASE_ENERGY_END) {
            phaseEnergyGathering(level, origin, tick);
        } else if (tick < PHASE_BEAM_END) {
            phaseHeavenBeam(level, origin, tick);
        } else if (tick < PHASE_BLAST_TICK) {
            phaseBlastCharging(level, origin, tick);
        } else if (tick == PHASE_BLAST_TICK) {
            phaseCatastrophicBlast(level, player, origin);
        } else {
            phaseAftermath(level, origin, tick);
        }
    }

    private static void phaseChanneling(
            ServerLevel level,
            RitualOrigin origin,
            int tick
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        level.sendParticles(
                ParticleTypes.SOUL,
                cx,
                cy + 0.5,
                cz,
                12,
                0.8,
                0.5,
                0.8,
                0.05
        );

        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                cx,
                cy + 1.0,
                cz,
                8,
                0.5,
                0.8,
                0.5,
                0.02
        );

        level.sendParticles(
                ParticleTypes.SMOKE,
                cx,
                cy + 0.2,
                cz,
                15,
                1.0,
                0.3,
                1.0,
                0.05
        );

        level.sendParticles(
                ParticleTypes.ASH,
                cx,
                cy + 0.3,
                cz,
                10,
                0.8,
                0.4,
                0.8,
                0.02
        );

        double spiralAngle = tick * 0.5;
        double spiralRadius =
                2.0 - (tick / (double) PHASE_CHANNELING_END) * 0.8;

        double sx =
                cx + spiralRadius * Math.cos(spiralAngle);

        double sz =
                cz + spiralRadius * Math.sin(spiralAngle);

        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                sx,
                cy + 1.0,
                sz,
                3,
                0.05,
                0.4,
                0.05,
                0
        );

        level.sendParticles(
                SOUL_DUST,
                sx,
                cy + 0.6,
                sz,
                3,
                0.05,
                0.2,
                0.05,
                0
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.1,
                cz,
                3.0,
                80,
                tick * 0.1
        );

        if (tick % 10 == 0) {
            level.playSound(
                    null,
                    cx,
                    cy,
                    cz,
                    SoundEvents.WITHER_AMBIENT,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.5F
            );

            level.playSound(
                    null,
                    cx,
                    cy,
                    cz,
                    SoundEvents.AMBIENT_CAVE,
                    SoundSource.PLAYERS,
                    1.5F,
                    0.8F
            );
        }
    }

    private static void phaseSkyCircleFormation(
            ServerLevel level,
            RitualOrigin origin,
            int tick
    ) {
        drawSkyRings(
                level,
                origin,
                tick,
                8
        );

        drawGroundCircle(
                level,
                origin.x(),
                origin.y() + 0.1,
                origin.z(),
                8.0,
                160,
                tick * 0.08
        );

        if (tick == PHASE_CHANNELING_END) {
            level.playSound(
                    null,
                    origin.x(),
                    origin.y() + 15.0,
                    origin.z(),
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.PLAYERS,
                    3.0F,
                    0.8F
            );
        }
    }

    private static void drawSkyRings(
            ServerLevel level,
            RitualOrigin origin,
            int tick,
            int density
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        double outerAngleOffset = tick * 0.15;

        for (int i = 0; i < 64; i++) {

            double angle =
                    outerAngleOffset +
                            (i * Math.PI * 2.0 / 64.0);

            double radius = 16.0;

            double x =
                    cx + radius * Math.cos(angle);

            double z =
                    cz + radius * Math.sin(angle);

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x,
                    cy + 15.0,
                    z,
                    density,
                    0.1,
                    0.1,
                    0.1,
                    0.01
            );

            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x,
                    cy + 15.0,
                    z,
                    2,
                    0.1,
                    0.1,
                    0.1,
                    0.01
            );

            if (i % 4 == 0) {
                level.sendParticles(
                        ICE_DUST,
                        x,
                        cy + 15.05,
                        z,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        0
                );
            }
        }

        double innerAngleOffset = -tick * 0.2;

        for (int i = 0; i < 32; i++) {

            double angle =
                    innerAngleOffset +
                            (i * Math.PI * 2.0 / 32.0);

            double radius = 8.0;

            double x =
                    cx + radius * Math.cos(angle);

            double z =
                    cz + radius * Math.sin(angle);

            level.sendParticles(
                    ParticleTypes.GLOW,
                    x,
                    cy + 15.0,
                    z,
                    density,
                    0.1,
                    0.1,
                    0.1,
                    0.01
            );

            level.sendParticles(
                    ParticleTypes.SOUL,
                    x,
                    cy + 15.0,
                    z,
                    2,
                    0.1,
                    0.05,
                    0.1,
                    0
            );
        }
    }

    private static void phaseEnergyGathering(
            ServerLevel level,
            RitualOrigin origin,
            int tick
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        drawSkyRings(
                level,
                origin,
                tick,
                2
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.1,
                cz,
                20.0,
                250,
                tick * 0.1
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.15,
                cz,
                35.0,
                350,
                -tick * 0.08
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.2,
                cz,
                50.0,
                500,
                tick * 0.05
        );

        for (int b = 0; b < 5; b++) {
            drawConvergingBeam(
                    level,
                    origin
            );
        }

        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                cx,
                cy + 2.0,
                cz,
                15,
                2.0,
                2.0,
                2.0,
                0.06
        );

        level.sendParticles(
                ICE_DUST,
                cx,
                cy + 1.5,
                cz,
                8,
                1.5,
                1.5,
                1.5,
                0.05
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                cx,
                cy + 2.0,
                cz,
                10,
                1.0,
                1.0,
                1.0,
                0.03
        );

        if (tick % 10 == 0) {
            level.playSound(
                    null,
                    cx,
                    cy,
                    cz,
                    SoundEvents.BEACON_AMBIENT,
                    SoundSource.PLAYERS,
                    2.0F,
                    1.2F
            );

            level.playSound(
                    null,
                    cx,
                    cy,
                    cz,
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.5F
            );
        }
    }

    private static void drawConvergingBeam(
            ServerLevel level,
            RitualOrigin origin
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        double randomAngle =
                level.getRandom().nextDouble() * Math.PI * 2.0;

        double radius = 16.0;

        double startX =
                cx + radius * Math.cos(randomAngle);

        double startZ =
                cz + radius * Math.sin(randomAngle);

        for (double h = 0.0; h <= 1.0; h += 0.04) {

            double jitter =
                    (level.getRandom().nextDouble() - 0.5)
                            * 0.7
                            * Math.sin(h * Math.PI);

            double px =
                    startX +
                            (cx - startX) * h +
                            jitter;

            double py =
                    cy + 15.0 +
                            ((cy + 1.0) - (cy + 15.0)) * h;

            double pz =
                    startZ +
                            (cz - startZ) * h +
                            jitter;

            level.sendParticles(
                    ParticleTypes.PORTAL,
                    px,
                    py,
                    pz,
                    2,
                    0.05,
                    0.05,
                    0.05,
                    0.01
            );

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    px,
                    py,
                    pz,
                    2,
                    0.03,
                    0.03,
                    0.03,
                    0.01
            );
        }
    }

    private static void phaseHeavenBeam(
            ServerLevel level,
            RitualOrigin origin,
            int tick
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        for (double h = 0.0; h <= 20.0; h += 0.35) {

            level.sendParticles(
                    ParticleTypes.GLOW,
                    cx,
                    cy + h,
                    cz,
                    15,
                    0.8,
                    0.25,
                    0.8,
                    0.02
            );

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    cx,
                    cy + h,
                    cz,
                    18,
                    1.0,
                    0.2,
                    1.0,
                    0.05
            );

            level.sendParticles(
                    ICE_DUST,
                    cx,
                    cy + h,
                    cz,
                    5,
                    0.2,
                    0.2,
                    0.2,
                    0.01
            );
        }

        drawGroundCircle(
                level,
                cx,
                cy + 0.15,
                cz,
                50.0,
                500,
                tick * 0.1
        );

        level.sendParticles(
                ParticleTypes.FALLING_LAVA,
                cx,
                cy + 0.1,
                cz,
                3,
                0,
                0,
                0,
                0
        );

        if (tick == PHASE_ENERGY_END) {

            level.playSound(
                    null,
                    cx,
                    cy,
                    cz,
                    SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS,
                    3.0F,
                    0.5F
            );

            level.playSound(
                    null,
                    cx,
                    cy,
                    cz,
                    SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.PLAYERS,
                    2.0F,
                    0.5F
            );
        }
    }

    private static void phaseBlastCharging(
            ServerLevel level,
            RitualOrigin origin,
            int tick
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        double progress =
                (tick - PHASE_BEAM_END) /
                        (double) (PHASE_BLAST_TICK - PHASE_BEAM_END);

        double radius =
                5.0 + progress * 45.0;

        drawGroundCircle(
                level,
                cx,
                cy + 0.2,
                cz,
                radius,
                Math.max(100, (int) (radius * 12)),
                -tick * 0.15
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                cx,
                cy + 2.0,
                cz,
                30,
                2.0,
                2.0,
                2.0,
                0.15
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                cx,
                cy + 3.0,
                cz,
                25,
                2.0,
                3.0,
                2.0,
                0.1
        );

        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                cx,
                cy + 1.0,
                cz,
                20,
                2.0,
                1.0,
                2.0,
                0.08
        );
    }

    private static void phaseCatastrophicBlast(
            ServerLevel level,
            Player player,
            RitualOrigin origin
    ) {
        playBlastSounds(
                level,
                origin
        );

        spawnBlastParticles(
                level,
                origin
        );

        freezeTerrain(
                level,
                origin
        );

        freezeAndDamageEntities(
                level,
                player,
                origin
        );
    }

    private static void playBlastSounds(
            ServerLevel level,
            RitualOrigin origin
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        level.playSound(
                null,
                cx,
                cy,
                cz,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                4.0F,
                0.5F
        );

        level.playSound(
                null,
                cx,
                cy,
                cz,
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                3.0F,
                0.5F
        );

        level.playSound(
                null,
                cx,
                cy,
                cz,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                4.0F,
                0.5F
        );

        level.playSound(
                null,
                cx,
                cy,
                cz,
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS,
                2.0F,
                0.8F
        );
    }

    private static void spawnBlastParticles(
            ServerLevel level,
            RitualOrigin origin
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                cx,
                cy + 1.0,
                cz,
                15,
                3.0,
                2.0,
                3.0,
                0.1
        );

        level.sendParticles(
                ParticleTypes.EXPLOSION,
                cx,
                cy + 1.0,
                cz,
                40,
                4.0,
                3.0,
                4.0,
                0.1
        );

        level.sendParticles(
                ParticleTypes.CLOUD,
                cx,
                cy + 1.0,
                cz,
                500,
                10.0,
                3.0,
                10.0,
                0.4
        );

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                cx,
                cy + 1.0,
                cz,
                150,
                8.0,
                3.0,
                8.0,
                0.2
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.15,
                cz,
                10.0,
                180,
                0
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.2,
                cz,
                20.0,
                260,
                0
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.25,
                cz,
                30.0,
                360,
                0
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.3,
                cz,
                40.0,
                440,
                0
        );

        drawGroundCircle(
                level,
                cx,
                cy + 0.35,
                cz,
                50.0,
                600,
                0
        );

        for (int i = 0; i < 2000; i++) {

            double angle =
                    level.getRandom().nextDouble() *
                            Math.PI * 2.0;

            double radius =
                    Math.sqrt(level.getRandom().nextDouble()) *
                            CIRCLE_RADIUS;

            double px =
                    cx + Math.cos(angle) * radius;

            double pz =
                    cz + Math.sin(angle) * radius;

            double py =
                    cy +
                            level.getRandom().nextDouble() * 10.0;

            level.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    px,
                    py,
                    pz,
                    1,
                    0.08,
                    0.08,
                    0.08,
                    0.04
            );

            if (i % 2 == 0) {
                level.sendParticles(
                        ParticleTypes.WHITE_ASH,
                        px,
                        py,
                        pz,
                        1,
                        0.08,
                        0.08,
                        0.08,
                        0.02
                );
            }

            if (i % 3 == 0) {
                level.sendParticles(
                        ParticleTypes.SOUL,
                        px,
                        py,
                        pz,
                        1,
                        0.05,
                        0.05,
                        0.05,
                        0.01
                );
            }

            if (i % 5 == 0) {
                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        px,
                        py,
                        pz,
                        1,
                        0.1,
                        0.1,
                        0.1,
                        0.03
                );
            }
        }

        for (int i = 0; i < 500; i++) {

            double angle =
                    level.getRandom().nextDouble() *
                            Math.PI * 2.0;

            double radius =
                    5.0 +
                            level.getRandom().nextDouble() * 45.0;

            double px =
                    cx + Math.cos(angle) * radius;

            double pz =
                    cz + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.FALLING_LAVA,
                    px,
                    cy + 0.15,
                    pz,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
    }

    private static void drawGroundCircle(
            ServerLevel level,
            double cx,
            double cy,
            double cz,
            double radius,
            int points,
            double rotation
    ) {
        for (int i = 0; i < points; i++) {

            double angle =
                    rotation +
                            (Math.PI * 2.0 * i) / points;

            double x =
                    cx + Math.cos(angle) * radius;

            double z =
                    cz + Math.sin(angle) * radius;

            level.sendParticles(
                    ICE_DUST,
                    x,
                    cy,
                    z,
                    2,
                    0.12,
                    0.04,
                    0.12,
                    0.01
            );

            if (i % 4 == 0) {
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        x,
                        cy + 0.15,
                        z,
                        1,
                        0.04,
                        0.05,
                        0.04,
                        0
                );
            }

            if (i % 8 == 0) {
                level.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        x,
                        cy + 0.25,
                        z,
                        1,
                        0.05,
                        0.1,
                        0.05,
                        0.01
                );
            }
        }
    }

    private static void freezeTerrain(
            ServerLevel level,
            RitualOrigin origin
    ) {
        BlockPos centerPos =
                new BlockPos(
                        (int) Math.floor(origin.x()),
                        (int) Math.floor(origin.y()),
                        (int) Math.floor(origin.z())
                );

        for (int dx = -BLAST_RADIUS; dx <= BLAST_RADIUS; dx++) {

            for (int dy = -BLAST_RADIUS; dy <= BLAST_RADIUS; dy++) {

                for (int dz = -BLAST_RADIUS; dz <= BLAST_RADIUS; dz++) {

                    double distanceSquared =
                            dx * dx +
                                    dy * dy +
                                    dz * dz;

                    if (distanceSquared >
                            (double) BLAST_RADIUS * BLAST_RADIUS) {
                        continue;
                    }

                    freezeSingleBlock(
                            level,
                            centerPos.offset(dx, dy, dz)
                    );
                }
            }
        }
    }

    private static void freezeSingleBlock(
            ServerLevel level,
            BlockPos pos
    ) {
        BlockState state =
                level.getBlockState(pos);

        if (state.is(Blocks.WATER)) {

            level.setBlockAndUpdate(
                    pos,
                    Blocks.ICE.defaultBlockState()
            );

        } else if (state.is(Blocks.LAVA)) {

            level.setBlockAndUpdate(
                    pos,
                    Blocks.OBSIDIAN.defaultBlockState()
            );

        } else if (
                state.isAir() &&
                        level.getBlockState(pos.below()).isSolid()
        ) {

            level.setBlockAndUpdate(
                    pos,
                    Blocks.SNOW.defaultBlockState()
            );

        } else if (
                state.is(Blocks.GRASS_BLOCK) ||
                        state.is(Blocks.DIRT)
        ) {

            double random =
                    level.getRandom().nextDouble();

            if (random < 0.1) {

                level.setBlockAndUpdate(
                        pos,
                        Blocks.PACKED_ICE.defaultBlockState()
                );

            } else if (random < 0.2) {

                level.setBlockAndUpdate(
                        pos,
                        Blocks.BLUE_ICE.defaultBlockState()
                );

            } else if (random < 0.35) {

                level.setBlockAndUpdate(
                        pos,
                        Blocks.SNOW_BLOCK.defaultBlockState()
                );

            } else if (random < 0.45) {

                level.setBlockAndUpdate(
                        pos,
                        Blocks.POWDER_SNOW.defaultBlockState()
                );
            }
        }
    }

    private static void freezeAndDamageEntities(
            ServerLevel level,
            Player caster,
            RitualOrigin origin
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        AABB aabb =
                new AABB(
                        cx - BLAST_RADIUS,
                        cy - BLAST_RADIUS,
                        cz - BLAST_RADIUS,
                        cx + BLAST_RADIUS,
                        cy + BLAST_RADIUS,
                        cz + BLAST_RADIUS
                );

        List<LivingEntity> targets =
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        aabb,
                        entity -> entity != caster
                );

        for (LivingEntity target : targets) {

            if (
                    target.distanceToSqr(cx, cy, cz) >
                            (double) BLAST_RADIUS * BLAST_RADIUS
            ) {
                continue;
            }

            target.setTicksFrozen(
                    FREEZE_TICKS
            );

            target.hurt(
                    level.damageSources().magic(),
                    BLAST_DAMAGE
            );

            level.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    target.getX(),
                    target.getY() + 1.0,
                    target.getZ(),
                    40,
                    0.6,
                    1.0,
                    0.6,
                    0.03
            );

            level.sendParticles(
                    ParticleTypes.ITEM_SNOWBALL,
                    target.getX(),
                    target.getY() + 1.0,
                    target.getZ(),
                    20,
                    0.4,
                    0.8,
                    0.4,
                    0.03
            );

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    target.getX(),
                    target.getY() + 1.0,
                    target.getZ(),
                    15,
                    0.5,
                    1.0,
                    0.5,
                    0.02
            );

            if (target.isAlive()) {

                encaseInIce(
                        level,
                        target.blockPosition()
                );

                encaseInIce(
                        level,
                        target.blockPosition().above()
                );
            }
        }
    }

    private static void encaseInIce(
            ServerLevel level,
            BlockPos pos
    ) {
        if (level.getBlockState(pos).isAir()) {

            level.setBlockAndUpdate(
                    pos,
                    Blocks.ICE.defaultBlockState()
            );
        }
    }

    private static void phaseAftermath(
            ServerLevel level,
            RitualOrigin origin,
            int tick
    ) {
        double cx = origin.x();
        double cy = origin.y();
        double cz = origin.z();

        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                cx,
                cy + 5.0,
                cz,
                30,
                12.0,
                5.0,
                12.0,
                0.05
        );

        level.sendParticles(
                ParticleTypes.WHITE_ASH,
                cx,
                cy + 5.0,
                cz,
                15,
                12.0,
                5.0,
                12.0,
                0.02
        );

        level.sendParticles(
                ParticleTypes.CLOUD,
                cx,
                cy + 3.0,
                cz,
                8,
                10.0,
                2.0,
                10.0,
                0.01
        );

        double angle =
                tick * 0.3;

        double spiritY =
                cy +
                        ((tick - PHASE_BEAM_END) * 0.05);

        double sx =
                cx + 1.5 * Math.cos(angle);

        double sz =
                cz + 1.5 * Math.sin(angle);

        level.sendParticles(
                ParticleTypes.END_ROD,
                sx,
                spiritY,
                sz,
                3,
                0.03,
                0.03,
                0.03,
                0.01
        );

        if (tick % 5 == 0) {

            level.sendParticles(
                    ParticleTypes.GLOW,
                    sx,
                    spiritY,
                    sz,
                    5,
                    0.15,
                    0.2,
                    0.15,
                    0.01
            );
        }

        if (tick % 15 == 0) {

            level.playSound(
                    null,
                    cx,
                    cy,
                    cz,
                    SoundEvents.WEATHER_RAIN,
                    SoundSource.PLAYERS,
                    0.5F,
                    0.5F
            );
        }
    }
}