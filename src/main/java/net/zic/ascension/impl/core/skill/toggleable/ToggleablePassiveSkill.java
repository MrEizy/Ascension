package net.zic.ascension.impl.core.skill.toggleable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.capabilities.EntityQiProvider;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ToggleablePassiveSkill implements ToggleableSkill {
    private final Component name;
    private final Component description;
    private final boolean enabledByDefault;
    private final Optional<QiUpkeep> qiUpkeep;

    private final List<ValueContainer.BaseModifier> baseStats;
    private final Map<Identifier, List<ValueContainerModifier>> statModifiers;
    private final List<ValueContainer.BaseModifier> baseAffinities;
    private final Map<Identifier, List<ValueContainerModifier>> affinityModifiers;

    public ToggleablePassiveSkill(
            Component name,
            Component description,
            boolean enabledByDefault,
            Optional<QiUpkeep> qiUpkeep,
            List<ValueContainer.BaseModifier> baseStats,
            Map<Identifier, List<ValueContainerModifier>> statModifiers,
            List<ValueContainer.BaseModifier> baseAffinities,
            Map<Identifier, List<ValueContainerModifier>> affinityModifiers
    ) {
        this.name = name;
        this.description = description;
        this.enabledByDefault = enabledByDefault;
        this.qiUpkeep = qiUpkeep;
        this.baseStats = baseStats;
        this.statModifiers = statModifiers;
        this.baseAffinities = baseAffinities;
        this.affinityModifiers = affinityModifiers;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public Optional<QiUpkeep> getQiUpkeep() {
        return qiUpkeep;
    }

    public List<ValueContainer.BaseModifier> getBaseStats() {
        return baseStats;
    }

    public Map<Identifier, List<ValueContainerModifier>> getStatModifiers() {
        return statModifiers;
    }

    public List<ValueContainer.BaseModifier> getBaseAffinities() {
        return baseAffinities;
    }

    public Map<Identifier, List<ValueContainerModifier>> getAffinityModifiers() {
        return affinityModifiers;
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.TOGGLEABLE_PASSIVE_SKILL_TYPE.get();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public void onAdded(OriginSource source, SkillData data) {
        if (isEnabled(data)) {
            applyModifiers(source);
        }
    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {
        if (isEnabled(data)) {
            removeModifiers(source);
        }
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public SkillData newData(RegistryAccess access) {
        return new ToggleablePassiveSkillData(enabledByDefault);
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new ToggleablePassiveSkillData(input, enabledByDefault);
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new ToggleablePassiveSkillData(buf);
    }

    @Override
    public boolean isEnabled(SkillData data) {
        return data instanceof ToggleablePassiveSkillData toggleData
                && toggleData.isEnabled();
    }

    @Override
    public void setEnabled(SkillData data, boolean enabled) {
        if (data instanceof ToggleablePassiveSkillData toggleData) {
            toggleData.setEnabled(enabled);
        }
    }

    @Override
    public boolean canEnable(
            LivingEntity entity,
            OriginSource source,
            SkillData data
    ) {
        if (qiUpkeep.isEmpty()) {
            return true;
        }

        EntityQiProvider qiProvider = entity.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER
        );
        if (qiProvider == null) {
            return false;
        }

        return qiProvider.getQi() >= qiUpkeep.get().cost(qiProvider.getMaxQi());
    }

    @Override
    public void onEnabled(OriginSource source, SkillData data) {
        applyModifiers(source);
    }

    @Override
    public void onDisabled(OriginSource source, SkillData data) {
        removeModifiers(source);
    }

    @Override
    public boolean tickEnabled(
            LivingEntity entity,
            OriginSource source,
            SkillData data
    ) {
        if (qiUpkeep.isEmpty()) {
            return true;
        }

        QiUpkeep upkeep = qiUpkeep.get();
        if (entity.level().getGameTime() % upkeep.interval() != 0L) {
            return true;
        }

        EntityQiProvider qiProvider = entity.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER
        );
        if (qiProvider == null) {
            return false;
        }

        return qiProvider.reduceQi(upkeep.cost(qiProvider.getMaxQi()));
    }

    private void applyModifiers(OriginSource source) {
        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container());
            if (stat != null) {
                source.addStat(stat, baseModifier.val());
            }
        }

        statModifiers.forEach((statId, modifiers) -> {
            Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(statId);
            if (stat == null) {
                return;
            }
            for (ValueContainerModifier modifier : modifiers) {
                source.addStatModifier(stat, modifier);
            }
        });

        for (ValueContainer.BaseModifier baseModifier : baseAffinities) {
            source.addAffinity(baseModifier.container(), baseModifier.val());
        }

        affinityModifiers.forEach((path, modifiers) -> {
            for (ValueContainerModifier modifier : modifiers) {
                source.addAffinityModifier(path, modifier);
            }
        });
    }

    private void removeModifiers(OriginSource source) {
        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container());
            if (stat != null) {
                source.removeStat(stat, baseModifier.val());
            }
        }

        statModifiers.forEach((statId, modifiers) -> {
            Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(statId);
            if (stat == null) {
                return;
            }
            for (ValueContainerModifier modifier : modifiers) {
                source.removeStatModifier(stat, modifier.getIdentifier());
            }
        });

        for (ValueContainer.BaseModifier baseModifier : baseAffinities) {
            source.removeAffinity(baseModifier.container(), baseModifier.val());
        }

        affinityModifiers.forEach((path, modifiers) -> {
            for (ValueContainerModifier modifier : modifiers) {
                source.removeAffinityModifier(path, modifier.getIdentifier());
            }
        });
    }
}
