package net.zic.ascension.api.ascension.core.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.Map;

public record ResourceDefinition(
        ScaledValue maximum,
        ScaledValue starting,
        ScaledValue regeneration,
        int regenerationInterval
) {
    public static final Codec<ResourceDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.optionalFieldOf("maximum", ScaledValue.constant(100.0D)).forGetter(ResourceDefinition::maximum),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("starting", ScaledValue.constant(0.0D)).forGetter(ResourceDefinition::starting),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("regeneration", ScaledValue.constant(0.0D)).forGetter(ResourceDefinition::regeneration),
            Codec.intRange(1, 72000).optionalFieldOf("regeneration_interval", 20).forGetter(ResourceDefinition::regenerationInterval)
    ).apply(instance, ResourceDefinition::new));

    public double maximum(LivingEntity entity, Identifier resource) {
        return Math.max(0.0D, resolve(maximum, entity, resource));
    }

    public double starting(LivingEntity entity, Identifier resource) {
        return Math.clamp(resolve(starting, entity, resource), 0.0D, maximum(entity, resource));
    }

    public double regeneration(LivingEntity entity, Identifier resource) {
        return Math.max(0.0D, resolve(regeneration, entity, resource));
    }

    private static double resolve(ScaledValue value, LivingEntity entity, Identifier resource) {
        if (value == null || entity == null) {
            return 0.0D;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(entity);
        return value.resolve(new ScaledValue.Context(source, resource, entity, null, 0.0D, Map.of()));
    }
}
