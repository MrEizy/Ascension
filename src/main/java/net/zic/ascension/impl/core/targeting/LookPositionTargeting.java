package net.zic.ascension.impl.core.targeting;

import net.zic.ascension.api.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.targeting.SkillTarget;
import net.zic.ascension.api.core.targeting.TargetingContext;
import net.zic.ascension.api.core.targeting.TargetingDefinition;
import net.zic.ascension.api.core.targeting.TargetingResult;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.targeting.TargetingService;
import net.zic.ascension.impl.datapack.targeting.AscensionTargetingTypes;

public record LookPositionTargeting(
        ScaledValue range,
        boolean includeFluids,
        boolean fallbackToMaximumRange
) implements TargetingDefinition {
    public static final MapCodec<LookPositionTargeting> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("range").forGetter(LookPositionTargeting::range),
            Codec.BOOL.optionalFieldOf("include_fluids", false).forGetter(LookPositionTargeting::includeFluids),
            Codec.BOOL.optionalFieldOf("fallback_to_maximum_range", false)
                    .forGetter(LookPositionTargeting::fallbackToMaximumRange)
    ).apply(instance, LookPositionTargeting::new));

    @Override
    public CodecType<TargetingDefinition> getType() {
        return AscensionTargetingTypes.LOOK_POSITION.get();
    }

    @Override
    public TargetingResult resolve(TargetingContext context) {
        SkillTarget target = TargetingService.lookPosition(
                context.level(),
                context.caster(),
                range.resolve(context.scaledValueContext(null)),
                includeFluids,
                fallbackToMaximumRange
        );
        return target == null ? TargetingResult.success(java.util.List.of()) : TargetingResult.success(target);
    }
}
