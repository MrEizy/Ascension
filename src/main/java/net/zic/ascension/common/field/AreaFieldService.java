package net.zic.ascension.common.field;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.impl.core.field.AreaFieldManager;

import java.util.UUID;

public final class AreaFieldService {
    private AreaFieldService() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definition,
            Vec3 center
    ) {
        return AreaFieldManager.spawn(context, definition, center);
    }

    public static boolean remove(UUID runtimeId) {
        return AreaFieldManager.remove(runtimeId);
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        return AreaFieldManager.remove(level, runtimeId);
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        return AreaFieldManager.removeOwned(ownerId, definitionId);
    }

    public static int removeOwned(ServerLevel level, UUID ownerId, Identifier definitionId) {
        return AreaFieldManager.removeOwned(level, ownerId, definitionId);
    }
}
