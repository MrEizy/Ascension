package net.zic.ascension.impl.core.movement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.movement.MovementCollisionPolicy;
import net.zic.ascension.api.core.movement.MovementContext;
import net.zic.ascension.api.core.movement.MovementDefinition;
import net.zic.ascension.api.core.movement.MovementDirection;
import net.zic.ascension.api.core.movement.MovementFailureReason;
import net.zic.ascension.api.core.movement.MovementResult;
import net.zic.ascension.api.core.movement.MovementType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.movement.MovementService;
import net.zic.ascension.impl.datapack.movement.AscensionMovementTypes;

public record DirectionalMovement(
        ScaledValue distance,
        MovementDirection direction,
        boolean includeVertical,
        MovementCollisionPolicy collision,
        boolean preserveVelocity
) implements MovementDefinition {
    public static final MapCodec<DirectionalMovement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("distance").forGetter(DirectionalMovement::distance),
            MovementDirection.CODEC.optionalFieldOf("direction", MovementDirection.LOOK)
                    .forGetter(DirectionalMovement::direction),
            Codec.BOOL.optionalFieldOf("include_vertical", true).forGetter(DirectionalMovement::includeVertical),
            MovementCollisionPolicy.CODEC.optionalFieldOf(
                    "collision",
                    MovementCollisionPolicy.STOP_BEFORE_COLLISION
            ).forGetter(DirectionalMovement::collision),
            Codec.BOOL.optionalFieldOf("preserve_velocity", false).forGetter(DirectionalMovement::preserveVelocity)
    ).apply(instance, DirectionalMovement::new));

    @Override
    public MovementType getType() {
        return AscensionMovementTypes.DIRECTIONAL.get();
    }

    @Override
    public MovementResult execute(MovementContext context) {
        LivingEntity mover = context.mover();
        LivingEntity target = context.execution().target();
        Vec3 resolvedDirection = switch (direction) {
            case LOOK -> mover.getLookAngle();
            case TOWARD_TARGET -> target == null ? null : target.position().subtract(mover.position());
            case AWAY_FROM_TARGET -> target == null ? null : mover.position().subtract(target.position());
        };
        if (resolvedDirection == null) {
            return MovementResult.failure(
                    mover.position(),
                    mover.position(),
                    MovementFailureReason.MISSING_TARGET
            );
        }
        if (!includeVertical) {
            resolvedDirection = new Vec3(resolvedDirection.x, 0.0D, resolvedDirection.z);
        }
        if (resolvedDirection.lengthSqr() <= 1.0E-8D) {
            return MovementResult.failure(
                    mover.position(),
                    mover.position(),
                    MovementFailureReason.INVALID_DIRECTION
            );
        }
        double resolvedDistance = distance.resolve(context.execution().scaledValueContext());
        if (!Double.isFinite(resolvedDistance) || resolvedDistance <= 0.0D) {
            return MovementResult.failure(
                    mover.position(),
                    mover.position(),
                    MovementFailureReason.NO_MOVEMENT
            );
        }
        Vec3 destination = mover.position().add(resolvedDirection.normalize().scale(resolvedDistance));
        return MovementService.move(
                context.level(),
                mover,
                destination,
                collision,
                preserveVelocity
        );
    }
}
