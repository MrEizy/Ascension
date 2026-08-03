package net.zic.ascension.api.ascension.core.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;

public record ProjectileBehaviorContext(
        ServerLevel level,
        ServerPlayer owner,
        LivingEntity target,
        VirtualProjectileAccess projectile,
        SkillExecutionContext execution
) {
}
