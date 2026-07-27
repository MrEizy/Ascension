package net.zic.ascension.impl.core.movement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.movement.MovementAnchor;
import net.zic.ascension.api.core.movement.MovementCollisionPolicy;
import net.zic.ascension.api.core.movement.MovementContext;
import net.zic.ascension.api.core.movement.MovementDefinition;
import net.zic.ascension.api.core.movement.MovementFailureReason;
import net.zic.ascension.api.core.movement.MovementResult;
import net.zic.ascension.api.core.movement.MovementType;
import net.zic.ascension.common.movement.MovementService;
import net.zic.ascension.impl.datapack.movement.AscensionMovementTypes;

public record AnchorMovement(
        Identifier anchor,
        boolean consume,
        boolean restoreRotation,
        MovementCollisionPolicy collision,
        boolean preserveVelocity
) implements MovementDefinition {
    public static final MapCodec<AnchorMovement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("anchor").forGetter(AnchorMovement::anchor),
            Codec.BOOL.optionalFieldOf("consume", true).forGetter(AnchorMovement::consume),
            Codec.BOOL.optionalFieldOf("restore_rotation", true).forGetter(AnchorMovement::restoreRotation),
            MovementCollisionPolicy.CODEC.optionalFieldOf("collision", MovementCollisionPolicy.FAIL)
                    .forGetter(AnchorMovement::collision),
            Codec.BOOL.optionalFieldOf("preserve_velocity", false).forGetter(AnchorMovement::preserveVelocity)
    ).apply(instance, AnchorMovement::new));

    @Override
    public MovementType getType() {
        return AscensionMovementTypes.ANCHOR.get();
    }

    @Override
    public MovementResult execute(MovementContext context) {
        LivingEntity mover = context.mover();
        MovementAnchor resolved = MovementService.getAnchor(mover, anchor);
        if (resolved == null) {
            return MovementResult.failure(
                    mover.position(),
                    mover.position(),
                    MovementFailureReason.MISSING_ANCHOR
            );
        }
        if (!mover.level().dimension().identifier().equals(resolved.dimension())) {
            return MovementResult.failure(
                    mover.position(),
                    resolved.position(),
                    MovementFailureReason.WRONG_DIMENSION
            );
        }

        MovementResult result = MovementService.move(
                context.level(),
                mover,
                resolved.position(),
                collision,
                preserveVelocity
        );
        if (result.succeeded()) {
            if (restoreRotation) {
                mover.setYRot(resolved.yaw());
                mover.setXRot(resolved.pitch());
            }
            if (consume) {
                MovementService.removeAnchor(mover, anchor);
            }
        }
        return result;
    }
}
