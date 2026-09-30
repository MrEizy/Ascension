package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.zic.ascension.AscensionCraft;

/**
 * Constants + geometry for the shared "Inner World" dimension. One void dimension, one plot per
 * player, plots laid out on a spiral grid so no two plots can ever be closer than MIN_GAP_BLOCKS.
 */
public final class InnerWorld {
    private InnerWorld() {
    }

    public static final Identifier SKILL_ID = AscensionCraft.prefix("castable/inner_world");
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, AscensionCraft.prefix("inner_world"));

    /** Distance between plot CENTERS. Must be a multiple of 16. */
    public static final int PLOT_SPACING_BLOCKS = 8192;
    /** Radius (in chunks) of a brand-new inner world. */
    public static final int BASE_RADIUS_CHUNKS = 4;
    /** Extra radius (in chunks) gained per major realm. */
    public static final int CHUNKS_PER_REALM = 6;
    /** Hard cap so plots can never grow into each other. */
    public static final int MAX_RADIUS_CHUNKS = 96;
    /** Guaranteed minimum void between two plots' borders. */
    public static final int MIN_GAP_BLOCKS = 5000;
    public static final int SURFACE_Y = 64;
    /** Falling below this in the inner world rescues you to the spawn dais. */
    public static final int VOID_RESCUE_Y = 0;

    static {
        int worstCaseHalfSpan = MAX_RADIUS_CHUNKS * 16 + 16;
        if (PLOT_SPACING_BLOCKS % 16 != 0 || PLOT_SPACING_BLOCKS - 2 * worstCaseHalfSpan < MIN_GAP_BLOCKS) {
            throw new IllegalStateException("Inner World plot spacing/radius violates the minimum gap");
        }
    }

    public record Bounds(double minX, double maxX, double minZ, double maxZ) {
    }

    public static int radiusChunksForTier(int tier) {
        return Math.min(MAX_RADIUS_CHUNKS, BASE_RADIUS_CHUNKS + Math.max(0, tier) * CHUNKS_PER_REALM);
    }

    /** Ulam-spiral: plot 0 at the origin, then rings outward. */
    public static int[] plotGrid(int index) {
        if (index <= 0) {
            return new int[]{0, 0};
        }
        int k = (int) Math.ceil((Math.sqrt(index + 1.0) - 1.0) / 2.0);
        int t = 2 * k;
        int m = (t + 1) * (t + 1) - 1;
        if (index >= m - t) {
            return new int[]{k - (m - index), -k};
        }
        m -= t;
        if (index >= m - t) {
            return new int[]{-k, -k + (m - index)};
        }
        m -= t;
        if (index >= m - t) {
            return new int[]{-k + (m - index), k};
        }
        return new int[]{k, k - (m - index - t)};
    }

    public static int[] plotCenterChunk(int index) {
        int[] grid = plotGrid(index);
        int step = PLOT_SPACING_BLOCKS / 16;
        return new int[]{grid[0] * step, grid[1] * step};
    }

    public static BlockPos plotCenter(int index) {
        int[] chunk = plotCenterChunk(index);
        return new BlockPos(chunk[0] * 16 + 8, SURFACE_Y, chunk[1] * 16 + 8);
    }

    public static Bounds bounds(int index, int radiusChunks) {
        int[] chunk = plotCenterChunk(index);
        return new Bounds(
                (chunk[0] - radiusChunks) * 16.0,
                (chunk[0] + radiusChunks + 1) * 16.0,
                (chunk[1] - radiusChunks) * 16.0,
                (chunk[1] + radiusChunks + 1) * 16.0
        );
    }

    /**
     * TODO: return the player's current MAJOR REALM (0 = first realm). This is the ONE integration
     * point I couldn't fill in without your realm accessor. Until it's wired the inner world stays at
     * tier 0 (or use /innerworld tier <player> <n>, or call raiseTier() from your realm-up event).
     */
    public static int majorRealm(ServerPlayer player) {
        return 0;
    }

    /** Raise (never lower) a player's tier and queue the new rings. Safe to call from a realm-up event. */
    public static void raiseTier(ServerPlayer player, int tier) {
        MinecraftServer server = player.level().getServer();
        InnerWorldPlots.Plot plot = InnerWorldPlots.getOrCreate(player.getUUID());
        if (tier > plot.tier) {
            plot.tier = tier;
            InnerWorldPlots.save();
            player.sendSystemMessage(Component.translatable("ascension.inner_world.expanding"));
        }
        InnerWorldGrowth.ensureQueued(server, player.getUUID(), plot);
    }
}