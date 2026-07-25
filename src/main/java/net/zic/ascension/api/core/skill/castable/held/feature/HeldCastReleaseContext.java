package net.zic.ascension.api.core.skill.castable.held.feature;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.value.ScaledValueContext;

public record HeldCastReleaseContext(
        HeldCastExecutionContext execution,
        LivingEntity target,
        Vec3 position
) {
    public ScaledValueContext scaledValueContext() {
        return execution.scaledValueContext(target);
    }
}
