package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.value_containers.ModifierOperation;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ChunkHandler {

    /*TODO
        on chunk load restore default options from either config or datapack (undecided)

        consider adding config options or datapack support so distance from spawn can be a factor
    */
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
        LevelChunk chunk = event.getChunk();
        ChunkQiContainer chunkQiContainer = chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);

        //TODO load regen rate and base cap from biome and dimension
        for(LevelChunkSection section : chunk.getSections()){
            section.getBiomes().getAll(biome->{
                //DO smth here
                biome.value().modifiableBiomeInfo().ex
            });
            //TODO for testing use a default value of 100 and regen rate of 2
        }

        chunk.setData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER, new ChunkQiContainer(
                chunkQiContainer.getEnergy(),
                100, //TODO replace with biome specific value
                2
        ));
    }
    public static void onEnterChunk(EntityEvent.EnteringSection event) {
        if(!event.didChunkChange()) return;
        if(!(event.getEntity() instanceof LivingEntity entity)) return;

        AscensionEntityDataHolder holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY, null);
        if(holder == null) return;

        AscensionEntityData data = holder.getData(entity);

        ChunkPos pos = event.getNewPos().chunk();
        LevelChunk chunk = entity.level().getChunk(pos.x(),pos.z());

        ChunkQiContainer qiContainer = chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);


        //remove ALL previous modifiers
        Identifier chunkModifierId = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"chunk_modifier");
        for(Identifier path : data.getAllAffinities()){
            data.removeAffinityModifier(path,chunkModifierId);
        }

        //add all affinities as an ADD_FINAL modifier
        for (Identifier path : qiContainer.getAllAffinities()){
            data.addAffinityModifier(path, new ValueContainerModifier(
                    qiContainer.getAffinity(path),
                    ModifierOperation.ADD_FINAL,
                    chunkModifierId
            ));
        }



    }



}
