package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.effect.SkillEffectService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record SkillEffectFeature(
        Identifier effect,
        ScaledValue duration,
        ScaledValue potency
) implements SkillExecutionFeature {
    public static final MapCodec<SkillEffectFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(SkillEffectFeature::effect),
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(SkillEffectFeature::duration),
            ScaledValue.CODEC.codec().optionalFieldOf("potency", ScaledValue.constant(1.0D))
                    .forGetter(SkillEffectFeature::potency)
    ).apply(instance, SkillEffectFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.SKILL_EFFECT.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        if (context.target() == null) {
            return;
        }
        int resolvedDuration = Math.max(1, (int) Math.round(duration.resolve(context.scaledValueContext())));
        double resolvedPotency = Math.max(0.0D, potency.resolve(context.scaledValueContext()));
        SkillEffectService.apply(
                context.target(),
                effect,
                context.caster().getUUID(),
                context.skill(),
                resolvedDuration,
                resolvedPotency
        );
    }
}
