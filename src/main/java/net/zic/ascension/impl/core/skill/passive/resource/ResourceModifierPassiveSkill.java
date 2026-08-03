package net.zic.ascension.impl.core.skill.passive.resource;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.levelled.LevelledSkill;
import net.zic.ascension.api.ascension.core.skill.levelled.SkillProgressionData;
import net.zic.ascension.api.ascension.core.resource.modifier.ResourceModifierDefinition;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

import java.util.List;

public final class ResourceModifierPassiveSkill implements LevelledSkill {
    private final Component name;
    private final Component description;
    private final int defaultAccessibleLevel;
    private final List<ResourceModifierLevelDefinition> levels;
    private final List<Double> experienceRequirements;

    public ResourceModifierPassiveSkill(
            Component name,
            Component description,
            int defaultAccessibleLevel,
            List<ResourceModifierLevelDefinition> levels,
            List<Double> experienceRequirements
    ) {
        this.name = name;
        this.description = description;
        this.levels = levels == null ? List.of() : List.copyOf(levels);
        this.defaultAccessibleLevel = Math.clamp(defaultAccessibleLevel, 0, this.levels.size());
        this.experienceRequirements = experienceRequirements == null
                ? List.of()
                : experienceRequirements.stream().map(value -> Math.max(0.0D, value)).toList();
    }

    public int getConfiguredDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    public List<ResourceModifierLevelDefinition> getLevels() {
        return levels;
    }

    public List<Double> getExperienceRequirements() {
        return experienceRequirements;
    }

    public List<ResourceModifierDefinition> getModifiers(int level) {
        if (level <= 0 || level > levels.size()) {
            return List.of();
        }
        return levels.get(level - 1).modifiers();
    }

    @Override
    public int getMaximumLevel() {
        return levels.size();
    }

    @Override
    public int getDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    @Override
    public double getExperienceRequiredForNextLevel(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= experienceRequirements.size()) {
            return Double.POSITIVE_INFINITY;
        }
        return experienceRequirements.get(currentLevel);
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.RESOURCE_MODIFIER_PASSIVE_SKILL_TYPE.get();
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
    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public SkillData newData(RegistryAccess access) {
        return new ResourceModifierPassiveSkillData(new SkillProgressionData());
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new ResourceModifierPassiveSkillData(input);
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new ResourceModifierPassiveSkillData(buf);
    }
}
