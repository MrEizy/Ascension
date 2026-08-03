package net.zic.ascension.chunks.atmospheric_qi;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.core.Holder;
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
import net.zic.ascension.api.ascension.core.entity.AscensionEntityPathBonusHolder;
import net.zic.ascension.configuration.biome.BiomeConfiguration;
import net.zic.ascension.configuration.biome.BiomeConfigurations;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.dimension.DimensionConfiguration;
import net.zic.ascension.configuration.dimension.DimensionConfigurations;

import java.util.stream.Stream;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ChunkHandler {
    public static final  Identifier chunkModifierId = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"chunk_modifier");

    /*
        TODO on chunk load restore default options from either config or datapack (undecided)

        consider adding config options or datapack support so distance from spawn can be a factor
        TODO add at least an initial client sync
    */
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
        if(event.getChunk().getLevel().isClientSide()) return;
        LevelChunk chunk = event.getChunk();
        ChunkQiContainer chunkQiContainer = chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);

        //TODO load regen rate and base cap from biome and dimension

        ReferenceSet<Holder<Biome>> processed = new ReferenceOpenHashSet<>();
        for(LevelChunkSection section : chunk.getSections()){
            section.getBiomes().getAll(biome->{
               // if(BiomeConfigurations.getInstance() == null) return; //should not happen but just in case
                if(!BiomeConfigurations.getInstance().hasConfiguration(biome)) return;
                if(processed.contains(biome)) return;
                //DO smth here
                BiomeConfiguration configuration = BiomeConfigurations.getInstance().getConfiguration(biome);
                chunkQiContainer.energyCap.setBaseValue(chunkQiContainer.energyCap.getBaseValue()+configuration.energyCap());
                chunkQiContainer.energyRegenRate.setBaseValue(chunkQiContainer.energyRegenRate.getBaseValue()+configuration.energyRegen());
                for(Identifier path : configuration.affinities().keySet()){
                    chunkQiContainer.addAffinity(path, configuration.affinities().getDouble(path));
                  }
                processed.add(biome);
            });
        }
       DimensionConfiguration dimensionConfiguration = DimensionConfigurations.getInstance().getConfiguration(event.getChunk().getLevel().dimension().identifier());

        if(dimensionConfiguration == null) return;

        chunkQiContainer.energyCap.setBaseValue(chunkQiContainer.energyCap.getBaseValue()+dimensionConfiguration.energyCap());
        chunkQiContainer.energyRegenRate.setBaseValue(chunkQiContainer.energyRegenRate.getBaseValue()+dimensionConfiguration.energyRegen());
        for(Identifier path : dimensionConfiguration.affinities().keySet()){
            chunkQiContainer.addAffinity(path, dimensionConfiguration.affinities().getDouble(path));
        }
        //no need to mark unsaved
        //chunk.syncData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);
    }

    @SubscribeEvent
    public static void onEnterChunk(EntityEvent.EnteringSection event) {
        if(!event.didChunkChange()) return;
        if(!(event.getEntity() instanceof LivingEntity entity)) return;


        AscensionEntityPathBonusHolder bonusHolder = entity.getData(CoreAttachments.PATH_BONUS_HOLDER);

        ChunkPos oldPos = event.getOldPos().chunk();
        ChunkPos pos = event.getNewPos().chunk();
        LevelChunk chunk = entity.level().getChunk(pos.x(),pos.z());
        LevelChunk oldChunk = entity.level().getChunk(oldPos.x(),oldPos.z());

        bonusHolder.removePathBonusProvider(oldChunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER));
        bonusHolder.registerPathBonusProvider(chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER));

    }



    @SubscribeEvent
    public static void onEnterLevel(EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof LivingEntity entity)) return;



        AscensionEntityPathBonusHolder bonusHolder = entity.getData(CoreAttachments.PATH_BONUS_HOLDER);

        ChunkAccess chunk = entity.level().getChunk(entity.blockPosition());

        bonusHolder.registerPathBonusProvider(chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER));



    }
    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event){
        if(!(event.getEntity() instanceof LivingEntity entity)) return;
        AscensionEntityDataProvider holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY, null);
        if(holder == null) return;
        AscensionEntityPathBonusHolder bonusHolder = entity.getData(CoreAttachments.PATH_BONUS_HOLDER);

        ChunkAccess chunk = entity.level().getChunk(entity.blockPosition());

        bonusHolder.removePathBonusProvider(chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER));

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
               if(!chunk.hasData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER))return;
               chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER).regenEnergy();

               chunk.markUnsaved();
            });
        }
    }

}
