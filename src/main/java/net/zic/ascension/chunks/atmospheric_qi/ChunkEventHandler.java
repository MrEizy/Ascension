package net.zic.ascension.chunks.atmospheric_qi;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.ConfigurationDataMaps;
import net.zic.ascension.configuration.biome.v2.BiomeConfiguration;
import net.zic.ascension.configuration.dimension.v2.DimensionConfiguration;


import java.util.stream.Stream;

/*
    TODO add affinities to ChunkAffinityProvider
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ChunkEventHandler {
    public static final Identifier chunkModifierId = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"chunk_modifier");


    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
        if(event.getChunk().getLevel().isClientSide()) return;
        LevelChunk chunk = event.getChunk();
        ChunkQiHandler qiHandler = ChunkQiHelper.getQiHandler(event.getChunk());


        ReferenceSet<Holder<Biome>> processed = new ReferenceOpenHashSet<>();
        for(LevelChunkSection section : chunk.getSections()){
            section.getBiomes().getAll(biome->{
                BiomeConfiguration configuration = biome.getData(ConfigurationDataMaps.BIOME_CONFIGURATION);
                if (configuration == null) return;

                if(processed.contains(biome)) return;

                qiHandler.addCapacityBaseValue(configuration.capacity());
                qiHandler.addRegenRateBaseValue(configuration.regenRate());

                processed.add(biome);
            });
        }
        DimensionConfiguration dimensionConfiguration = DimensionConfiguration.getConfiguration(event.getChunk().getLevel());

        if(dimensionConfiguration == null) return;
        qiHandler.addCapacityBaseValue(dimensionConfiguration.capacity());
        qiHandler.addRegenRateBaseValue(dimensionConfiguration.regenRate());

    }
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {

        int bucket = event.getServer().getTickCount() % 20;

        for (ServerLevel level : event.getServer().getAllLevels()){

            Stream<ChunkHolder> chunks = level.getChunkSource().chunkMap.allChunksWithAtLeastStatus(ChunkStatus.FULL);
            chunks.forEach(chunkHolder -> {
                LevelChunk chunk = chunkHolder.getTickingChunk();
                if(chunk == null) return;
                if(chunk.getPos().hashCode()%20 != bucket) return;
                if(!chunk.hasData(AscensionAttachments.CHUNK_QI_HANDLER))return;
                chunk.getData(AscensionAttachments.CHUNK_QI_HANDLER).regenQi();

                chunk.markUnsaved();
            });
        }
    }
}
