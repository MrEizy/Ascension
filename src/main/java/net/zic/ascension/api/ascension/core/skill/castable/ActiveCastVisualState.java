package net.zic.ascension.api.ascension.core.skill.castable;

import net.minecraft.resources.Identifier;

public record ActiveCastVisualState(Identifier skill, int stage, double progress) {
    public ActiveCastVisualState {
        stage = Math.max(0, stage);
        progress = Double.isFinite(progress) ? Math.clamp(progress, 0.0D, 1.0D) : 0.0D;
    }
}
