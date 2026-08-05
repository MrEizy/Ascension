package net.zic.ascension.api.ascension.core.skill.castable.feature;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
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

    public static SkillExecutionAttribution direct(LivingEntity caster) {
        return new SkillExecutionAttribution(
                caster.getUUID(),
                caster.getUUID(),
                caster,
                Optional.empty(),
                Optional.empty()
        );
    }

    public static SkillExecutionAttribution virtualProjectile(
            LivingEntity owner,
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
