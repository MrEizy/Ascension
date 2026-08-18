package net.zic.ascension.impl.datapack.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill.Data;

import java.util.List;
import java.util.Map;

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
                SkillDefinitions.CODEC.codec().optionalFieldOf("definitions", SkillDefinitions.EMPTY).forGetter(ActiveSkill::definitions),
                ActiveCastDefinition.CODEC.optionalFieldOf("cast", ActiveCastDefinition.instant()).forGetter(ActiveSkill::cast),
                TargetingDefinition.CODEC.fieldOf("targeting").forGetter(ActiveSkill::targeting),
                Codec.BOOL.optionalFieldOf("require_targets", true).forGetter(ActiveSkill::requireTargets),
                SkillAction.CODEC.listOf().optionalFieldOf("features", List.of()).forGetter(ActiveSkill::actions),
                ActiveSkillCostDefinition.CODEC.codec().listOf().optionalFieldOf("costs", List.of()).forGetter(ActiveSkill::costs),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("cooldown", ScaledValue.constant(0.0D)).forGetter(ActiveSkill::cooldown),
                SkillMasteryRank.CODEC.optionalFieldOf("base_mastery_cap", SkillMasteryRank.INITIATE).forGetter(ActiveSkill::defaultMasteryCap),
                Codec.unboundedMap(SkillMasteryRank.CODEC, Codec.DOUBLE).optionalFieldOf("mastery_requirements", Map.of())
                        .forGetter(ActiveSkill::masteryRequirements)
        ).apply(instance, ActiveSkill::new));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
