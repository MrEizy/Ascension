package net.zic.ascension.api.ascension.core.skill.castable.action;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public record SkillActionAttribution(
        UUID ownerId,
        UUID casterId,
        Entity directEntity,
        Optional<Identifier> projectileDefinition,
        Optional<UUID> projectileRuntime
) {
    public SkillActionAttribution {
        projectileDefinition = projectileDefinition == null ? Optional.empty() : projectileDefinition;
        projectileRuntime = projectileRuntime == null ? Optional.empty() : projectileRuntime;
    }

    public static SkillActionAttribution direct(LivingEntity caster) {
        return new SkillActionAttribution(
                caster.getUUID(),
                caster.getUUID(),
                caster,
                Optional.empty(),
                Optional.empty()
        );
    }

    public static SkillActionAttribution virtualProjectile(
            LivingEntity owner,
            Identifier definition,
            UUID runtimeId
    ) {
        return new SkillActionAttribution(
                owner.getUUID(),
                owner.getUUID(),
                null,
                Optional.ofNullable(definition),
                Optional.ofNullable(runtimeId)
        );
    }
}
