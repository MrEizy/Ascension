package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;
import net.zic.ascension.impl.runtime.visual.RuntimeVisualSync;

import java.util.List;
import java.util.UUID;

public record RuntimeVisualFeature(
        Identifier visual,
        ScaledValue duration,
        Position position,
        boolean follow,
        boolean rotateWithSubject,
        Vec3 offset,
        PointMode points,
        ScaledValue progress,
        ScaledValue primary,
        ScaledValue secondary,
        int stage
) implements SkillExecutionFeature {
    public static final MapCodec<RuntimeVisualFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("visual").forGetter(RuntimeVisualFeature::visual),
            ScaledValue.CODEC.codec().optionalFieldOf("duration", ScaledValue.constant(20.0D))
                    .forGetter(RuntimeVisualFeature::duration),
            Position.CODEC.optionalFieldOf("position", Position.EXECUTION).forGetter(RuntimeVisualFeature::position),
            Codec.BOOL.optionalFieldOf("follow", false).forGetter(RuntimeVisualFeature::follow),
            Codec.BOOL.optionalFieldOf("rotate_with_subject", false)
                    .forGetter(RuntimeVisualFeature::rotateWithSubject),
            CodecHelpers.VEC3.optionalFieldOf("offset", Vec3.ZERO).forGetter(RuntimeVisualFeature::offset),
            PointMode.CODEC.optionalFieldOf("points", PointMode.NONE).forGetter(RuntimeVisualFeature::points),
            ScaledValue.CODEC.codec().optionalFieldOf("progress", ScaledValue.constant(0.0D))
                    .forGetter(RuntimeVisualFeature::progress),
            ScaledValue.CODEC.codec().optionalFieldOf("primary", ScaledValue.constant(0.0D))
                    .forGetter(RuntimeVisualFeature::primary),
            ScaledValue.CODEC.codec().optionalFieldOf("secondary", ScaledValue.constant(0.0D))
                    .forGetter(RuntimeVisualFeature::secondary),
            Codec.intRange(0, 1024).optionalFieldOf("stage", 0).forGetter(RuntimeVisualFeature::stage)
    ).apply(instance, RuntimeVisualFeature::new));

    public RuntimeVisualFeature {
        position = position == null ? Position.EXECUTION : position;
        offset = offset == null ? Vec3.ZERO : offset;
        points = points == null ? PointMode.NONE : points;
        stage = Math.max(0, stage);
    }

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.RUNTIME_VISUAL.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        LivingEntity subject = switch (position) {
            case CASTER -> context.caster();
            case TARGET -> context.target();
            case EXECUTION -> null;
        };
        Vec3 basePosition = subject == null
                ? context.position()
                : subject.position().add(0.0D, subject.getBbHeight() * 0.5D, 0.0D);
        UUID runtimeId = UUID.randomUUID();
        long lifetime = Math.max(1L, Math.round(duration.resolve(context.scaledValueContext())));
        double resolvedProgress = Math.clamp(progress.resolve(context.scaledValueContext()), 0.0D, 1.0D);
        double resolvedPrimary = primary.resolve(context.scaledValueContext());
        double resolvedSecondary = secondary.resolve(context.scaledValueContext());
        List<Vec3> resolvedPoints = points.resolve(context);
        List<RuntimeVisualState.Link> links = resolvedPoints.size() >= 2
                ? List.of(new RuntimeVisualState.Link(0, 1))
                : List.of();
        int flags = follow && subject != null
                ? RuntimeVisualState.OWNER_RELATIVE
                | (rotateWithSubject ? RuntimeVisualState.ROTATE_WITH_OWNER : 0)
                : 0;
        RuntimeVisualSync.spawn(context.level(), new RuntimeVisualState(
                runtimeId,
                visual,
                subject == null ? context.caster().getUUID() : subject.getUUID(),
                basePosition.add(follow && subject != null ? Vec3.ZERO : offset),
                follow && subject != null ? offset : Vec3.ZERO,
                resolvedPoints,
                links,
                context.level().getGameTime() + lifetime,
                stage,
                flags,
                (float) resolvedProgress,
                runtimeId.getMostSignificantBits(),
                resolvedPrimary,
                resolvedSecondary
        ));
    }

    public enum Position implements StringRepresentable {
        EXECUTION("execution"),
        CASTER("caster"),
        TARGET("target");

        public static final Codec<Position> CODEC = StringRepresentable.fromEnum(Position::values);
        private final String name;

        Position(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum PointMode implements StringRepresentable {
        NONE("none"),
        CASTER_TO_EXECUTION("caster_to_execution"),
        CASTER_TO_TARGET("caster_to_target");

        public static final Codec<PointMode> CODEC = StringRepresentable.fromEnum(PointMode::values);
        private final String name;

        PointMode(String name) {
            this.name = name;
        }

        public List<Vec3> resolve(SkillExecutionContext context) {
            Vec3 caster = context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D);
            return switch (this) {
                case NONE -> List.of();
                case CASTER_TO_EXECUTION -> List.of(caster, context.position());
                case CASTER_TO_TARGET -> context.target() == null
                        ? List.of(caster, context.position())
                        : List.of(caster, context.target().getBoundingBox().getCenter());
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
