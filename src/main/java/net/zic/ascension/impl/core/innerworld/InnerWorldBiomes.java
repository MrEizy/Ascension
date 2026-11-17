package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Applies each theme's atmosphere biome (sky, fog, grass/foliage tint, particles, ambience) to a generated chunk.
 * The dimension itself stays flat-void, so this rewrites the per-section biome data after the terrain is placed,
 * one biome per 4x4 column cell (the biome grid's native resolution), the same for the whole height of the column.
 *
 *
 * Qi note: ChunkHandler computes a chunk's qi from its biomes on ChunkEvent.Load, which fires BEFORE we generate
 * (while the chunk is still void). So biome-driven qi catches up the next time the chunk loads from disk.
 */
final class InnerWorldBiomes {
    private InnerWorldBiomes() {
    }

    @SuppressWarnings("unchecked")
    static void apply(ServerLevel level, SeededWorld world, int chunkX, int chunkZ, int centerX, int centerZ) {
        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        var lookup = level.registryAccess().lookupOrThrow(Registries.BIOME);
        Map<Identifier, Holder<Biome>> cache = new HashMap<>();
        LevelChunkSection[] sections = chunk.getSections();
        boolean changed = false;

        for (int qx = 0; qx < 4; qx++) {
            for (int qz = 0; qz < 4; qz++) {
                int lx = chunkX * 16 + qx * 4 + 2 - centerX;
                int lz = chunkZ * 16 + qz * 4 + 2 - centerZ;
                Optional<Identifier> id = world.biomeAt(lx, lz);
                if (id.isEmpty()) {
                    continue;
                }
                Holder<Biome> holder = cache.get(id.get());
                if (holder == null) {
                    holder = lookup.get(ResourceKey.create(Registries.BIOME, id.get())).<Holder<Biome>>map(h -> h).orElse(null);
                    if (holder == null) {
                        continue; // unknown biome id in the profile: leave the void biome rather than fail
                    }
                    cache.put(id.get(), holder);
                }
                for (LevelChunkSection section : sections) {
                    PalettedContainer<Holder<Biome>> container = (PalettedContainer<Holder<Biome>>) section.getBiomes();
                    for (int qy = 0; qy < 4; qy++) {
                        container.set(qx, qy, qz, holder);
                    }
                }
                changed = true;
            }
        }

        if (changed) {
            chunk.markUnsaved();
            level.getChunkSource().chunkMap.resendBiomesForChunks(List.<ChunkAccess>of(chunk));
        }
    }
}