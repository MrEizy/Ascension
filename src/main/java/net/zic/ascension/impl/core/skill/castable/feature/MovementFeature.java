package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.datapack.CodecType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.movement.MovementService;
import net.zic.ascension.impl.core.movement.MovementAnchorContainer;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

import java.util.Optional;

public record MovementFeature(
        Mode mode,
        Subject subject,
        ScaledValue distance,
        Direction direction,
        boolean includeVertical,
        ScaledValue maximumDistance,
        ScaledValue stoppingDistance,
        ScaledValue verticalOffset,
        Optional<Identifier> anchor,
        boolean consumeAnchor,
        boolean restoreRotation,
        MovementService.CollisionPolicy collision,
        boolean preserveVelocity
) implements SkillExecutionFeature {
    public static final MapCodec<MovementFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Mode.CODEC.fieldOf("mode").forGetter(MovementFeature::mode),
            Subject.CODEC.optionalFieldOf("subject", Subject.CASTER).forGetter(MovementFeature::subject),
            ScaledValue.CODEC.codec().optionalFieldOf("distance", ScaledValue.constant(0.0D))
                    .forGetter(MovementFeature::distance),
            Direction.CODEC.optionalFieldOf("direction", Direction.LOOK).forGetter(MovementFeature::direction),
            Codec.BOOL.optionalFieldOf("include_vertical", true).forGetter(MovementFeature::includeVertical),
            ScaledValue.CODEC.codec().optionalFieldOf("maximum_distance", ScaledValue.constant(Double.MAX_VALUE))
                    .forGetter(MovementFeature::maximumDistance),
            ScaledValue.CODEC.codec().optionalFieldOf("stopping_distance", ScaledValue.constant(0.0D))
                    .forGetter(MovementFeature::stoppingDistance),
            ScaledValue.CODEC.codec().optionalFieldOf("vertical_offset", ScaledValue.constant(0.0D))
                    .forGetter(MovementFeature::verticalOffset),
            Identifier.CODEC.optionalFieldOf("anchor").forGetter(MovementFeature::anchor),
            Codec.BOOL.optionalFieldOf("consume_anchor", true).forGetter(MovementFeature::consumeAnchor),
            Codec.BOOL.optionalFieldOf("restore_rotation", true).forGetter(MovementFeature::restoreRotation),
            MovementService.CollisionPolicy.CODEC.optionalFieldOf(
                    "collision",
                    MovementService.CollisionPolicy.STOP_BEFORE_COLLISION
            ).forGetter(MovementFeature::collision),
            Codec.BOOL.optionalFieldOf("preserve_velocity", false).forGetter(MovementFeature::preserveVelocity)
    ).apply(instance, MovementFeature::new));

    public MovementFeature {
        anchor = anchor == null ? Optional.empty() : anchor;
    }

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.MOVEMENT.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        LivingEntity mover = subject == Subject.CASTER ? context.caster() : context.target();
        if (mover == null) {
            return;
        }
        switch (mode) {
            case DIRECTIONAL -> moveDirectional(context, mover);
            case TARGET_POSITION -> moveToPosition(context, mover);
            case ANCHOR -> moveToAnchor(context, mover);
        }
    }

    private void moveDirectional(SkillExecutionContext context, LivingEntity mover) {
        LivingEntity target = context.target();
        Vec3 resolvedDirection = switch (direction) {
            case LOOK -> mover.getLookAngle();
            case TOWARD_TARGET -> target == null ? null : target.position().subtract(mover.position());
            case AWAY_FROM_TARGET -> target == null ? null : mover.position().subtract(target.position());
        };
        if (resolvedDirection == null) {
            return;
        }
        if (!includeVertical) {
            resolvedDirection = new Vec3(resolvedDirection.x, 0.0D, resolvedDirection.z);
        }
        double resolvedDistance = distance.resolve(context.scaledValueContext());
        if (resolvedDirection.lengthSqr() <= 1.0E-8D
                || !Double.isFinite(resolvedDistance)
                || resolvedDistance <= 0.0D) {
            return;
        }
        MovementService.move(
                context.level(),
                mover,
                mover.position().add(resolvedDirection.normalize().scale(resolvedDistance)),
                collision,
                preserveVelocity
        );
    }

    private void moveToPosition(SkillExecutionContext context, LivingEntity mover) {
        Vec3 targetPosition = context.position();
        Vec3 delta = targetPosition.subtract(mover.position());
        double length = delta.length();
        double maximum = Math.max(0.0D, maximumDistance.resolve(context.scaledValueContext()));
        double stopping = Math.max(0.0D, stoppingDistance.resolve(context.scaledValueContext()));
        if (length <= stopping || length <= 1.0E-8D) {
            return;
        }
        double travelled = Math.min(length - stopping, maximum);
        Vec3 destination = mover.position()
                .add(delta.normalize().scale(travelled))
                .add(0.0D, verticalOffset.resolve(context.scaledValueContext()), 0.0D);
        MovementService.move(context.level(), mover, destination, collision, preserveVelocity);
    }

    private void moveToAnchor(SkillExecutionContext context, LivingEntity mover) {
        if (anchor.isEmpty()) {
            return;
        }
        MovementAnchorContainer.Anchor resolved = MovementService.getAnchor(mover, anchor.get());
        if (resolved == null || !mover.level().dimension().identifier().equals(resolved.dimension())) {
            return;
        }
        MovementService.Result result = MovementService.move(
                context.level(),
                mover,
                resolved.position(),
                collision,
                preserveVelocity
        );
        if (!result.succeeded()) {
            return;
        }
        if (restoreRotation) {
            mover.setYRot(resolved.yaw());
            mover.setXRot(resolved.pitch());
        }
        if (consumeAnchor) {
            MovementService.removeAnchor(mover, anchor.get());
        }
    }

    public enum Mode implements StringRepresentable {
        DIRECTIONAL("directional"),
        TARGET_POSITION("target_position"),
        ANCHOR("anchor");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Subject implements StringRepresentable {
        CASTER("caster"),
        TARGET("target");

        public static final Codec<Subject> CODEC = StringRepresentable.fromEnum(Subject::values);

        private final String name;

        Subject(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Direction implements StringRepresentable {
        LOOK("look"),
        TOWARD_TARGET("toward_target"),
        AWAY_FROM_TARGET("away_from_target");

        public static final Codec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
