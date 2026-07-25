package net.zic.ascension.impl.datapack.skill.passive.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.skill.levelled.SkillProgressionData;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.passive.resource.ResourceModifierLevelDefinition;
import net.zic.ascension.impl.core.skill.passive.resource.ResourceModifierPassiveSkill;
import net.zic.ascension.impl.core.skill.passive.resource.ResourceModifierPassiveSkillData;

import java.util.List;

public final class ResourceModifierPassiveSkillType extends SkillType {
    private static final MapCodec<ResourceModifierPassiveSkillData> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkillProgressionData.CODEC.codec().fieldOf("progression")
                    .forGetter(ResourceModifierPassiveSkillData::getSkillProgression)
    ).apply(instance, ResourceModifierPassiveSkillData::new));

    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<ResourceModifierPassiveSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ResourceModifierPassiveSkill::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(ResourceModifierPassiveSkill::getDescription),
                Codec.INT.optionalFieldOf("default_accessible_level", 0)
                        .forGetter(ResourceModifierPassiveSkill::getConfiguredDefaultAccessibleLevel),
                ResourceModifierLevelDefinition.CODEC.codec().listOf().fieldOf("levels")
                        .forGetter(ResourceModifierPassiveSkill::getLevels),
                Codec.DOUBLE.listOf().optionalFieldOf("experience_requirements", List.of())
                        .forGetter(ResourceModifierPassiveSkill::getExperienceRequirements)
        ).apply(instance, ResourceModifierPassiveSkill::new));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
