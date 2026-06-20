package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ChunkHandler {

    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
        LevelChunk chunk = event.getChunk();
        ChunkQiContainer chunkQiContainer = chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);

        //TODO load regen rate and base cap from biome

        for(LevelChunkSection section : chunk.getSections()){
            section.getBiomes().getAll(biome->{
                //DO smth here
            });
        }


    }

}
