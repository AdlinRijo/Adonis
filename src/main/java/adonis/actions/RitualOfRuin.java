package adonis.actions;

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

import java.util.List;

/**
 * "Ritual of Ruin" - a written-style summoning ritual.
 * <p>
 * Broken into six timed phases (ticks are ~100ms apart, 150 iterations total ≈ 15s):
 * 1. Channeling            (0–3s)   – dark soul energy gathers around the caster
 * 2. Sky Circle Formation  (3–6s)   – a rotating double ring forms overhead
 * 3. Energy Gathering      (6–10s)  – lightning-like beams converge into the circle
 * 4. Heaven Beam           (10–11s) – a single blinding column descends
 * 5. Catastrophic Blast    (11s)    – the freezing shockwave, block + entity effects
 * 6. Aftermath             (11–15s) – residual snow and Chloe's spirit
 */
public final class RitualOfRuin {

    // ---- Tuning constants -------------------------------------------------
    private static final int TOTAL_TICKS = 150;
    private static final int TICK_MS = 100;

    private static final int PHASE_CHANNELING_END = 30;   // 0–3s
    private static final int PHASE_SKY_CIRCLE_END = 60;   // 3–6s
    private static final int PHASE_ENERGY_END = 100;      // 6–10s
    private static final int PHASE_BEAM_END = 110;        // 10–11s
    private static final int PHASE_BLAST_TICK = 110;      // 11s (single instant)

    private static final int BLAST_RADIUS = 15;
    private static final float BLAST_DAMAGE = 40.0F;
    private static final int FREEZE_TICKS = 400;

    // Icy blue-white tint used for the DUST particle accents in phases 3–4.
    // This DustParticleOptions build packs color as a single 0xRRGGBB int rather than a Vector3f.
    private static final int ICE_DUST_COLOR = 0xA6D9FF;   // r=0.65 g=0.85 b=1.00
    private static final int SOUL_DUST_COLOR = 0x408CE6;  // r=0.25 g=0.55 b=0.90

    private static final DustParticleOptions ICE_DUST = new DustParticleOptions(ICE_DUST_COLOR, 1.4f);
    private static final DustParticleOptions SOUL_DUST = new DustParticleOptions(SOUL_DUST_COLOR, 1.0f);

    private RitualOfRuin() {
    }

    // ---- Entry point --------------------------------------------------

    public static InteractionResult cast(Level level, Player player) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        RitualOrigin origin = new RitualOrigin(player.getX(), player.getY(), player.getZ());

        // Run the ritual timeline off-thread so sleeps don't block the server tick loop.
        new Thread(() -> runRitual(serverLevel, player, origin), "ritual-of-ruin").start();

