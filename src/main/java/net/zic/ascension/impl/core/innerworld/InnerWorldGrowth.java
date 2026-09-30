package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Grows inner worlds ring by ring, chunk by chunk, spread across ticks. The invisible border only
 * advances when a whole ring has finished, so players never see (or stand on) half-built land.
 * Terrain is a deterministic function of world coordinates, so re-running a ring is harmless.
 */
public final class InnerWorldGrowth {
    private InnerWorldGrowth() {
    }

    private record Job(UUID owner, int cx, int cz, int ring, boolean lastOfRing) {
    }

    private static final int COLUMN_BUDGET_PER_TICK = 128;
    private static final int SET_FLAGS = 18; // update clients + skip neighbour shape updates
    private static final double DAIS_RADIUS = 5.5D;

    private static final ArrayDeque<Job> QUEUE = new ArrayDeque<>();
    private static final Map<UUID, Integer> QUEUED = new HashMap<>();
    private static Job current;
    private static int nextColumn;

    public static void clear() {
        QUEUE.clear();
        QUEUED.clear();
        current = null;
        nextColumn = 0;
    }

    public static void rebuild(MinecraftServer server) {
        clear();
        for (Map.Entry<String, InnerWorldPlots.Plot> entry : InnerWorldPlots.entries().entrySet()) {
            try {
                ensureQueued(server, UUID.fromString(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static boolean isQueued(UUID owner) {
        return QUEUED.containsKey(owner);
    }

    public static void ensureQueued(MinecraftServer server, UUID owner, InnerWorldPlots.Plot plot) {
        int target = InnerWorld.radiusChunksForTier(plot.tier);
        int from = Math.max(plot.generatedRadius, QUEUED.getOrDefault(owner, -1)) + 1;
        if (from > target) {
            QUEUED.putIfAbsent(owner, plot.generatedRadius);
            return;
        }
        int[] center = InnerWorld.plotCenterChunk(plot.index);
        for (int ring = from; ring <= target; ring++) {
            enqueueRing(owner, center[0], center[1], ring);
        }
        QUEUED.put(owner, target);
    }

    private static void enqueueRing(UUID owner, int cx, int cz, int ring) {
        List<int[]> chunks = new ArrayList<>();
        if (ring == 0) {
            chunks.add(new int[]{cx, cz});
        } else {
            for (int dx = -ring; dx <= ring; dx++) {
                chunks.add(new int[]{cx + dx, cz - ring});
                chunks.add(new int[]{cx + dx, cz + ring});
            }
            for (int dz = -ring + 1; dz <= ring - 1; dz++) {
                chunks.add(new int[]{cx - ring, cz + dz});
                chunks.add(new int[]{cx + ring, cz + dz});
            }
        }
        for (int i = 0; i < chunks.size(); i++) {
            int[] c = chunks.get(i);
            QUEUE.add(new Job(owner, c[0], c[1], ring, i == chunks.size() - 1));
        }
    }

    public static void tick(MinecraftServer server) {
        if (QUEUE.isEmpty() && current == null) {
            return;
        }
        ServerLevel level = server.getLevel(InnerWorld.DIMENSION);
        if (level == null) {
            return;
        }
        int budget = COLUMN_BUDGET_PER_TICK;
        while (budget > 0) {
            if (current == null) {
                current = QUEUE.poll();
                nextColumn = 0;
                if (current == null) {
                    return;
                }
            }
            InnerWorldPlots.Plot plot = InnerWorldPlots.get(current.owner());
            if (plot == null) {
                current = null;
                continue;
            }
            long seed = seedFor(plot.index);
            BlockPos center = InnerWorld.plotCenter(plot.index);
            int baseX = current.cx() * 16;
            int baseZ = current.cz() * 16;
            while (budget > 0 && nextColumn < 256) {
                fillColumn(level, seed, baseX + (nextColumn & 15), baseZ + (nextColumn >> 4), center.getX(), center.getZ());
                nextColumn++;
                budget--;
            }
            if (nextColumn >= 256) {
                finish(server, plot, current);
                current = null;
            }
        }
    }

    private static void finish(MinecraftServer server, InnerWorldPlots.Plot plot, Job job) {
        if (!job.lastOfRing()) {
            return;
        }
        plot.generatedRadius = Math.max(plot.generatedRadius, job.ring());
        InnerWorldPlots.save();
        if (plot.generatedRadius >= InnerWorld.radiusChunksForTier(plot.tier)) {
            ServerPlayer owner = server.getPlayerList().getPlayer(job.owner());
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("ascension.inner_world.expanded", plot.generatedRadius));
            }
        }
    }

    private static long seedFor(int plotIndex) {
        return 0x5DEECE66DL * (plotIndex + 1L) ^ 0xA5CE1FL;
    }

    private static void fillColumn(ServerLevel level, long seed, int x, int z, int centerX, int centerZ) {
        double dist = Math.sqrt((double) (x - centerX) * (x - centerX) + (double) (z - centerZ) * (z - centerZ));
        boolean dais = dist <= DAIS_RADIUS;

        double n = noise(seed, x / 48.0, z / 48.0) * 0.6 + noise(seed + 1, x / 20.0, z / 20.0) * 0.3 + noise(seed + 2, x / 7.0, z / 7.0) * 0.1;
        double blend = smoothstep(6.0, 18.0, dist);
        int top = InnerWorld.SURFACE_Y + (int) Math.round((n - 0.5) * 28.0 * blend);
        int thickness = 24 + (int) (noise(seed + 3, x / 30.0, z / 30.0) * 10.0);
        int bottom = top - thickness;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = bottom; y <= top; y++) {
            BlockState state;
            if (y >= top - 3) {
                state = dais ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
                        : (y == top ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState());
            } else if (y <= bottom + 5) {
                state = Blocks.DEEPSLATE.defaultBlockState();
            } else {
                state = Blocks.STONE.defaultBlockState();
            }
            level.setBlock(pos.set(x, y, z), state, SET_FLAGS);
        }

        if (!dais) {
            double roll = hash(seed + 7, x, z);
            Block decor = null;
            if (roll < 0.07) {
                decor = Blocks.SHORT_GRASS;
            } else if (roll < 0.075) {
                decor = Blocks.DANDELION;
            } else if (roll < 0.08) {
                decor = Blocks.POPPY;
            } else if (roll < 0.084) {
                decor = Blocks.AZURE_BLUET;
            }
            if (decor != null) {
                level.setBlock(pos.set(x, top + 1, z), decor.defaultBlockState(), SET_FLAGS);
            }
        }
    }

    private static double smoothstep(double edge0, double edge1, double value) {
        double t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    private static double noise(long seed, double x, double z) {
        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        double u = fx * fx * (3.0 - 2.0 * fx);
        double v = fz * fz * (3.0 - 2.0 * fz);
        double a = hash(seed, x0, z0);
        double b = hash(seed, x0 + 1, z0);
        double c = hash(seed, x0, z0 + 1);
        double d = hash(seed, x0 + 1, z0 + 1);
        return Mth.lerp(v, Mth.lerp(u, a, b), Mth.lerp(u, c, d));
    }

    private static double hash(long seed, int x, int z) {
        long h = seed ^ (x * 341873128712L) ^ (z * 132897987541L);
        h *= 0x9E3779B97F4A7C15L;
        h ^= (h >>> 32);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 29);
        return ((h >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
    }
}