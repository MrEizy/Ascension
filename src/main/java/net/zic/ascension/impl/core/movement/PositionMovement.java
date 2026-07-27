package net.zic.ascension.impl.core.movement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.movement.MovementCollisionPolicy;
import net.zic.ascension.api.core.movement.MovementContext;
import net.zic.ascension.api.core.movement.MovementDefinition;
import net.zic.ascension.api.core.movement.MovementFailureReason;
import net.zic.ascension.api.core.movement.MovementResult;
import net.zic.ascension.api.core.movement.MovementType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.movement.MovementService;
import net.zic.ascension.impl.datapack.movement.AscensionMovementTypes;

public record PositionMovement(
        ScaledValue maximumDistance,
        ScaledValue stoppingDistance,
        ScaledValue verticalOffset,
        MovementCollisionPolicy collision,
        boolean preserveVelocity
) implements MovementDefinition {
    public static final MapCodec<PositionMovement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().optionalFieldOf(
                    "maximum_distance",
                    ScaledValue.constant(Double.MAX_VALUE)
            ).forGetter(PositionMovement::maximumDistance),
            ScaledValue.CODEC.codec().optionalFieldOf("stopping_distance", ScaledValue.constant(0.0D))
                    .forGetter(PositionMovement::stoppingDistance),
            ScaledValue.CODEC.codec().optionalFieldOf("vertical_offset", ScaledValue.constant(0.0D))
                    .forGetter(PositionMovement::verticalOffset),
            MovementCollisionPolicy.CODEC.optionalFieldOf(
                    "collision",
                    MovementCollisionPolicy.STOP_BEFORE_COLLISION
            ).forGetter(PositionMovement::collision),
            Codec.BOOL.optionalFieldOf("preserve_velocity", false).forGetter(PositionMovement::preserveVelocity)
    ).apply(instance, PositionMovement::new));

    @Override
    public MovementType getType() {
        return AscensionMovementTypes.POSITION.get();
    }

    @Override
    public MovementResult execute(MovementContext context) {
        LivingEntity mover = context.mover();
        Vec3 targetPosition = context.execution().position();
        if (targetPosition == null) {
            return MovementResult.failure(
                    mover.position(),
                    mover.position(),
                    MovementFailureReason.MISSING_TARGET
            );
        }

        Vec3 delta = targetPosition.subtract(mover.position());
        double length = delta.length();
        double maximum = Math.max(0.0D, maximumDistance.resolve(context.execution().scaledValueContext()));
        double stopping = Math.max(0.0D, stoppingDistance.resolve(context.execution().scaledValueContext()));
        if (length <= stopping) {
            return MovementResult.failure(
                    mover.position(),
                    targetPosition,
                    MovementFailureReason.NO_MOVEMENT
            );
        }

        double travelled = Math.min(length - stopping, maximum);
        Vec3 destination = mover.position().add(delta.normalize().scale(travelled));
        destination = destination.add(
                0.0D,
                verticalOffset.resolve(context.execution().scaledValueContext()),
                0.0D
        );
        return MovementService.move(
                context.level(),
                mover,
                destination,
                collision,
                preserveVelocity
        );
    }
}
