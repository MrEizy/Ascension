package net.zic.ascension.api.core.skill.castable.held;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.api.value.ScaledValueContext;

public record HeldCastMovementDefinition(
        ScaledValue horizontalDrag,
        ScaledValue verticalDrag,
        boolean disableSprinting
) {
    public static final MapCodec<HeldCastMovementDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().optionalFieldOf("horizontal_drag", ScaledValue.constant(1.0D))
                    .forGetter(HeldCastMovementDefinition::horizontalDrag),
            ScaledValue.CODEC.codec().optionalFieldOf("vertical_drag", ScaledValue.constant(1.0D))
                    .forGetter(HeldCastMovementDefinition::verticalDrag),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("disable_sprinting", false)
                    .forGetter(HeldCastMovementDefinition::disableSprinting)
    ).apply(instance, HeldCastMovementDefinition::new));

    public static HeldCastMovementDefinition defaults() {
        return new HeldCastMovementDefinition(
                ScaledValue.constant(1.0D),
                ScaledValue.constant(1.0D),
                false
        );
    }

    public void apply(LivingEntity caster, ScaledValueContext context) {
        if (caster == null) {
            return;
        }

        double horizontal = sanitizeMultiplier(horizontalDrag.resolve(context), 1.0D);
        double vertical = sanitizeMultiplier(verticalDrag.resolve(context), 1.0D);
        Vec3 movement = caster.getDeltaMovement();
        caster.setDeltaMovement(movement.x * horizontal, movement.y * vertical, movement.z * horizontal);
        if (disableSprinting) {
            caster.setSprinting(false);
        }
    }

    private static double sanitizeMultiplier(double value, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, 0.0D, 1.0D) : fallback;
    }
}
