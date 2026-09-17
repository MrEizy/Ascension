package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
//TODO
public class ChunkHelper {
    public static ChunkQiHandler getQiHandler(ChunkAccess chunkAccess){
        return chunkAccess.getData(AscensionAttachments.CHUNK_QI_HANDLER);
    }

    public static ChunkPathAffinityProvider getAffinityProvider(ChunkAccess chunkAccess){
        return chunkAccess.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER);
    }


}
