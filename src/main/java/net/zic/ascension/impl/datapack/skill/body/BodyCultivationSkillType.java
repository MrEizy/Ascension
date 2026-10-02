package net.zic.ascension.impl.datapack.skill.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.skill.body.BodyCultivationSkill;

import java.util.List;

public final class BodyCultivationSkillType extends SkillType {
    private static final MapCodec<BodyCultivationSkill.Data> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("tempering", 0.0D).forGetter(BodyCultivationSkill.Data::tempering)
    ).apply(instance, BodyCultivationSkill.Data::new));

    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<BodyCultivationSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(BodyCultivationSkill::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(BodyCultivationSkill::getDescription),
                net.minecraft.resources.Identifier.CODEC.fieldOf("path").forGetter(BodyCultivationSkill::path),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(BodyCultivationSkill::priority),
                BodyCultivationSkill.Stimulus.CODEC.listOf().fieldOf("stimuli").forGetter(BodyCultivationSkill::stimuli),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("maximum_tempering", ScaledValue.constant(100.0D)).forGetter(BodyCultivationSkill::maximumTempering),
                BodyCultivationSkill.Conversion.CODEC.fieldOf("conversion").forGetter(BodyCultivationSkill::conversion),
                BodyCultivationSkill.Multiplier.CODEC.listOf().optionalFieldOf("multipliers", List.of()).forGetter(BodyCultivationSkill::multipliers)
        ).apply(instance, BodyCultivationSkill::new));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
