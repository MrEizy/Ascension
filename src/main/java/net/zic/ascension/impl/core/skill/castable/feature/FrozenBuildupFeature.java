package net.zic.ascension.impl.core.skill.castable.feature;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.effect.frozen.FrozenStateService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record FrozenBuildupFeature(ScaledValue amount, int decayDelay) implements SkillExecutionFeature {
    public static final MapCodec<FrozenBuildupFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("amount").forGetter(FrozenBuildupFeature::amount),
            Codec.INT.optionalFieldOf("decay_delay", 40).forGetter(FrozenBuildupFeature::decayDelay)
    ).apply(instance, FrozenBuildupFeature::new));

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.FROZEN_BUILDUP.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        if (context.target() == null) {
            return;
        }
        FrozenStateService.apply(
                context.target(),
                amount.resolve(context.scaledValueContext()),
                decayDelay
        );
    }
}
