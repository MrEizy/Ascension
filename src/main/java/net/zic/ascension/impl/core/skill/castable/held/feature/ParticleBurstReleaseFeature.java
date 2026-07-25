package net.zic.ascension.impl.core.skill.castable.held.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastReleaseFeatureTypes;

public record ParticleBurstReleaseFeature(
        Identifier particle,
        ScaledValue count,
        ScaledValue spread,
        ScaledValue speed
) implements HeldCastReleaseFeature {
    public static final MapCodec<ParticleBurstReleaseFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("particle").forGetter(ParticleBurstReleaseFeature::particle),
            ScaledValue.CODEC.codec().optionalFieldOf("count", ScaledValue.constant(12.0D))
                    .forGetter(ParticleBurstReleaseFeature::count),
            ScaledValue.CODEC.codec().optionalFieldOf("spread", ScaledValue.constant(0.5D))
                    .forGetter(ParticleBurstReleaseFeature::spread),
            ScaledValue.CODEC.codec().optionalFieldOf("speed", ScaledValue.constant(0.05D))
                    .forGetter(ParticleBurstReleaseFeature::speed)
    ).apply(instance, ParticleBurstReleaseFeature::new));

    @Override
    public HeldCastReleaseFeatureType getType() {
        return AscensionHeldCastReleaseFeatureTypes.PARTICLE_BURST.get();
    }

    @Override
    public void apply(HeldCastReleaseContext context) {
        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(particle);
        if (!(type instanceof SimpleParticleType simple)) {
            return;
        }
        int resolvedCount = Math.clamp((int) Math.round(count.resolve(context.scaledValueContext())), 0, 512);
        double resolvedSpread = Math.clamp(spread.resolve(context.scaledValueContext()), 0.0D, 16.0D);
        double resolvedSpeed = Math.clamp(speed.resolve(context.scaledValueContext()), 0.0D, 4.0D);
        context.execution().level().sendParticles(
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
