package net.zic.ascension.impl.datapack.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillLevelDefinition;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill.Data;

import java.util.List;

public final class ActiveSkillType extends SkillType {
    private static final MapCodec<Data> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkillProgressionData.CODEC.codec().optionalFieldOf("progression", new SkillProgressionData())
                    .forGetter(Data::getSkillProgression)
    ).apply(instance, Data::new));

    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<ActiveSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ActiveSkill::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(ActiveSkill::getDescription),
                Codec.INT.optionalFieldOf("default_accessible_level", 0).forGetter(ActiveSkill::getConfiguredDefaultAccessibleLevel),
                SkillDefinitions.CODEC.codec().optionalFieldOf("definitions", SkillDefinitions.EMPTY).forGetter(ActiveSkill::definitions),
                ActiveSkillLevelDefinition.Template.CODEC.forGetter(ActiveSkill::getRootTemplate),
                ActiveSkillLevelDefinition.Template.CODEC.codec().listOf().optionalFieldOf("levels", List.of())
                        .forGetter(ActiveSkill::getLevelTemplates),
                Codec.DOUBLE.listOf().optionalFieldOf("experience_requirements", List.of())
                        .forGetter(ActiveSkill::getExperienceRequirements)
        ).apply(instance, ActiveSkill::new));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