        return InteractionResult.SUCCESS;
    }

    /** Simple value holder for the cast location, captured once at cast time. */
    private record RitualOrigin(double x, double y, double z) {
    }

    // ---- Timeline driver ------------------------------------------------

    private static void runRitual(ServerLevel serverLevel, Player player, RitualOrigin origin) {
        try {
            for (int tick = 0; tick < TOTAL_TICKS; tick++) {
                final int currentTick = tick;

                if (player.isRemoved() || !player.isAlive()) {
                    break;
                }

                serverLevel.getServer().execute(() -> runTick(serverLevel, player, origin, currentTick));

                Thread.sleep(TICK_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Dispatches a single tick to the appropriate phase handler. */
    private static void runTick(ServerLevel serverLevel, Player player, RitualOrigin o, int tick) {
        if (tick < PHASE_CHANNELING_END) {
            phaseChanneling(serverLevel, o, tick);
        } else if (tick < PHASE_SKY_CIRCLE_END) {
            phaseSkyCircleFormation(serverLevel, o, tick);
        } else if (tick < PHASE_ENERGY_END) {
            phaseEnergyGathering(serverLevel, o, tick);
        } else if (tick < PHASE_BEAM_END) {
            phaseHeavenBeam(serverLevel, o, tick);
        } else if (tick == PHASE_BLAST_TICK) {
            phaseCatastrophicBlast(serverLevel, player, o);
        } else {
            phaseAftermath(serverLevel, o, tick);
        }
    }

    // ---- Phase 1: Channeling (0–3s) --------------------------------------

    private static void phaseChanneling(ServerLevel level, RitualOrigin o, int tick) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        // Dark soul energy coiling up from the ground
        level.sendParticles(ParticleTypes.SOUL, cx, cy + 0.5, cz, 6, 0.5, 0.5, 0.5, 0.05);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy + 1.0, cz, 4, 0.3, 0.5, 0.3, 0.02);
        level.sendParticles(ParticleTypes.SMOKE, cx, cy + 0.2, cz, 10, 0.8, 0.2, 0.8, 0.05);
        level.sendParticles(ParticleTypes.ASH, cx, cy + 0.3, cz, 6, 0.6, 0.3, 0.6, 0.02);

        // A slow, tightening spiral of embers around the caster's body
        double spiralAngle = tick * 0.5;
        double spiralRadius = 1.4 - (tick / (double) PHASE_CHANNELING_END) * 0.6;
        double sx = cx + spiralRadius * Math.cos(spiralAngle);
        double sz = cz + spiralRadius * Math.sin(spiralAngle);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sx, cy + 1.0, sz, 1, 0.02, 0.4, 0.02, 0.0);
        level.sendParticles(SOUL_DUST, sx, cy + 0.6, sz, 2, 0.05, 0.2, 0.05, 0.0);

        if (tick % 10 == 0) {
            level.playSound(null, cx, cy, cz, SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.0F, 0.5F);
            level.playSound(null, cx, cy, cz, SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 1.5F, 0.8F);
        }
    }

    // ---- Phase 2: Sky Circle Formation (3–6s) ----------------------------

    private static void phaseSkyCircleFormation(ServerLevel level, RitualOrigin o, int tick) {
        drawSkyRings(level, o, tick, /*density*/ 2);

        if (tick == PHASE_CHANNELING_END) {
            level.playSound(null, o.x(), o.y() + 15.0, o.z(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 3.0F, 0.8F);
        }
    }

    /** Draws the outer and inner counter-rotating rings, plus rune-like glyph ticks along the outer ring. */
    private static void drawSkyRings(ServerLevel level, RitualOrigin o, int tick, int density) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        // Outer rotating circle (radius 8.0)
        double outerAngleOffset = tick * 0.15;
        for (int i = 0; i < 16; i++) {
            double angle = outerAngleOffset + (i * Math.PI / 8);
            double xOffset = 8.0 * Math.cos(angle);
            double zOffset = 8.0 * Math.sin(angle);
            level.sendParticles(ParticleTypes.END_ROD, cx + xOffset, cy + 15.0, cz + zOffset, density, 0.1, 0.1, 0.1, 0.01);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx + xOffset, cy + 15.0, cz + zOffset, 1, 0.1, 0.1, 0.1, 0.01);
            // Rune glyph flicker every 4th node
            if (i % 4 == 0) {
                level.sendParticles(ICE_DUST, cx + xOffset, cy + 15.05, cz + zOffset, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }

        // Inner rotating circle (radius 4.0, opposite direction)
        double innerAngleOffset = -tick * 0.2;
        for (int i = 0; i < 8; i++) {
            double angle = innerAngleOffset + (i * Math.PI / 4);
            double xOffset = 4.0 * Math.cos(angle);
            double zOffset = 4.0 * Math.sin(angle);
            level.sendParticles(ParticleTypes.GLOW, cx + xOffset, cy + 15.0, cz + zOffset, density, 0.1, 0.1, 0.1, 0.01);
            level.sendParticles(ParticleTypes.SOUL, cx + xOffset, cy + 15.0, cz + zOffset, 1, 0.1, 0.05, 0.1, 0.0);
        }
    }

    // ---- Phase 3: Energy Gathering (6–10s) --------------------------------

    private static void phaseEnergyGathering(ServerLevel level, RitualOrigin o, int tick) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        // Keep the sky circle alive but thinner, so the beams read as the focal point
        drawSkyRings(level, o, tick, /*density*/ 1);

        // Two randomized converging lightning-style beams per tick
        for (int b = 0; b < 2; b++) {
            drawConvergingBeam(level, o);
        }

        // Snow and dust drawn upward toward the circle, hinting at gathering power
        level.sendParticles(ParticleTypes.SNOWFLAKE, cx, cy + 2.0, cz, 4, 1.0, 1.0, 1.0, 0.06);
        level.sendParticles(ICE_DUST, cx, cy + 1.5, cz, 2, 0.8, 0.8, 0.8, 0.05);

        if (tick % 10 == 0) {
            level.playSound(null, cx, cy, cz, SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 2.0F, 1.2F);
            level.playSound(null, cx, cy, cz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
    }

    /** Draws one jagged, lightning-like beam from a random point on the outer ring down to the caster. */
    private static void drawConvergingBeam(ServerLevel level, RitualOrigin o) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        double randomAngle = level.getRandom().nextDouble() * 2 * Math.PI;
        double r = 8.0;
        double beamX = cx + r * Math.cos(randomAngle);
        double beamZ = cz + r * Math.sin(randomAngle);

        for (double h = 0.0; h <= 1.0; h += 0.08) {
            // Small jitter perpendicular to the beam gives it a jagged "lightning" silhouette
            double jitter = (level.getRandom().nextDouble() - 0.5) * 0.4 * Math.sin(h * Math.PI);
            double px = beamX + (cx - beamX) * h + jitter;
            double py = (cy + 15.0) + ((cy + 1.0) - (cy + 15.0)) * h;
            double pz = beamZ + (cz - beamZ) * h + jitter;

            level.sendParticles(ParticleTypes.PORTAL, px, py, pz, 1, 0.05, 0.05, 0.05, 0.01);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.03, 0.03, 0.03, 0.01);
        }
    }

    // ---- Phase 4: Heaven Beam (10–11s) ------------------------------------

    private static void phaseHeavenBeam(ServerLevel level, RitualOrigin o, int tick) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        for (double h = 0.0; h <= 15.0; h += 0.5) {
            level.sendParticles(ParticleTypes.GLOW, cx, cy + h, cz, 10, 0.5, 0.2, 0.5, 0.02);
            level.sendParticles(ParticleTypes.END_ROD, cx, cy + h, cz, 14, 0.8, 0.2, 0.8, 0.05);
            // Thin bright core of the beam
            level.sendParticles(ICE_DUST, cx, cy + h, cz, 3, 0.15, 0.2, 0.15, 0.01);
        }
        // Faint outward flash ring at ground level, foreshadowing the blast
        level.sendParticles(ParticleTypes.FALLING_LAVA, cx, cy + 0.1, cz, 1, 0.0, 0.0, 0.0, 0.0);

        if (tick == PHASE_ENERGY_END) {
            level.playSound(null, cx, cy, cz, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 3.0F, 0.5F);
            level.playSound(null, cx, cy, cz, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 0.5F);
        }
    }

    // ---- Phase 5: Catastrophic Blast (11s, single instant) ---------------

    private static void phaseCatastrophicBlast(ServerLevel level, Player player, RitualOrigin o) {
        playBlastSounds(level, o);
        spawnBlastParticles(level, o);
        freezeTerrain(level, o);
        freezeAndDamageEntities(level, player, o);
    }

    private static void playBlastSounds(ServerLevel level, RitualOrigin o) {
        double cx = o.x(), cy = o.y(), cz = o.z();
        level.playSound(null, cx, cy, cz, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F, 0.5F);
        level.playSound(null, cx, cy, cz, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 3.0F, 0.5F);
        level.playSound(null, cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 4.0F, 0.5F);
        level.playSound(null, cx, cy, cz, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 0.8F);
    }

    private static void spawnBlastParticles(ServerLevel level, RitualOrigin o) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy + 1.0, cz, 12, 2.0, 2.0, 2.0, 0.1);
        level.sendParticles(ParticleTypes.EXPLOSION, cx, cy + 1.0, cz, 4, 1.5, 1.5, 1.5, 0.05);
        level.sendParticles(ParticleTypes.CLOUD, cx, cy + 1.0, cz, 120, 5.0, 2.0, 5.0, 0.2);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 1.0, cz, 60, 4.0, 1.5, 4.0, 0.1);

        // Spherical shell of frost particles blown outward from the epicenter
        for (int s = 0; s < 260; s++) {
            double angle = level.getRandom().nextDouble() * 2 * Math.PI;
            double pitch = (level.getRandom().nextDouble() - 0.5) * Math.PI;
            double r = 5.0 + level.getRandom().nextDouble() * 10.0;
            double px = cx + r * Math.cos(angle) * Math.cos(pitch);
            double py = cy + 1.0 + r * Math.sin(pitch);
            double pz = cz + r * Math.sin(angle) * Math.cos(pitch);

            level.sendParticles(ParticleTypes.SNOWFLAKE, px, py, pz, 2, 0.1, 0.1, 0.1, 0.05);
            if (s % 3 == 0) {
                level.sendParticles(ParticleTypes.WHITE_ASH, px, py, pz, 1, 0.1, 0.1, 0.1, 0.02);
            }
            if (s % 5 == 0) {
                level.sendParticles(ParticleTypes.SOUL, px, py, pz, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Bright flash rings expanding along the ground to sell the shockwave
        for (int ring = 1; ring <= 3; ring++) {
            double radius = ring * 5.0;
            int points = 24 * ring;
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i;
                double px = cx + radius * Math.cos(angle);
                double pz = cz + radius * Math.sin(angle);
                level.sendParticles(ParticleTypes.FALLING_LAVA, px, cy + 0.1, pz, 1, 0.0, 0.0, 0.0, 0.0);
                level.sendParticles(ParticleTypes.SNOWFLAKE, px, cy + 0.2, pz, 1, 0.2, 0.1, 0.2, 0.02);
            }
        }
    }

    /** Converts water/lava/air/ground within blast radius into ice-themed blocks. */
    private static void freezeTerrain(ServerLevel level, RitualOrigin o) {
        BlockPos centerPos = new BlockPos((int) Math.floor(o.x()), (int) Math.floor(o.y()), (int) Math.floor(o.z()));

        for (int dx = -BLAST_RADIUS; dx <= BLAST_RADIUS; dx++) {
            for (int dy = -BLAST_RADIUS; dy <= BLAST_RADIUS; dy++) {
                for (int dz = -BLAST_RADIUS; dz <= BLAST_RADIUS; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > (double) BLAST_RADIUS * BLAST_RADIUS) {
                        continue;
                    }
                    freezeSingleBlock(level, centerPos.offset(dx, dy, dz));
                }
            }
        }
    }

    private static void freezeSingleBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
        } else if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
        } else if (state.isAir() && level.getBlockState(pos.below()).isSolid()) {
            level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
        } else if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
            double rand = level.getRandom().nextDouble();
            if (rand < 0.1) {
                level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            } else if (rand < 0.2) {
                level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            } else if (rand < 0.35) {
                level.setBlockAndUpdate(pos, Blocks.SNOW_BLOCK.defaultBlockState());
            } else if (rand < 0.45) {
                level.setBlockAndUpdate(pos, Blocks.POWDER_SNOW.defaultBlockState());
            }
        }
    }

    private static void freezeAndDamageEntities(ServerLevel level, Player caster, RitualOrigin o) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        AABB aabb = new AABB(cx - BLAST_RADIUS, cy - BLAST_RADIUS, cz - BLAST_RADIUS,
                cx + BLAST_RADIUS, cy + BLAST_RADIUS, cz + BLAST_RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != caster);

        for (LivingEntity target : targets) {
            if (target.distanceToSqr(cx, cy, cz) > (double) BLAST_RADIUS * BLAST_RADIUS) {
                continue;
            }

            target.setTicksFrozen(FREEZE_TICKS);
            target.hurt(level.damageSources().magic(), BLAST_DAMAGE);

            // Small personal frost burst so each frozen target reads clearly amid the chaos
            level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(),
                    20, 0.4, 0.6, 0.4, 0.02);
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 1.0, target.getZ(),
                    10, 0.3, 0.5, 0.3, 0.02);

            if (target.isAlive()) {
                encaseInIce(level, target.blockPosition());
                encaseInIce(level, target.blockPosition().above());
            }
        }
    }

    private static void encaseInIce(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).isAir()) {
            level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
        }
    }

    // ---- Phase 6: Aftermath (11–15s) --------------------------------------

    private static void phaseAftermath(ServerLevel level, RitualOrigin o, int tick) {
        double cx = o.x(), cy = o.y(), cz = o.z();

        // Gentle residual snowfall settling over the frozen ground
        level.sendParticles(ParticleTypes.SNOWFLAKE, cx, cy + 5.0, cz, 12, 8.0, 4.0, 8.0, 0.05);
        level.sendParticles(ParticleTypes.WHITE_ASH, cx, cy + 5.0, cz, 6, 8.0, 4.0, 8.0, 0.02);
        level.sendParticles(ParticleTypes.CLOUD, cx, cy + 3.0, cz, 3, 6.0, 1.0, 6.0, 0.01);

        // Chloe's spirit: a slow rising, swirling light around the caster
        double angle = tick * 0.3;
        double spiritY = cy + ((tick - PHASE_BEAM_END) * 0.05);
        double sx = cx + 0.8 * Math.cos(angle);
        double sz = cz + 0.8 * Math.sin(angle);
        level.sendParticles(ParticleTypes.END_ROD, sx, spiritY, sz, 1, 0.01, 0.01, 0.01, 0.01);
        if (tick % 5 == 0) {
            level.sendParticles(ParticleTypes.GLOW, sx, spiritY, sz, 2, 0.1, 0.15, 0.1, 0.01);
        }

        if (tick % 15 == 0) {
            level.playSound(null, cx, cy, cz, SoundEvents.WEATHER_RAIN, SoundSource.PLAYERS, 0.5F, 0.5F);
        }
    }
}