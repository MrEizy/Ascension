package net.zic.ascension.impl.datapack.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.PassiveModule;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.passive.ResourceModifierPassiveSkill;
import net.zic.ascension.impl.core.skill.passive.ResourceModifierPassiveSkill.Data;

import java.util.List;

public final class ResourceModifierPassiveSkillType extends SkillType {
    private static final MapCodec<Data> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkillProgressionData.CODEC.codec().optionalFieldOf("progression", new SkillProgressionData())
                    .forGetter(Data::getSkillProgression),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Data::isEnabled)
    ).apply(instance, Data::new));

    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<ResourceModifierPassiveSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ResourceModifierPassiveSkill::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(ResourceModifierPassiveSkill::getDescription),
                Codec.INT.optionalFieldOf("default_accessible_level", 0)
                        .forGetter(ResourceModifierPassiveSkill::getConfiguredDefaultAccessibleLevel),
                ResourceModifierPassiveSkill.LevelTemplate.CODEC.codec().listOf().optionalFieldOf("levels", List.of())
                        .forGetter(ResourceModifierPassiveSkill::getLevelTemplates),
                PassiveModule.CODEC.listOf().optionalFieldOf("modules", List.of())
                        .forGetter(skill -> skill.getLevels().isEmpty() ? List.of() : skill.getLevels().getFirst().modules()),
                SkillDefinitions.CODEC.codec().optionalFieldOf("definitions", SkillDefinitions.EMPTY).forGetter(ResourceModifierPassiveSkill::definitions),
                Codec.DOUBLE.listOf().optionalFieldOf("experience_requirements", List.of())
                        .forGetter(ResourceModifierPassiveSkill::getExperienceRequirements),
                Codec.BOOL.optionalFieldOf("toggleable", false).forGetter(ResourceModifierPassiveSkill::isToggleable),
                Codec.BOOL.optionalFieldOf("enabled_by_default", true).forGetter(ResourceModifierPassiveSkill::isEnabledByDefault),
                ResourceModifierPassiveSkill.Upkeep.CODEC.optionalFieldOf("upkeep").forGetter(ResourceModifierPassiveSkill::getUpkeep)
        ).apply(instance, ResourceModifierPassiveSkill::create));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
