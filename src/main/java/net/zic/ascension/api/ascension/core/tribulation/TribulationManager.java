package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;

import java.util.*;
import java.util.function.BiConsumer;

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
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        TribulationManager manager = getInstance();
        if (manager == null) {
            return;
        }
        UUID entityId = event.getEntity().getUUID();
        HashSet<UUID> entityTribulations = manager.entityTribulations.get(entityId);
        if (entityTribulations == null) {
            return;
        }
        for (UUID tribulationId : entityTribulations) {
            TribulationInstance instance = manager.tribulations.get(tribulationId);
            if (instance != null) {
                instance.setEntityReference(null);
            }
        }
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        TribulationManager manager = getInstance();
        if (manager == null) {
            return;
        }
        HashSet<UUID> entityTribulations = manager.entityTribulations.get(entity.getUUID());
        if (entityTribulations == null) {
            return;
        }
        for (UUID tribulationId : entityTribulations) {
            TribulationInstance instance = manager.tribulations.get(tribulationId);
            if (instance != null) {
                instance.setEntityReference(entity);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        TribulationManager manager = getInstance();
        if (manager == null) {
            return;
        }
        HashSet<UUID> entityTribulations = manager.entityTribulations.remove(event.getEntity().getUUID());
        if (entityTribulations == null) {
            return;
        }
        for (UUID tribulationId : entityTribulations) {
            TribulationInstance instance = manager.tribulations.get(tribulationId);
            if (instance == null) {
                continue;
            }
            instance.getTribulation().getType().onTargetDeath(manager, tribulationId, instance);
            manager.tribulations.remove(tribulationId);
        }
        manager.setDirty();
    }

    private final HashMap<UUID,TribulationInstance> tribulations = new HashMap<>();
    private final ArrayList<UUID> toRemoveUUID = new ArrayList<>();
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
    public TribulationDefinition getTribulation(UUID tribulation){
        if(!tribulations.containsKey(tribulation)) return null;
        return tribulations.get(tribulation).getTribulation();
    }
    public HashMap<UUID,HashSet<UUID>> getEntities() {return entityTribulations;}

    public UUID triggerTribulation(TribulationDefinition definition, LivingEntity targetEntity){

        TribulationData data = definition.getType().newData(definition);
        UUID id = UUID.randomUUID();
        tribulations.put(id,new TribulationInstance(definition,data,targetEntity));

        entityTribulations.computeIfAbsent(targetEntity.getUUID(),key->new HashSet<>());
        entityTribulations.get(targetEntity.getUUID()).add(id);
        setDirty();

        return id;
    }

    public void finishTribulation(UUID id){
        toRemoveUUID.add(id);
    }
    private void resolveFinishedTribulations(){

        while(!toRemoveUUID.isEmpty()){
            UUID id = toRemoveUUID.removeLast();
            TribulationInstance tribulationInstance = tribulations.remove(id);
            entityTribulations.get(tribulationInstance.getEntityId()).remove(id);
            if(entityTribulations.get(tribulationInstance.getEntityId()).isEmpty()) entityTribulations.remove(tribulationInstance.getEntityId());

        }
        setDirty();
    }

    public void setTribulationConsumer(UUID tribulation, BiConsumer<TribulationDefinition,TribulationData> consumer){
        if(!hasTribulation(tribulation)) return;
        getTribulations().get(tribulation).setFinalizationConsumer(consumer);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        instance = null;
    }
}
