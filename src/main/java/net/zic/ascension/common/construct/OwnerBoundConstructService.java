package net.zic.ascension.common.construct;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.zic.ascension.api.core.construct.OwnerBoundConstructView;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.impl.core.construct.OwnerBoundConstructManager;

import java.util.List;
import java.util.UUID;

public final class OwnerBoundConstructService {
    private OwnerBoundConstructService() {
    }

    public static UUID spawn(SkillExecutionContext context, Identifier definition) {
        return OwnerBoundConstructManager.spawn(context, definition);
    }

    public static boolean remove(UUID runtimeId) {
        return OwnerBoundConstructManager.remove(runtimeId);
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        return OwnerBoundConstructManager.remove(level, runtimeId);
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        return OwnerBoundConstructManager.removeOwned(ownerId, definitionId);
    }

    public static int removeOwned(ServerLevel level, UUID ownerId, Identifier definitionId) {
        return OwnerBoundConstructManager.removeOwned(level, ownerId, definitionId);
    }

    public static double modifyStability(UUID runtimeId, double amount) {
        return OwnerBoundConstructManager.modifyStability(runtimeId, amount);
    }

    public static List<OwnerBoundConstructView> findOwned(UUID ownerId, Identifier definitionId) {
        return OwnerBoundConstructManager.findOwned(ownerId, definitionId);
    }
}
