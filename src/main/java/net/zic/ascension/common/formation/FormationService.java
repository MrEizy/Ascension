package net.zic.ascension.common.formation;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.formation.FormationInstanceView;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.impl.core.formation.FormationManager;

import java.util.List;
import java.util.UUID;

public final class FormationService {
    private FormationService() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definition,
            Vec3 center
    ) {
        return FormationManager.spawn(context, definition, center);
    }

    public static boolean remove(UUID runtimeId) {
        return FormationManager.remove(runtimeId);
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        return FormationManager.remove(level, runtimeId);
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        return FormationManager.removeOwned(ownerId, definitionId);
    }

    public static int removeOwned(ServerLevel level, UUID ownerId, Identifier definitionId) {
        return FormationManager.removeOwned(level, ownerId, definitionId);
    }

    public static List<FormationInstanceView> findOwned(UUID ownerId, Identifier definitionId) {
        return FormationManager.findOwned(ownerId, definitionId);
    }
}
