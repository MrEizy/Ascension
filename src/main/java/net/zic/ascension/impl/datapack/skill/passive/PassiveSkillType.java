package net.zic.ascension.impl.datapack.skill.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveModifier;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.passive.PassiveSkill;

import java.util.List;

public final class PassiveSkillType extends SkillType {
    private static final MapCodec<PassiveSkill.Data> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkillProgressionData.CODEC.codec().optionalFieldOf("progression", new SkillProgressionData())
                    .forGetter(PassiveSkill.Data::getSkillProgression),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PassiveSkill.Data::isEnabled)
    ).apply(instance, PassiveSkill.Data::new));

    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<PassiveSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(PassiveSkill::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(PassiveSkill::getDescription),
                Codec.intRange(1, 1024).optionalFieldOf("levels", 1).forGetter(PassiveSkill::getConfiguredLevels),
                Codec.INT.optionalFieldOf("level_cap", 1).forGetter(PassiveSkill::getConfiguredBaseLevelCap),
                Codec.DOUBLE.listOf().optionalFieldOf("level_xp", List.of()).forGetter(PassiveSkill::getLevelExperienceRequirements),
                PassiveModifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(PassiveSkill::modifiers),
                PassiveTrigger.CODEC.listOf().optionalFieldOf("triggers", List.of()).forGetter(PassiveSkill::triggers),
                SkillDefinitions.CODEC.codec().optionalFieldOf("definitions", SkillDefinitions.EMPTY).forGetter(PassiveSkill::definitions),
                Codec.BOOL.optionalFieldOf("toggleable", false).forGetter(PassiveSkill::isToggleable),
                Codec.BOOL.optionalFieldOf("enabled_by_default", true).forGetter(PassiveSkill::isEnabledByDefault),
                PassiveSkill.Upkeep.CODEC.optionalFieldOf("upkeep").forGetter(PassiveSkill::getUpkeep),
                Identifier.CODEC.optionalFieldOf("state_group").forGetter(PassiveSkill::stateGroup),
                Identifier.CODEC.listOf().optionalFieldOf("granted_skills", List.of()).forGetter(PassiveSkill::grantedSkills),
                SkillCondition.CODEC.listOf().optionalFieldOf("enable_conditions", List.of()).forGetter(PassiveSkill::enableConditions),
                SkillAction.CODEC.listOf().optionalFieldOf("on_enable", List.of()).forGetter(PassiveSkill::enableActions),
                SkillAction.CODEC.listOf().optionalFieldOf("on_disable", List.of()).forGetter(PassiveSkill::disableActions)
        ).apply(instance, PassiveSkill::create));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
