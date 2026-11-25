package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
//TODO
public class ChunkHelper {
    public static ChunkQiHandler getQiHandler(ChunkAccess chunkAccess){
        return chunkAccess.getData(AscensionAttachments.CHUNK_QI_HANDLER);
    }

    /**
     * Draws whole units of ambient qi from the caster's currently loaded chunk.
     * Fractional demand is rounded probabilistically so its long-run cost and
     * base cultivation gain are unchanged without adding fractional qi storage.
     * The caller must use the returned amount, not the original requested rate.
     */
    public static int extractCultivationQi(LivingEntity caster, double requested) {
        if (!(caster.level() instanceof ServerLevel level)
                || !Double.isFinite(requested) || requested <= 0.0D) return 0;

        LevelChunk chunk = level.getChunkSource().getChunkNow(caster.chunkPosition().x(), caster.chunkPosition().z());
        if (chunk == null || !chunk.hasData(AscensionAttachments.CHUNK_QI_HANDLER)) return 0;

        ChunkQiHandler handler = getQiHandler(chunk);
        long available = Math.min(handler.getQi(), handler.getCapacity());
        if (available <= 0L) return 0;

        double bounded = Math.min(requested, Math.min(available, Integer.MAX_VALUE));
        int demand = (int) bounded;
        double fraction = bounded - demand;
        if (fraction > 0.0D && level.getRandom().nextDouble() < fraction) demand++;
        if (demand == 0) return 0;

        int extracted = handler.extractQi(demand);
        if (extracted > 0) chunk.markUnsaved();
        return extracted;
    }

    public static ChunkPathAffinityProvider getAffinityProvider(ChunkAccess chunkAccess){
        return chunkAccess.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER);
    }


}
