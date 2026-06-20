package net.zic.ascension.impl.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.physique.AscensionPhysiqueTypes;
import net.zic.ascension.impl.datapack.util.AffinityModifier;
import net.zic.ascension.impl.datapack.util.BaseAffinity;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SimplePhysique(Component name, Component description, List<Identifier> unlockedPaths,
                             List<Identifier> skills, List<ValueContainer.BaseModifier> baseStats,
                             Map<Identifier, List<ValueContainerModifier>> statModifiers,
                             List<BaseAffinity> baseAffinities,
                             Map<Identifier, List<AffinityModifier>> affinityModifiers,
                             Optional<AscensionItemTooltipDefinition> itemTooltip) implements Physique {


    public SimplePhysique(
            Component name,
            Component description,
            List<Identifier> unlockedPaths,
            List<Identifier> skills,
            List<ValueContainer.BaseModifier> baseStats,
            Map<Identifier, List<ValueContainerModifier>> statModifiers,
            List<BaseAffinity> baseAffinities,
            Map<Identifier, List<AffinityModifier>> affinityModifiers,
            Optional<AscensionItemTooltipDefinition> itemTooltip
    ) {
        this.name = name;
        this.description = description;
        this.unlockedPaths = unlockedPaths;
        this.skills = skills;
        this.baseStats = baseStats;
        this.baseAffinities = baseAffinities;
        this.statModifiers = statModifiers;
        this.affinityModifiers = affinityModifiers;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        AscensionCraft.LOGGER.info("created Simple Physique {}", name);
    }

    @Override
    public PhysiqueType getType() {
        return   AscensionPhysiqueTypes.SIMPLE_PHYSIQUE_TYPE.get();
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, PhysiqueData data) {

        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            source.addStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()), baseModifier.val());
        }
        for (Identifier stat : statModifiers.keySet()) {
            for (ValueContainerModifier modifier : statModifiers.get(stat)) {
                source.addStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat), modifier);
            }
        }
        for (Identifier skill : skills) {
            source.addSkill(skill);
        }

        return unlockedPaths;
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, PhysiqueData data) {
        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            source.removeStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()), baseModifier.val());
        }
        for (Identifier stat : statModifiers.keySet()) {
            for (ValueContainerModifier modifier : statModifiers.get(stat)) {

                source.removeStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat), modifier.getIdentifier());
            }
        }
        //TODO update to more properly handle the try remove to more efficiently check by directly calling skillRemovalAttempt on bloodline,technique, physique and data source

        for (Identifier skill : skills) {
            source.removeSkill(skill);
        }

        return unlockedPaths;
    }

    @Override
    public void applyToEntity(LivingEntity entity, PhysiqueData data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, PhysiqueData data) {

    }

    @Override
    public PhysiqueData newData(RegistryAccess access) {
        return new EmptyPhysiqueData();
    }

    @Override
    public PhysiqueData loadData(ValueInput input,RegistryAccess access) {
        return new EmptyPhysiqueData();
    }

    @Override
    public PhysiqueData loadData(ByteBuf buf) {
        return new EmptyPhysiqueData();
    }
}
