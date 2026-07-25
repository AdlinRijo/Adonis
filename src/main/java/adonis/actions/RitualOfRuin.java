package adonis.actions;

import net.minecraft.core.BlockPos;
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

public final class RitualOfRuin {
    private RitualOfRuin() {
    }

    public static InteractionResult cast(Level level, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        // Start the ritual on a separate thread to prevent blocking the main server thread during sleeps.
        new Thread(() -> {
            try {
                for (int iteration = 0; iteration < 150; iteration++) {
                    final int currentIteration = iteration;

                    // If the player is removed or no longer alive, abort the ritual
                    if (player.isRemoved() || !player.isAlive()) {
                        break;
                    }

                    // Schedule execution of Minecraft world changes on the main server thread
                    serverLevel.getServer().execute(() -> {
                        // 1. Channeling Phase (0 to 3 seconds)
                        if (currentIteration < 30) {
                            serverLevel.sendParticles(ParticleTypes.SOUL, cx, cy + 0.5, cz, 5, 0.5, 0.5, 0.5, 0.05);
                            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy + 1.0, cz, 3, 0.3, 0.5, 0.3, 0.02);
                            serverLevel.sendParticles(ParticleTypes.SMOKE, cx, cy + 0.2, cz, 8, 0.8, 0.2, 0.8, 0.05);

                            if (currentIteration % 10 == 0) {
                                serverLevel.playSound(null, cx, cy, cz, SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.0F, 0.5F);
                                serverLevel.playSound(null, cx, cy, cz, SoundEvents.AMBIENT_CAVE, SoundSource.PLAYERS, 1.5F, 0.8F);
                            }
                        }
                        // 2. Sky Circle Formation Phase (3 to 6 seconds)
                        else if (currentIteration < 60) {
                            double angleOffset = currentIteration * 0.15;
                            // Outer rotating circle (radius 8.0)
                            for (int i = 0; i < 16; i++) {
                                double angle = angleOffset + (i * Math.PI / 8);
                                double xOffset = 8.0 * Math.cos(angle);
                                double zOffset = 8.0 * Math.sin(angle);
                                serverLevel.sendParticles(ParticleTypes.END_ROD, cx + xOffset, cy + 15.0, cz + zOffset, 2, 0.1, 0.1, 0.1, 0.01);
                                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx + xOffset, cy + 15.0, cz + zOffset, 1, 0.1, 0.1, 0.1, 0.01);
                            }

                            // Inner rotating circle (radius 4.0, opposite direction)
                            double innerAngleOffset = -currentIteration * 0.2;
                            for (int i = 0; i < 8; i++) {
                                double angle = innerAngleOffset + (i * Math.PI / 4);
                                double xOffset = 4.0 * Math.cos(angle);
                                double zOffset = 4.0 * Math.sin(angle);
                                serverLevel.sendParticles(ParticleTypes.GLOW, cx + xOffset, cy + 15.0, cz + zOffset, 2, 0.1, 0.1, 0.1, 0.01);
                            }

                            if (currentIteration == 30) {
                                serverLevel.playSound(null, cx, cy + 15.0, cz, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 3.0F, 0.8F);
                            }
                        }
                        // 3. Energy Gathering Phase (6 to 10 seconds)
                        else if (currentIteration < 100) {
                            // Maintain sky circles
                            double angleOffset = currentIteration * 0.15;
                            for (int i = 0; i < 16; i++) {
                                double angle = angleOffset + (i * Math.PI / 8);
                                double xOffset = 8.0 * Math.cos(angle);
                                double zOffset = 8.0 * Math.sin(angle);
                                serverLevel.sendParticles(ParticleTypes.END_ROD, cx + xOffset, cy + 15.0, cz + zOffset, 1, 0.1, 0.1, 0.1, 0.01);
                            }

                            // Draw converging beams
                            for (int b = 0; b < 2; b++) {
                                double randomAngle = serverLevel.getRandom().nextDouble() * 2 * Math.PI;
                                double r = 8.0;
                                double beamX = cx + r * Math.cos(randomAngle);
                                double beamZ = cz + r * Math.sin(randomAngle);

                                for (double h = 0.0; h <= 1.0; h += 0.1) {
                                    double px = beamX + (cx - beamX) * h;
                                    double py = (cy + 15.0) + ((cy + 1.0) - (cy + 15.0)) * h;
                                    double pz = beamZ + (cz - beamZ) * h;
                                    serverLevel.sendParticles(ParticleTypes.PORTAL, px, py, pz, 1, 0.05, 0.05, 0.05, 0.01);
                                }
                            }

                            if (currentIteration % 10 == 0) {
                                serverLevel.playSound(null, cx, cy, cz, SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 2.0F, 1.2F);
                                serverLevel.playSound(null, cx, cy, cz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.5F);
                            }
                        }
                        // 4. Heaven Beam Phase (10 to 11 seconds)
                        else if (currentIteration < 110) {
                            for (double h = 0.0; h <= 15.0; h += 0.5) {
                                serverLevel.sendParticles(ParticleTypes.GLOW, cx, cy + h, cz, 8, 0.5, 0.2, 0.5, 0.02);
                                serverLevel.sendParticles(ParticleTypes.END_ROD, cx, cy + h, cz, 12, 0.8, 0.2, 0.8, 0.05);
                            }

                            if (currentIteration == 100) {
                                serverLevel.playSound(null, cx, cy, cz, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 3.0F, 0.5F);
                                serverLevel.playSound(null, cx, cy, cz, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 0.5F);
                            }
                        }
                        // 5. Catastrophic Blast (THE RUIN!) (11.0 seconds)
                        else if (currentIteration == 110) {
                            serverLevel.playSound(null, cx, cy, cz, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F, 0.5F);
                            serverLevel.playSound(null, cx, cy, cz, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 3.0F, 0.5F);
                            serverLevel.playSound(null, cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 4.0F, 0.5F);
                            serverLevel.playSound(null, cx, cy, cz, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 0.8F);

                            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy + 1.0, cz, 10, 2.0, 2.0, 2.0, 0.1);
                            serverLevel.sendParticles(ParticleTypes.CLOUD, cx, cy + 1.0, cz, 100, 5.0, 2.0, 5.0, 0.2);

                            // Sphere particle blast
                            for (int s = 0; s < 200; s++) {
                                double angle = serverLevel.getRandom().nextDouble() * 2 * Math.PI;
                                double pitch = (serverLevel.getRandom().nextDouble() - 0.5) * Math.PI;
                                double r = 5.0 + serverLevel.getRandom().nextDouble() * 10.0;
                                double px = cx + r * Math.cos(angle) * Math.cos(pitch);
                                double py = cy + 1.0 + r * Math.sin(pitch);
                                double pz = cz + r * Math.sin(angle) * Math.cos(pitch);
                                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, px, py, pz, 2, 0.1, 0.1, 0.1, 0.05);
                            }

                            // Block Freezing
                            int radius = 15;
                            BlockPos centerPos = new BlockPos((int) Math.floor(cx), (int) Math.floor(cy), (int) Math.floor(cz));
                            for (int dx = -radius; dx <= radius; dx++) {
                                for (int dy = -radius; dy <= radius; dy++) {
                                    for (int dz = -radius; dz <= radius; dz++) {
                                        double distSq = dx * dx + dy * dy + dz * dz;
                                        if (distSq <= radius * radius) {
                                            BlockPos pos = centerPos.offset(dx, dy, dz);
                                            BlockState state = serverLevel.getBlockState(pos);

                                            if (state.is(Blocks.WATER)) {
                                                serverLevel.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                                            } else if (state.is(Blocks.LAVA)) {
                                                serverLevel.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                                            } else if (state.isAir() && serverLevel.getBlockState(pos.below()).isSolid()) {
                                                serverLevel.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
                                            } else if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
                                                double rand = serverLevel.getRandom().nextDouble();
                                                if (rand < 0.1) {
                                                    serverLevel.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
                                                } else if (rand < 0.2) {
                                                    serverLevel.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
                                                } else if (rand < 0.35) {
                                                    serverLevel.setBlockAndUpdate(pos, Blocks.SNOW_BLOCK.defaultBlockState());
                                                } else if (rand < 0.45) {
                                                    serverLevel.setBlockAndUpdate(pos, Blocks.POWDER_SNOW.defaultBlockState());
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Entity damage and freezing
                            AABB aabb = new AABB(cx - radius, cy - radius, cz - radius, cx + radius, cy + radius, cz + radius);
                            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != player);
                            for (LivingEntity target : targets) {
                                if (target.distanceToSqr(cx, cy, cz) <= radius * radius) {
                                    target.setTicksFrozen(400);
                                    target.hurt(serverLevel.damageSources().magic(), 40.0F);

                                    if (target.isAlive()) {
                                        BlockPos targetPos = target.blockPosition();
                                        if (serverLevel.getBlockState(targetPos).isAir()) {
                                            serverLevel.setBlockAndUpdate(targetPos, Blocks.ICE.defaultBlockState());
                                        }
                                        BlockPos headPos = targetPos.above();
                                        if (serverLevel.getBlockState(headPos).isAir()) {
                                            serverLevel.setBlockAndUpdate(headPos, Blocks.ICE.defaultBlockState());
                                        }
                                    }
                                }
                            }
                        }
                        // 6. Aftermath Phase (11 to 15 seconds)
                        else {
                            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, cx, cy + 5.0, cz, 10, 8.0, 4.0, 8.0, 0.05);
                            serverLevel.sendParticles(ParticleTypes.WHITE_ASH, cx, cy + 5.0, cz, 5, 8.0, 4.0, 8.0, 0.02);

                            // Chloe's spirit: swirling soft light around caster
                            double angle = currentIteration * 0.3;
                            double spiritY = cy + ((currentIteration - 110) * 0.05);
                            double sx = cx + 0.8 * Math.cos(angle);
                            double sz = cz + 0.8 * Math.sin(angle);
                            serverLevel.sendParticles(ParticleTypes.END_ROD, sx, spiritY, sz, 1, 0.01, 0.01, 0.01, 0.01);

                            if (currentIteration % 15 == 0) {
                                serverLevel.playSound(null, cx, cy, cz, SoundEvents.WEATHER_RAIN, SoundSource.PLAYERS, 0.5F, 0.5F);
                            }
                        }
                    });

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return InteractionResult.SUCCESS;
    }
}
