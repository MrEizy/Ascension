package net.zic.ascension.core.physique;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.datapack.physique.AscensionPhysiqueTypes;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimplePhysique implements Physique {


    private final Component name;
    private final Component description;
    private final List<Identifier> unlockedPaths;
    private final List<ValueContainer.BaseModifier> baseStats;
    private final Map<Identifier,List<ValueContainerModifier>> statModifiers;
    private final List<ValueContainer.BaseModifier> baseAffinities;
    private final Map<Identifier,List<ValueContainerModifier>> affinityModifiers;
    public SimplePhysique(
            Component name,
            Component description,
            List<Identifier> unlockedPaths,
            List<ValueContainer.BaseModifier> baseStats,
            Map<Identifier,List<ValueContainerModifier>> statModifiers,
            List<ValueContainer.BaseModifier> baseAffinities,
            Map<Identifier,List<ValueContainerModifier>> affinityModifiers
    ){
        this.name = name;
        this.description = description;
        this.unlockedPaths = unlockedPaths;
        this.baseStats = baseStats;
        this.baseAffinities = baseAffinities;
        this.statModifiers = statModifiers;
        this.affinityModifiers =affinityModifiers;
        AscensionCraft.LOGGER.info("created Simple Physique {}", name);
    }

    @Override
    public PhysiqueType getType() {
        return AscensionPhysiqueTypes.SIMPLE_PHYSIQUE_TYPE.get();
    }

    public List<ValueContainer.BaseModifier> getBaseAffinities() {
        return baseAffinities;
    }

    public List<ValueContainer.BaseModifier> getBaseStats() {
        return baseStats;
    }

    public Map<Identifier, List<ValueContainerModifier>> getAffinityModifiers() {
        return affinityModifiers;
    }

    public Map<Identifier, List<ValueContainerModifier>> getStatModifiers() {
        return statModifiers;
    }

    public Component getName(){
        return name;
    }
    public Component getDescription(){
        return description;
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, PhysiqueData data) {

        for(ValueContainer.BaseModifier baseModifier : baseStats){
            source.addStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()),baseModifier.val());
        }
        for(Identifier stat : statModifiers.keySet()){
            for(ValueContainerModifier modifier : statModifiers.get(stat)){
                source.addStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat),modifier);
            }
        }

        return unlockedPaths;
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, PhysiqueData data) {
        for(ValueContainer.BaseModifier baseModifier : baseStats){
            source.removeStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()),baseModifier.val());
        }
        for(Identifier stat : statModifiers.keySet()){
            for(ValueContainerModifier modifier : statModifiers.get(stat)){

                source.removeStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat),modifier.getIdentifier());
            }
        }
        //TODO need to think of a general way to let all entities listening to the source know stats where changed.
        //TODO so make a basic StatChangeEvent that holds an entity in the lib
        //TODO then the source is able to call a SourceStatChangeEvent. Entity Listeners that see this event(and "own" the source)
        //TODO are able to call StatChangeEvent, which is picked up by my lib Listener
        //TODO this should allow for compatability with other mods using a similar system
        return unlockedPaths;
    }

    @Override
    public void applyToEntity(LivingEntity entity, PhysiqueData data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, PhysiqueData data) {

    }

    @Override
    public PhysiqueData newData() {
        return null;
    }

    @Override
    public PhysiqueData loadData(ValueInput input) {
        return null;
    }

    @Override
    public PhysiqueData loadData(RegistryFriendlyByteBuf buf) {
        return null;
    }

    public List<Identifier> getUnlockedPaths(){
        return unlockedPaths;
    }
}
