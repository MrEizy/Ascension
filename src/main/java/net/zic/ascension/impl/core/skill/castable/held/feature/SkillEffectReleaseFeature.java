package net.zic.ascension.impl.core.skill.castable.held.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.effect.SkillEffectService;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastReleaseFeatureTypes;

public record SkillEffectReleaseFeature(Identifier effect, ScaledValue duration, ScaledValue potency) implements HeldCastReleaseFeature {
    public static final MapCodec<SkillEffectReleaseFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(SkillEffectReleaseFeature::effect),
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(SkillEffectReleaseFeature::duration),
            ScaledValue.CODEC.codec().optionalFieldOf("potency", ScaledValue.constant(1.0D)).forGetter(SkillEffectReleaseFeature::potency)
    ).apply(instance, SkillEffectReleaseFeature::new));

    @Override
    public HeldCastReleaseFeatureType getType() {
        return AscensionHeldCastReleaseFeatureTypes.SKILL_EFFECT.get();
    }

    @Override
    public void apply(HeldCastReleaseContext context) {
        int resolvedDuration = Math.max(1, (int) Math.round(duration.resolve(context.scaledValueContext())));
        double resolvedPotency = Math.max(0.0D, potency.resolve(context.scaledValueContext()));
        SkillEffectService.apply(context.target(), effect, context.execution().caster().getUUID(), context.execution().skill(), resolvedDuration, resolvedPotency);
    }
}
