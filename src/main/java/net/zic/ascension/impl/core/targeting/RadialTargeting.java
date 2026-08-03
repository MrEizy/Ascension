package net.zic.ascension.impl.core.targeting;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.ascension.core.targeting.TargetSort;
import net.zic.ascension.api.ascension.core.targeting.TargetingContext;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.core.targeting.TargetingResult;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.datapack.targeting.AscensionTargetingTypes;

public record RadialTargeting(
        ScaledValue radius,
        int maximumTargets,
        TargetSort sort,
        TargetFilterDefinition filter
) implements TargetingDefinition {
    public static final MapCodec<RadialTargeting> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("radius").forGetter(RadialTargeting::radius),
            Codec.intRange(0, 1024).optionalFieldOf("maximum_targets", 0).forGetter(RadialTargeting::maximumTargets),
            TargetSort.CODEC.optionalFieldOf("sort", TargetSort.NEAREST).forGetter(RadialTargeting::sort),
            TargetFilterDefinition.CODEC.codec().optionalFieldOf("filter", TargetFilterDefinition.hostile())
                    .forGetter(RadialTargeting::filter)
    ).apply(instance, RadialTargeting::new));

    @Override
    public CodecType<TargetingDefinition> getType() {
        return AscensionTargetingTypes.RADIAL.get();
    }

    @Override
    public TargetingResult resolve(TargetingContext context) {
        double resolvedRadius = radius.resolve(context.scaledValueContext(null));
        Vec3 center = context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D);
        return TargetingResult.success(TargetingService.radial(
                context.level(),
                context.caster(),
                center,
                resolvedRadius,
                filter,
                sort,
                maximumTargets
        ));
    }
}
