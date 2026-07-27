package net.zic.ascension.impl.core.targeting;

import net.zic.ascension.api.datapack.CodecType;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.targeting.SkillTarget;
import net.zic.ascension.api.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.core.targeting.TargetingContext;
import net.zic.ascension.api.core.targeting.TargetingDefinition;
import net.zic.ascension.api.core.targeting.TargetingResult;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.targeting.TargetingService;
import net.zic.ascension.impl.datapack.targeting.AscensionTargetingTypes;

public record RayTargeting(
        ScaledValue range,
        ScaledValue width,
        TargetFilterDefinition filter
) implements TargetingDefinition {
    public static final MapCodec<RayTargeting> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("range").forGetter(RayTargeting::range),
            ScaledValue.CODEC.codec().optionalFieldOf("width", ScaledValue.constant(0.25D))
                    .forGetter(RayTargeting::width),
            TargetFilterDefinition.CODEC.codec().optionalFieldOf("filter", TargetFilterDefinition.hostile())
                    .forGetter(RayTargeting::filter)
    ).apply(instance, RayTargeting::new));

    @Override
    public CodecType<TargetingDefinition> getType() {
        return AscensionTargetingTypes.RAY.get();
    }

    @Override
    public TargetingResult resolve(TargetingContext context) {
        SkillTarget target = TargetingService.ray(
                context.level(),
                context.caster(),
                range.resolve(context.scaledValueContext(null)),
                width.resolve(context.scaledValueContext(null)),
                filter
        );
        return target == null ? TargetingResult.success(java.util.List.of()) : TargetingResult.success(target);
    }
}
