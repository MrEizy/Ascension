package net.zic.ascension.common.projectile;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.projectile.ProjectileLaunchDirection;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.impl.core.projectile.VirtualProjectileManager;

import java.util.UUID;

public final class VirtualProjectileService {
    private VirtualProjectileService() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definition,
            ProjectileLaunchDirection direction
    ) {
        return VirtualProjectileManager.spawn(context, definition, direction);
    }

    public static boolean remove(UUID runtimeId) {
        return VirtualProjectileManager.remove(runtimeId);
    }
}
