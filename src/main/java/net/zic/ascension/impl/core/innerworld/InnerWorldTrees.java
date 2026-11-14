package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.zic.ascension.AscensionCraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Plants the trees SeededWorld chose, using vanilla's own configured features (ConfiguredFeature.place), so the shapes, sizes,
 * roots, vines, mushrooms and fungi are exactly vanilla's and nothing here draws a block itself.
 *
 * A vanilla tree writes a big volume over its neighbours, so a chunk is only planted once it and all 8 chunks around it are
 * built (the same "is the terrain really there" probe the fluids use); otherwise canopies would spill over unbuilt void and
 * past the world edge. Every finished chunk re-checks the 3x3 chunks around it, so planting is driven by the growth itself and
 * needs no state on disk: sites are deterministic, and a tree already standing blocks a repeat (vanilla's own space check).
 */
public final class InnerWorldTrees {
    private InnerWorldTrees() {
    }

    private static final Set<String> DONE = new HashSet<>();
    private static final Map<Identifier, ConfiguredFeature<?, ?>> FEATURES = new HashMap<>();
    private static final Set<Identifier> WARNED = new HashSet<>();

    static void clear() {
        DONE.clear();
        FEATURES.clear();
        WARNED.clear();
    }

    /** Call after a seeded chunk finishes (terrain, biomes, herbs). */
    static void onChunkBuilt(ServerLevel level, SeededWorld world, InnerWorldPlots.Plot plot, int chunkX, int chunkZ, BlockPos center) {
        if (!world.hasTrees()) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                tryChunk(level, world, plot, chunkX + dx, chunkZ + dz, center);
            }
        }
    }

    private static void tryChunk(ServerLevel level, SeededWorld world, InnerWorldPlots.Plot plot, int chunkX, int chunkZ, BlockPos center) {
        String key = plot.index + ":" + chunkX + ":" + chunkZ;
        if (DONE.contains(key)) {
            return;
        }
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                int lx = (chunkX + ox) * 16 + 8 - center.getX();
                int lz = (chunkZ + oz) * 16 + 8 - center.getZ();
                if (!world.isColumnBuilt(level, center.getX(), center.getZ(), lx, lz)) {
                    return; // something around it isn't built yet: it will be asked again when that chunk finishes
                }
            }
        }
        DONE.add(key);

        InnerWorldHeightGenerator generator = InnerWorldStructures.generator(level);
        for (SeededWorld.TreeSpot spot : world.treeSpotsInChunk(chunkX, chunkZ, center.getX(), center.getZ())) {
            if (insideStructure(plot, spot.pos())) {
                continue; // a village or ruin stands here: vanilla trees would grow through its paths and roofs
            }
            ConfiguredFeature<?, ?> feature = feature(level, spot.feature());
            if (feature != null) {
                feature.place(level, generator, RandomSource.create(spot.seed()), spot.pos());
            }
        }
    }

    private static boolean insideStructure(InnerWorldPlots.Plot plot, BlockPos pos) {
        for (int[] box : plot.structureBoxes) {
            if (pos.getX() >= box[0] && pos.getX() <= box[2] && pos.getZ() >= box[1] && pos.getZ() <= box[3]) {
                return true;
            }
        }
        return false;
    }

    private static ConfiguredFeature<?, ?> feature(ServerLevel level, Identifier id) {
        ConfiguredFeature<?, ?> cached = FEATURES.get(id);
        if (cached != null) {
            return cached;
        }
        var holder = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(ResourceKey.create(Registries.CONFIGURED_FEATURE, id));
        if (holder.isEmpty()) {
            if (WARNED.add(id)) {
                AscensionCraft.LOGGER.warn("Inner World trees: unknown configured feature '{}' in a profile, skipping it.", id);
            }
            return null;
        }
        ConfiguredFeature<?, ?> value = ((Holder<ConfiguredFeature<?, ?>>) holder.get()).value();
        FEATURES.put(id, value);
        return value;
    }
}