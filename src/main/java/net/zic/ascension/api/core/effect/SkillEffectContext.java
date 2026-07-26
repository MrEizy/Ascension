package net.zic.ascension.api.core.effect;

import net.minecraft.resources.Identifier;

import java.util.UUID;

public interface SkillEffectContext {
    Identifier definition();

    UUID instanceId();

    UUID sourceEntity();

    Identifier sourceSkill();

    int remainingDuration();

    double potency();

    int stacks();
}
