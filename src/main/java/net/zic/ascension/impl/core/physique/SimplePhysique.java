package net.zic.ascension.impl.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.path.PathBonusModifier;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.physique.AscensionPhysiqueTypes;

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
                             List<PathBonusBase> basePathBonuses,
                             List<PathBonusModifier> pathBonusModifiers,
                             Optional<AscensionItemTooltipDefinition> itemTooltip) implements Physique {


    public SimplePhysique(
            Component name,
            Component description,
            List<Identifier> unlockedPaths,
            List<Identifier> skills,
            List<ValueContainer.BaseModifier> baseStats,
            Map<Identifier, List<ValueContainerModifier>> statModifiers,
            List<PathBonusBase> basePathBonuses,
            List<PathBonusModifier> pathBonusModifiers,
            Optional<AscensionItemTooltipDefinition> itemTooltip
    ) {
        this.name = name;
        this.description = description;
        this.unlockedPaths = unlockedPaths;
        this.skills = skills;
        this.baseStats = baseStats;
        this.basePathBonuses = basePathBonuses;
        this.statModifiers = statModifiers;
        this.pathBonusModifiers = pathBonusModifiers;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        AscensionCraft.LOGGER.info("created Simple Physique {}", name);
    }

    @Override
    public PhysiqueType getType() {
        return   AscensionPhysiqueTypes.SIMPLE_PHYSIQUE_TYPE.get();
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, PhysiqueData data) {

        Identifier physiqueId = CoreRegistries.PHYSIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this);

        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            source.addStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()), baseModifier.val());
        }
        for (Identifier stat : statModifiers.keySet()) {
            for (ValueContainerModifier modifier : statModifiers.get(stat)) {
                source.addStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat), modifier);
            }
        }

        for(PathBonusBase base : basePathBonuses) AscensionOriginSourceHelper.addBonus(source,base.category(),base.path(), base.value());
        for(PathBonusModifier modifier : pathBonusModifiers) AscensionOriginSourceHelper.addBonusModifier(source,modifier.category(),modifier.path(),modifier.modifier());


        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.addSkill(source,skill,physiqueId);
        }

        return unlockedPaths;
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, PhysiqueData data) {
        Identifier physiqueId = CoreRegistries.PHYSIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this);

        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            source.removeStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()), baseModifier.val());
        }
        for (Identifier stat : statModifiers.keySet()) {
            for (ValueContainerModifier modifier : statModifiers.get(stat)) {

                source.removeStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat), modifier.getIdentifier());
            }
        }

        for(PathBonusBase base : basePathBonuses) AscensionOriginSourceHelper.removeBonus(source,base.category(),base.path(), base.value());
        for(PathBonusModifier modifier : pathBonusModifiers) AscensionOriginSourceHelper.removeBonusModifier(source,modifier.category(),modifier.path(),modifier.modifier().getIdentifier());

        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.removeSkill(source,skill,physiqueId);
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
