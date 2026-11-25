package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

import java.util.Optional;

/** server-side search that only reads loaded chunks */
public final class QiCompassSearch {
    private QiCompassSearch() {}

    public static Optional<ChunkPos> findRichestChunk(ServerLevel level, Player player, int radius) {
        ChunkPos origin = player.chunkPosition();
        ChunkPos best = null;
        long bestQi = 0;
        double bestDistance = Double.POSITIVE_INFINITY;
        int radiusSquared = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSquared) continue;
                int x = origin.x() + dx;
                int z = origin.z() + dz;
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null || !chunk.hasData(AscensionAttachments.CHUNK_QI_HANDLER)) continue;

                ChunkQiHandler handler = ChunkHelper.getQiHandler(chunk);
                long qi = Math.min(handler.getQi(), handler.getCapacity());
                if (qi <= 0 || qi < bestQi) continue;

                double distanceX = x * 16.0 + 8.5 - player.getX();
                double distanceZ = z * 16.0 + 8.5 - player.getZ();
                double distance = distanceX * distanceX + distanceZ * distanceZ;
                if (qi > bestQi || distance < bestDistance) {
                    best = chunk.getPos();
                    bestQi = qi;
                    bestDistance = distance;
                }
            }
        }
        return Optional.ofNullable(best);
    }
}
