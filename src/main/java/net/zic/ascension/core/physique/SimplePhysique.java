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
        return null;
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
        return List.of();
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, PhysiqueData data) {
        return List.of();
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
