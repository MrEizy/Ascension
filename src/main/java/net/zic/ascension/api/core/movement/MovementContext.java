package net.zic.ascension.api.core.movement;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;

public record MovementContext(
        SkillExecutionContext execution,
        LivingEntity mover
) {
    public ServerLevel level() {
        return execution.level();
    }
}
