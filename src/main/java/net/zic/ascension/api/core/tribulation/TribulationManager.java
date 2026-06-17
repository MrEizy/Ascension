package net.zic.ascension.api.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.source.SourceHandler;

import java.util.*;
import java.util.function.Consumer;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class TribulationManager extends SavedData {

    private static TribulationManager instance;
    public static TribulationManager getInstance(){
        return instance;
    }

    public static final SavedDataType<TribulationManager> ID = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tribulation_manager"),
            TribulationManager::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(
                                    Codec.STRING.xmap(UUID::fromString, UUID::toString),
                                    TribulationInstance.CODEC
                            )
                            .fieldOf("tribulations")
                            .forGetter(TribulationManager::getTribulations)
            ).apply(instance, TribulationManager::new))
    );


    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event){
        event.getServer().getDataStorage().computeIfAbsent(ID);

        instance = event.getServer().getDataStorage().get(ID);


    }
    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event){
        if(!getInstance().entityTribulations.containsKey(event.getEntity().getUUID())) return;

        for(UUID tribulationId : getInstance().entityTribulations.get(event.getEntity().getUUID())){
            getInstance().tribulations.get(tribulationId).setEntityReference(null);
        }
    }
    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof LivingEntity entity)) return;
        if(!getInstance().entityTribulations.containsKey(entity.getUUID())) return;

        for(UUID tribulationId : getInstance().entityTribulations.get(entity.getUUID())){
            getInstance().tribulations.get(tribulationId).setEntityReference(entity);
        }
    }

    private final HashMap<UUID,TribulationInstance> tribulations = new HashMap<>();
    private final HashMap<UUID, HashSet<UUID>> entityTribulations = new HashMap<>();
    public TribulationManager(){

    }
    public TribulationManager(Map<UUID,TribulationInstance> tribulations){

        for(Map.Entry<UUID, TribulationInstance> entry : tribulations.entrySet()){
            if(entry.getValue() == TribulationInstance.INVALID){
                AscensionCraft.LOGGER.debug("discarding invalid tribulation instance");
                setDirty();
                continue;
            }
            this.tribulations.put(entry.getKey(),entry.getValue());

            entityTribulations.computeIfAbsent(entry.getValue().getEntityId(),key->new HashSet<>());
            entityTribulations.get(entry.getValue().getEntityId()).add(entry.getKey());

            AscensionCraft.LOGGER.debug("Loaded tribulation : {}",entry.getKey());
        }
    }

    public boolean hasTribulation(UUID tribulation){
        return tribulations.containsKey(tribulation);
    }

    public HashMap<UUID,TribulationInstance> getTribulations(){
        return tribulations;
    }
    public HashMap<UUID,HashSet<UUID>> getEntities() {return entityTribulations;}
    public void setOnFinishListener(UUID id, Consumer<TribulationData> tribulationDataConsumer){
        if(!tribulations.containsKey(id))return;
        tribulations.get(id).setFinalizationConsumer(tribulationDataConsumer);
    }
    public UUID triggerTribulation(TribulationDefinition definition, LivingEntity targetEntity){

        TribulationData data = definition.getType().newData();
        UUID id = UUID.randomUUID();
        tribulations.put(id,new TribulationInstance(definition,data,targetEntity));

        entityTribulations.computeIfAbsent(targetEntity.getUUID(),key->new HashSet<>());
        entityTribulations.get(targetEntity.getUUID()).add(id);
        setDirty();

        return id;
    }

    public void finishTribulation(UUID id){
        TribulationInstance tribulationInstance = tribulations.remove(id);
        entityTribulations.get(tribulationInstance.getEntityId()).remove(id);
        if(entityTribulations.get(tribulationInstance.getEntityId()).isEmpty()) entityTribulations.remove(tribulationInstance.getEntityId());
        setDirty();
    }



    public void setTribulationConsumer(UUID tribulation, Consumer<TribulationData> consumer){
        if(!hasTribulation(tribulation)) return;
        //TODO
    }
}
