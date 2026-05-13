package net.thejadeproject.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.custom.SpiritVeinBlock;
import net.thejadeproject.ascension.common.blocks.custom.SpiritualStoneClusterBlock;
import org.jetbrains.annotations.Nullable;

public class SpiritVeinBlockEntity extends BlockEntity {
    private int cycleTimer = 0;
    private static final int FULL_CYCLE = 6000; // 5 minutes = 6000 ticks
    private static final int VEIN_INTERVAL = FULL_CYCLE / SpiritVeinBlock.MAX_VEIN;
    private boolean hasAnnounced = false;
    private static final int ANNOUNCE_RANGE = 10;
    private static final int ANNOUNCE_CHECK_INTERVAL = 100;

    private static final int BOSS_BAR_DURATION = 200;
    @Nullable
    private ServerBossEvent bossBar;
    private int bossBarTicksRemaining;

    public SpiritVeinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPIRIT_VEIN_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpiritVeinBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.cycleTimer++;
        blockEntity.updateBossBar();

        if (!blockEntity.hasAnnounced && blockEntity.cycleTimer % ANNOUNCE_CHECK_INTERVAL == 0) {
            blockEntity.checkAndAnnounceNearbyPlayers((ServerLevel) level, pos);
        }

        int currentVein = state.getValue(SpiritVeinBlock.VEIN_LEVEL);

        if (blockEntity.cycleTimer >= VEIN_INTERVAL && currentVein < SpiritVeinBlock.MAX_VEIN) {
            int newVein = Math.min(SpiritVeinBlock.MAX_VEIN, currentVein + 1);
            level.setBlock(pos, state.setValue(SpiritVeinBlock.VEIN_LEVEL, newVein), 3);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        8 + newVein * 4, 0.3, 0.3, 0.3, 0.02);

                if (newVein == SpiritVeinBlock.MAX_VEIN) {
                    serverLevel.playSound(null, pos, SoundEvents.BEACON_ACTIVATE,
                            SoundSource.BLOCKS, 0.5f, 1.5f);
                }
            }

            blockEntity.setChanged();
        }

        if (blockEntity.cycleTimer >= FULL_CYCLE) {
            blockEntity.attemptClusterSpawn(state, (ServerLevel) level, pos);
            blockEntity.cycleTimer = 0;
            blockEntity.setChanged();
        }
    }

    private void updateBossBar() {
        if (bossBar != null) {
            bossBarTicksRemaining--;
            float progress = (float) bossBarTicksRemaining / BOSS_BAR_DURATION;
            bossBar.setProgress(Math.max(0.0f, progress));

            if (bossBarTicksRemaining <= 0) {
                bossBar.removeAllPlayers();
                bossBar = null;
            }
        }
    }

    private void checkAndAnnounceNearbyPlayers(ServerLevel level, BlockPos pos) {
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : level.players()) {
            if (player instanceof ServerPlayer) {
                double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());

                if (distance <= ANNOUNCE_RANGE * ANNOUNCE_RANGE) {
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestPlayer = player;
                    }
                }
            }
        }

        if (nearestPlayer != null && !hasAnnounced) {
            String coordinates = String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
            String playerName = nearestPlayer.getDisplayName().getString();

            createBossBar(level, coordinates, playerName);

            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT,
                    SoundSource.BLOCKS, 0.7f, 1.2f);

            hasAnnounced = true;
            setChanged();
        }
    }

    private void createBossBar(ServerLevel level, String coordinates, String playerName) {
        Component name = Component.literal("§d§lSpirit Vein detected at " + coordinates +
                " - §e" + playerName + "§d is closest!");

        bossBar = new ServerBossEvent(name, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        bossBar.setProgress(1.0f);
        bossBarTicksRemaining = BOSS_BAR_DURATION;

        for (Player player : level.players()) {
            if (player instanceof ServerPlayer serverPlayer) {
                bossBar.addPlayer(serverPlayer);
            }
        }
    }

    private void attemptClusterSpawn(BlockState state, ServerLevel level, BlockPos pos) {
        int currentVein = state.getValue(SpiritVeinBlock.VEIN_LEVEL);

        if (currentVein < SpiritVeinBlock.MAX_VEIN) {
            return;
        }

        boolean hasSpawned = false;
        RandomSource random = level.random;

        // 50% chance per side (all 6 directions now)
        for (Direction direction : Direction.values()) {
            if (random.nextFloat() <= 0.50f) { // 50% chance
                BlockPos targetPos = pos.relative(direction);

                if (canSpawnClusterAt(level, targetPos)) {
                    Block clusterBlock = ModBlocks.SPIRITUAL_STONE_CLUSTER.get();
                    BlockState clusterState = clusterBlock.defaultBlockState()
                            .setValue(SpiritualStoneClusterBlock.FACING, direction);

                    level.setBlock(targetPos, clusterState, 3);

                    hasSpawned = true;

                    spawnClusterEffect(level, pos, targetPos);
                }
            }
        }

        if (hasSpawned) {
            level.setBlock(pos, state.setValue(SpiritVeinBlock.VEIN_LEVEL, 0), 3);

            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 1.0f, 0.8f);

            for (int i = 0; i < 8; i++) {
                double angle = (i / 8.0) * Math.PI * 2;
                double distance = 1.5;
                double dx = Math.cos(angle) * distance;
                double dz = Math.sin(angle) * distance;

                level.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        0, dx, 0, dz, 0.3);
            }
        } else {
            level.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.1, 0.2, 0.02);
        }
    }

    private boolean canSpawnClusterAt(ServerLevel level, BlockPos pos) {
        BlockState targetState = level.getBlockState(pos);
        return targetState.canBeReplaced();
    }

    private void spawnClusterEffect(ServerLevel level, BlockPos source, BlockPos target) {
        int particles = 15;
        for (int i = 0; i <= particles; i++) {
            double progress = i / (double) particles;
            double x = source.getX() + 0.5 + (target.getX() - source.getX()) * progress;
            double y = source.getY() + 0.5 + (target.getY() - source.getY()) * progress;
            double z = source.getZ() + 0.5 + (target.getZ() - source.getZ()) * progress;

            level.sendParticles(ParticleTypes.DRIPPING_WATER,
                    x, y, z, 1, 0, 0, 0, 0.01);
        }

        level.sendParticles(ParticleTypes.FLASH,
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                1, 0, 0, 0, 0);

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                10, 0.3, 0.1, 0.3, 0.02);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CycleTimer", cycleTimer);
        tag.putBoolean("HasAnnounced", hasAnnounced);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (bossBar != null) {
            bossBar.removeAllPlayers();
            bossBar = null;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CycleTimer")) {
            cycleTimer = tag.getInt("CycleTimer");
        }
        if (tag.contains("HasAnnounced")) {
            hasAnnounced = tag.getBoolean("HasAnnounced");
        }
    }
}