package net.zic.ascension.events.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SpatialRuptureAPI {

    private static final TicketType<UUID> TELEPORT_TICKET = TicketType.create("spatial_rupture", UUID::compareTo, 10);

    /* --------------------------------------------------------------------- *
     *  PUBLIC ENTRY POINT – unchanged signature
     * --------------------------------------------------------------------- */
    public static CompletableFuture<Boolean> randomTeleport(ServerPlayer player, ServerLevel world, int radius) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BlockPos safePos = findSafeLocationInRadius(world, player, player.blockPosition(), radius);
                if (safePos != null) {
                    world.getServer().execute(() -> teleportPlayer(player, world, safePos));
                    return true;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    /* --------------------------------------------------------------------- *
     *  MAIN SEARCH – uses UUID tickets
     * --------------------------------------------------------------------- */
    private static BlockPos findSafeLocationInRadius(ServerLevel level, ServerPlayer player,
                                                     BlockPos center, int radius) {
        Random random = new Random();
        WorldBorder border = level.getWorldBorder();
        UUID owner = player.getUUID();

        // Tier 1 – 90-100 % radius (40 tries)
        BlockPos result = tryDistanceBand(level, center, radius * 0.9, radius, border, random, 40, owner);
        if (result != null) return result;

        // Tier 2 – 70-90 % (30 tries)
        result = tryDistanceBand(level, center, radius * 0.7, radius * 0.9, border, random, 30, owner);
        if (result != null) return result;

        // Tier 3 – 50-70 % (20 tries)
        result = tryDistanceBand(level, center, radius * 0.5, radius * 0.7, border, random, 20, owner);
        if (result != null) return result;

        // Tier 4 – uniform fallback 0-50 % (50 tries)
        for (int i = 0; i < 50; i++) {
            int x = center.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = center.getZ() + random.nextInt(radius * 2 + 1) - radius;
            if (!border.isWithinBounds(x, z)) continue;

            BlockPos surface = getSurfacePos(level, x, z, owner);
            if (surface == null || surface.getY() <= level.getMinBuildHeight() + 10) continue;

            BlockPos safe = findSafeGroundPosition(level, surface);
            if (safe != null) return safe;
        }
        return null;
    }

    /* --------------------------------------------------------------------- *
     *  ONE-CHUNK LOADER – waits for the chunk to be fully loaded
     * --------------------------------------------------------------------- */
    private static BlockPos getSurfacePos(ServerLevel level, int x, int z, UUID owner) {
        ChunkPos cp = new ChunkPos(x >> 4, z >> 4);
        level.getChunkSource().addRegionTicket(TELEPORT_TICKET, cp, 1, owner);

        // Wait up to 200 ms for the chunk to become accessible
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(200);
        while (System.nanoTime() < deadline) {
            if (level.hasChunk(cp.x, cp.z)) {
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        new BlockPos(x, 0, z));
                level.getChunkSource().removeRegionTicket(TELEPORT_TICKET, cp, 1, owner);
                return surface;
            }
            try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        }
        // Timed-out – chunk never loaded, remove ticket
        level.getChunkSource().removeRegionTicket(TELEPORT_TICKET, cp, 1, owner);
        return null;
    }

    /* --------------------------------------------------------------------- *
     *  BAND SAMPLER – uses the helper above
     * --------------------------------------------------------------------- */
    private static BlockPos tryDistanceBand(ServerLevel level, BlockPos center,
                                            double minDist, double maxDist,
                                            WorldBorder border, Random random,
                                            int attempts, UUID owner) {
        for (int i = 0; i < attempts; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            int x = (int) (center.getX() + dist * Math.cos(angle));
            int z = (int) (center.getZ() + dist * Math.sin(angle));

            if (!border.isWithinBounds(x, z)) continue;

            BlockPos surface = getSurfacePos(level, x, z, owner);
            if (surface == null || surface.getY() <= level.getMinBuildHeight() + 10) continue;

            BlockPos safe = findSafeGroundPosition(level, surface);
            if (safe != null) return safe;
        }
        return null;
    }

    /* --------------------------------------------------------------------- *
     *  SAFETY CHECKS – permissive (feel free to tighten)
     * --------------------------------------------------------------------- */
    private static BlockPos findSafeGroundPosition(ServerLevel level, BlockPos surfacePos) {
        int startY = Math.max(surfacePos.getY(), level.getMinBuildHeight() + 10);
        int endY   = Math.min(startY + 20, level.getMaxBuildHeight() - 3);

        for (int y = endY; y >= startY - 10; y--) {
            BlockPos ground = new BlockPos(surfacePos.getX(), y - 1, surfacePos.getZ());
            BlockPos feet   = ground.above();
            BlockPos head   = feet.above();

            if (isSolidGround(level, ground) && isAirOrWater(level, feet) && isAirOrWater(level, head)) {
                return feet;
            }
        }
        return null;
    }

    private static boolean isSolidGround(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty()
                && state.isFaceSturdy(level, pos, Direction.UP);
    }

    private static boolean isAirOrWater(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isAir()
                || state.getFluidState().isEmpty()
                || state.is(net.minecraft.world.level.block.Blocks.WATER);
    }

    /* --------------------------------------------------------------------- *
     *  TELEPORT – unchanged
     * --------------------------------------------------------------------- */
    private static void teleportPlayer(ServerPlayer player, ServerLevel level, BlockPos targetPos) {
        float yRot = player.getYRot();
        float xRot = player.getXRot();

        player.teleportTo(level,
                targetPos.getX() + 0.5,
                targetPos.getY(),
                targetPos.getZ() + 0.5,
                yRot, xRot);

        player.connection.resetPosition();
        player.setOnGround(true);
        player.hurtMarked = true;
        player.fallDistance = 0;
    }
}