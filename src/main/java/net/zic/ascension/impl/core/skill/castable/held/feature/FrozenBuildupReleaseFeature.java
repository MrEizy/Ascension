package net.zic.ascension.impl.core.skill.castable.held.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.common.effect.frozen.FrozenStateService;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastReleaseFeatureTypes;

public record FrozenBuildupReleaseFeature(ScaledValue amount, int decayDelay) implements HeldCastReleaseFeature {
    public static final MapCodec<FrozenBuildupReleaseFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("amount").forGetter(FrozenBuildupReleaseFeature::amount),
            com.mojang.serialization.Codec.INT.optionalFieldOf("decay_delay", 40).forGetter(FrozenBuildupReleaseFeature::decayDelay)
    ).apply(instance, FrozenBuildupReleaseFeature::new));

    @Override
    public HeldCastReleaseFeatureType getType() {
        return AscensionHeldCastReleaseFeatureTypes.FROZEN_BUILDUP.get();
    }

    @Override
    public void apply(HeldCastReleaseContext context) {
        FrozenStateService.apply(context.target(), amount.resolve(context.scaledValueContext()), decayDelay);
    }
}
