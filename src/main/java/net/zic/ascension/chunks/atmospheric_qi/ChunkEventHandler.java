package net.zic.ascension.chunks.atmospheric_qi;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreAttachments;

import net.zic.ascension.api.ascension.core.path.bonus.EntityPathBonusHolder;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.ConfigurationDataMaps;
import net.zic.ascension.configuration.biome.BiomeConfiguration;
import net.zic.ascension.configuration.dimension.DimensionConfiguration;
import net.zic.zenithlib.value_containers.typed.Modifier;


import java.util.stream.Stream;

/*
    TODO add affinities to ChunkAffinityProvider
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ChunkEventHandler {

    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
        if(event.getChunk().getLevel().isClientSide()) return;
        LevelChunk chunk = event.getChunk();
        ChunkQiHandler qiHandler = ChunkHelper.getQiHandler(event.getChunk());
        ChunkPathAffinityProvider affinityProvider = ChunkHelper.getAffinityProvider(event.getChunk());

        ReferenceSet<Holder<Biome>> processed = new ReferenceOpenHashSet<>();
        for(LevelChunkSection section : chunk.getSections()){
            section.getBiomes().getAll(biome->{

                BiomeConfiguration configuration = biome.getData(ConfigurationDataMaps.BIOME_CONFIGURATION);
                if (configuration == null) return;
                Identifier modifierId = biome.getKey().identifier();
                if(processed.contains(biome)) return;

                qiHandler.addCapacityFlatModifier(Modifier.base(modifierId,configuration.capacity()));
                qiHandler.addCapacityFlatModifier(Modifier.base(modifierId,configuration.regenRate()));
                affinityProvider.startProcess("updating_base_affinities");
                for(Identifier path : configuration.affinities().keySet()){
                    affinityProvider.addFlatModifier(path,Modifier.base(modifierId,configuration.affinities().getDouble(path)));
                }
                processed.add(biome);
            });
        }
        DimensionConfiguration dimensionConfiguration = DimensionConfiguration.getConfiguration(event.getChunk().getLevel());

        if(dimensionConfiguration == null) return;
        Identifier dimensionId = event.getChunk().getLevel().dimension().identifier();
        qiHandler.addCapacityFlatModifier(Modifier.base(dimensionId,dimensionConfiguration.capacity()));
        qiHandler.addCapacityFlatModifier(Modifier.base(dimensionId,dimensionConfiguration.regenRate()));
        for(Identifier path : dimensionConfiguration.affinities().keySet()){
            affinityProvider.addFlatModifier(path,Modifier.base(dimensionId,dimensionConfiguration.affinities().getDouble(path)));
        }

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
    //====================================== AFFINITY ======================================


    @SubscribeEvent
    public static void onEnterChunk(EntityEvent.EnteringSection event) {
        if(!event.didChunkChange()) return;
        if(!(event.getEntity() instanceof LivingEntity entity)) return;


        EntityPathBonusHolder bonusHolder = entity.getData(CoreAttachments.PATH_BONUS_HOLDER);

        ChunkPos oldPos = event.getOldPos().chunk();
        ChunkPos pos = event.getNewPos().chunk();
        LevelChunk chunk = entity.level().getChunk(pos.x(),pos.z());
        LevelChunk oldChunk = entity.level().getChunk(oldPos.x(),oldPos.z());

        bonusHolder.startProcess("updating_chunk_path_bonuses");
        bonusHolder.removeProvider(oldChunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER));
        oldChunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER).untrackEntity(entity);
        bonusHolder.registerProvider(chunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER));
        chunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER).trackEntity(entity);
        bonusHolder.resolveProcess("updating_chunk_path_bonuses");

    }


    @SubscribeEvent
    public static void onEnterLevel(EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof LivingEntity entity)) return;



        EntityPathBonusHolder bonusHolder = entity.getData(CoreAttachments.PATH_BONUS_HOLDER);
        BlockPos pos = entity.blockPosition();
        ChunkAccess chunk = entity.level().getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if(chunk == null) return;
        bonusHolder.registerProvider(chunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER));
        chunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER).trackEntity(entity);


    }
    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event){
        if(!(event.getEntity() instanceof LivingEntity entity)) return;


        EntityPathBonusHolder bonusHolder = entity.getData(CoreAttachments.PATH_BONUS_HOLDER);
        BlockPos pos = entity.blockPosition();
        ChunkAccess chunk = entity.level().getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if(chunk == null) return;
        bonusHolder.removeProvider(chunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER));
        chunk.getData(AscensionAttachments.CHUNK_AFFINITY_HANDLER).untrackEntity(entity);
    }



}
