package net.zic.ascension.impl.datapack.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.active.ActiveSkillLevelDefinition;
import net.zic.ascension.api.ascension.core.skill.levelled.SkillProgressionData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.castable.active.ActiveSkill;
import net.zic.ascension.impl.core.skill.castable.active.ActiveSkillData;

import java.util.List;

public final class ActiveSkillType extends SkillType {
    private static final MapCodec<ActiveSkillData> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkillProgressionData.CODEC.codec().fieldOf("progression")
                    .forGetter(ActiveSkillData::getSkillProgression)
    ).apply(instance, ActiveSkillData::new));

    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<ActiveSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ActiveSkill::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(ActiveSkill::getDescription),
                Codec.INT.optionalFieldOf("default_accessible_level", 0)
                        .forGetter(ActiveSkill::getConfiguredDefaultAccessibleLevel),
                ActiveSkillLevelDefinition.CODEC.codec().listOf().fieldOf("levels")
                        .forGetter(ActiveSkill::getLevels),
                Codec.DOUBLE.listOf().optionalFieldOf("experience_requirements", List.of())
                        .forGetter(ActiveSkill::getExperienceRequirements)
        ).apply(instance, ActiveSkill::new));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
