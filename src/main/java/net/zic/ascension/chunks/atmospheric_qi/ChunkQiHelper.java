package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public class ChunkQiHelper {
    public static ChunkQiHandler getQiHandler(ChunkAccess chunkAccess){
        return chunkAccess.getData(AscensionAttachments.CHUNK_QI_HANDLER);
    }
}
