package net.zic.ascension.api.ascension.core.skill.castable.feature;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public record SkillExecutionAttribution(
        UUID ownerId,
        UUID casterId,
        Entity directEntity,
        Optional<Identifier> projectileDefinition,
        Optional<UUID> projectileRuntime
) {
    public SkillExecutionAttribution {
        projectileDefinition = projectileDefinition == null ? Optional.empty() : projectileDefinition;
        projectileRuntime = projectileRuntime == null ? Optional.empty() : projectileRuntime;
    }

    public static SkillExecutionAttribution direct(ServerPlayer caster) {
        return new SkillExecutionAttribution(
                caster.getUUID(),
                caster.getUUID(),
                caster,
                Optional.empty(),
                Optional.empty()
        );
    }

    public static SkillExecutionAttribution virtualProjectile(
            ServerPlayer owner,
            Identifier definition,
            UUID runtimeId
    ) {
        return new SkillExecutionAttribution(
                owner.getUUID(),
                owner.getUUID(),
                null,
                Optional.ofNullable(definition),
                Optional.ofNullable(runtimeId)
        );
    }
}
