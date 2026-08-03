package net.zic.ascension.impl.core.targeting;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.ascension.core.targeting.TargetSort;
import net.zic.ascension.api.ascension.core.targeting.TargetingContext;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.core.targeting.TargetingResult;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.datapack.targeting.AscensionTargetingTypes;

public record ConeTargeting(
        ScaledValue range,
        ScaledValue angle,
        int maximumTargets,
        TargetSort sort,
        TargetFilterDefinition filter
) implements TargetingDefinition {
    public static final MapCodec<ConeTargeting> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("range").forGetter(ConeTargeting::range),
            ScaledValue.CODEC.codec().fieldOf("angle").forGetter(ConeTargeting::angle),
            Codec.intRange(0, 1024).optionalFieldOf("maximum_targets", 0).forGetter(ConeTargeting::maximumTargets),
            TargetSort.CODEC.optionalFieldOf("sort", TargetSort.CLOSEST_TO_VIEW).forGetter(ConeTargeting::sort),
            TargetFilterDefinition.CODEC.codec().optionalFieldOf("filter", TargetFilterDefinition.hostile())
                    .forGetter(ConeTargeting::filter)
    ).apply(instance, ConeTargeting::new));

    @Override
    public CodecType<TargetingDefinition> getType() {
        return AscensionTargetingTypes.CONE.get();
    }

    @Override
    public TargetingResult resolve(TargetingContext context) {
        return TargetingResult.success(TargetingService.cone(
                context.level(),
                context.caster(),
                range.resolve(context.scaledValueContext(null)),
                angle.resolve(context.scaledValueContext(null)),
                filter,
                sort,
                maximumTargets
        ));
    }
}
