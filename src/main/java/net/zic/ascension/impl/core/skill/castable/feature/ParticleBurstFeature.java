package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record ParticleBurstFeature(
        Identifier particle,
        ScaledValue count,
        ScaledValue spread,
        ScaledValue speed
) implements SkillExecutionFeature {
    public static final MapCodec<ParticleBurstFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("particle").forGetter(ParticleBurstFeature::particle),
            ScaledValue.CODEC.codec().optionalFieldOf("count", ScaledValue.constant(12.0D))
                    .forGetter(ParticleBurstFeature::count),
            ScaledValue.CODEC.codec().optionalFieldOf("spread", ScaledValue.constant(0.5D))
                    .forGetter(ParticleBurstFeature::spread),
            ScaledValue.CODEC.codec().optionalFieldOf("speed", ScaledValue.constant(0.05D))
                    .forGetter(ParticleBurstFeature::speed)
    ).apply(instance, ParticleBurstFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.PARTICLE_BURST.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(particle);
        if (!(type instanceof SimpleParticleType simple)) {
            return;
        }
        int resolvedCount = Math.clamp((int) Math.round(count.resolve(context.scaledValueContext())), 0, 512);
        double resolvedSpread = Math.clamp(spread.resolve(context.scaledValueContext()), 0.0D, 16.0D);
        double resolvedSpeed = Math.clamp(speed.resolve(context.scaledValueContext()), 0.0D, 4.0D);
        context.level().sendParticles(
                simple,
                context.position().x,
                context.position().y,
                context.position().z,
                resolvedCount,
                resolvedSpread,
                resolvedSpread,
                resolvedSpread,
                resolvedSpeed
        );
    }
}
