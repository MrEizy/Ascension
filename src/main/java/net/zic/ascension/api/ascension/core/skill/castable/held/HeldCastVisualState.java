package net.zic.ascension.api.ascension.core.skill.castable.held;

import net.minecraft.resources.Identifier;

public record HeldCastVisualState(
        Identifier skill,
        HeldCastVisualPhase phase,
        int stage,
        double charge
) {
    public HeldCastVisualState {
        stage = Math.max(0, stage);
        charge = Double.isFinite(charge) ? Math.clamp(charge, 0.0D, 1.0D) : 0.0D;
    }
}
