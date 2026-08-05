package net.zic.ascension.impl.core.targeting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.datapack.targeting.AscensionTargetingTypes;

public final class TargetingDefinitions {
    private TargetingDefinitions() {
    }

    public record Self() implements TargetingDefinition {
        public static final MapCodec<Self> CODEC = MapCodec.unit(Self::new);

        @Override
        public CodecType<TargetingDefinition> getType() {
            return AscensionTargetingTypes.SELF.get();
        }

        @Override
        public TargetingDefinition.Result resolve(TargetingDefinition.Context context) {
            return TargetingDefinition.Result.success(TargetingDefinition.Target.entity(context.caster()));
        }
    }

    public record Ray(
            ScaledValue range,
            ScaledValue width,
            TargetingDefinition.Filter filter
    ) implements TargetingDefinition {
        public static final MapCodec<Ray> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("range").forGetter(Ray::range),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("width", ScaledValue.constant(0.25D)).forGetter(Ray::width),
                TargetingDefinition.Filter.CODEC.codec().optionalFieldOf("filter", TargetingDefinition.Filter.hostile()).forGetter(Ray::filter)
        ).apply(instance, Ray::new));

        @Override
        public CodecType<TargetingDefinition> getType() {
            return AscensionTargetingTypes.RAY.get();
        }

        @Override
        public TargetingDefinition.Result resolve(TargetingDefinition.Context context) {
            TargetingDefinition.Target target = TargetingService.ray(
                    context.level(),
                    context.caster(),
                    range.resolve(context.scaledValueContext(null)),
                    width.resolve(context.scaledValueContext(null)),
                    filter
            );
            return target == null ? TargetingDefinition.Result.success(java.util.List.of()) : TargetingDefinition.Result.success(target);
        }
    }

    public record Cone(
            ScaledValue range,
            ScaledValue angle,
            int maximumTargets,
            TargetingDefinition.Sort sort,
            TargetingDefinition.Filter filter
    ) implements TargetingDefinition {
        public static final MapCodec<Cone> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("range").forGetter(Cone::range),
                ScaledValue.COMPACT_CODEC.fieldOf("angle").forGetter(Cone::angle),
                Codec.intRange(0, 1024).optionalFieldOf("maximum_targets", 0).forGetter(Cone::maximumTargets),
                TargetingDefinition.Sort.CODEC.optionalFieldOf("sort", TargetingDefinition.Sort.CLOSEST_TO_VIEW).forGetter(Cone::sort),
                TargetingDefinition.Filter.CODEC.codec().optionalFieldOf("filter", TargetingDefinition.Filter.hostile()).forGetter(Cone::filter)
        ).apply(instance, Cone::new));

        @Override
        public CodecType<TargetingDefinition> getType() {
            return AscensionTargetingTypes.CONE.get();
        }

        @Override
        public TargetingDefinition.Result resolve(TargetingDefinition.Context context) {
            return TargetingDefinition.Result.success(TargetingService.cone(
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

    public record Radial(
            ScaledValue radius,
            int maximumTargets,
            TargetingDefinition.Sort sort,
            TargetingDefinition.Filter filter
    ) implements TargetingDefinition {
        public static final MapCodec<Radial> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("radius").forGetter(Radial::radius),
                Codec.intRange(0, 1024).optionalFieldOf("maximum_targets", 0).forGetter(Radial::maximumTargets),
                TargetingDefinition.Sort.CODEC.optionalFieldOf("sort", TargetingDefinition.Sort.NEAREST).forGetter(Radial::sort),
                TargetingDefinition.Filter.CODEC.codec().optionalFieldOf("filter", TargetingDefinition.Filter.hostile()).forGetter(Radial::filter)
        ).apply(instance, Radial::new));

        @Override
        public CodecType<TargetingDefinition> getType() {
            return AscensionTargetingTypes.RADIAL.get();
        }

        @Override
        public TargetingDefinition.Result resolve(TargetingDefinition.Context context) {
            Vec3 center = context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D);
            return TargetingDefinition.Result.success(TargetingService.radial(
                    context.level(),
                    context.caster(),
                    center,
                    radius.resolve(context.scaledValueContext(null)),
                    filter,
                    sort,
                    maximumTargets
            ));
        }
    }

    public record LookPosition(
            ScaledValue range,
            boolean includeFluids,
            boolean fallbackToMaximumRange
    ) implements TargetingDefinition {
        public static final MapCodec<LookPosition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("range").forGetter(LookPosition::range),
                Codec.BOOL.optionalFieldOf("include_fluids", false).forGetter(LookPosition::includeFluids),
                Codec.BOOL.optionalFieldOf("fallback_to_maximum_range", false).forGetter(LookPosition::fallbackToMaximumRange)
        ).apply(instance, LookPosition::new));

        @Override
        public CodecType<TargetingDefinition> getType() {
            return AscensionTargetingTypes.LOOK_POSITION.get();
        }

        @Override
        public TargetingDefinition.Result resolve(TargetingDefinition.Context context) {
            TargetingDefinition.Target target = TargetingService.lookPosition(
                    context.level(),
                    context.caster(),
                    range.resolve(context.scaledValueContext(null)),
                    includeFluids,
                    fallbackToMaximumRange
            );
            return target == null ? TargetingDefinition.Result.success(java.util.List.of()) : TargetingDefinition.Result.success(target);
        }
    }
}
