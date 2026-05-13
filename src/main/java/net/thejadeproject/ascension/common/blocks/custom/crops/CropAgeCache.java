package net.thejadeproject.ascension.common.blocks.custom.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Lightweight in-memory cache mapping (ServerLevel, BlockPos) → (grownSince tick, quality).
 *
 * ── Why this instead of a BlockEntity? ───────────────────────────────────
 * CropBlock has no BlockEntity. Adding one would require touching the block
 * class heavily and registering a new BE type. This cache stores the data
 * for fully-grown crops in memory — zero disk/NBT overhead.
 *
 * ── Persistence across chunk unload ──────────────────────────────────────
 * The cache is NOT persisted. If a chunk unloads and reloads before the crop
 * is harvested, the crop appears fully grown but has no age data in cache —
 * harvest drops will have ageTicks = 0 (treated as Young, quality re-rolled
 * is Basic as fallback). This is an acceptable tradeoff: the player only loses
 * the age bonus if they unload the chunk, not the crop itself.
 *
 * Alternative persistent approach (not used here):
 *   Store grownSince as a BlockEntity long. More robust but more overhead.
 *   Implement later if persistence becomes required.
 *
 * ── Cleanup ───────────────────────────────────────────────────────────────
 * WeakHashMap on the outer level means ServerLevel instances that are no
 * longer referenced (e.g. after world unload) are garbage collected along
 * with their data automatically.
 */
public class CropAgeCache {

    private static final WeakHashMap<ServerLevel, Map<BlockPos, CropData>> CACHE
            = new WeakHashMap<>();

    public static void store(ServerLevel level, BlockPos pos, long grownSince, int quality) {
        CACHE.computeIfAbsent(level, k -> new HashMap<>())
                .put(pos.immutable(), new CropData(grownSince, quality));
    }

    public static CropData retrieve(ServerLevel level, BlockPos pos) {
        Map<BlockPos, CropData> levelMap = CACHE.get(level);
        if (levelMap == null) return null;
        return levelMap.get(pos.immutable());
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        Map<BlockPos, CropData> levelMap = CACHE.get(level);
        if (levelMap != null) levelMap.remove(pos.immutable());
    }

    public record CropData(long grownSince, int quality) {}
}