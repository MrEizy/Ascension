package net.zic.ascension.impl.runtime.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.projectile.ProjectileImpactResponse;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;

public final class ProjectileImpactResponses {
    private ProjectileImpactResponses() {
    }

    public static void apply(
            RPGEngineDamageSource source,
            LivingEntity protectedEntity,
            ProjectileImpactResponse response
    ) {
        if (source == null
                || protectedEntity == null
                || response == null
                || response == ProjectileImpactResponse.NONE
                || !(source.getDirectEntity() instanceof Projectile projectile)
                || projectile.isRemoved()) {
            return;
        }

        switch (response) {
            case STOP -> stop(projectile);
            case DISCARD -> projectile.discard();
            case DEFLECT -> deflect(projectile, protectedEntity);
            default -> {
            }
        }
    }

    private static void stop(Projectile projectile) {
        projectile.setDeltaMovement(Vec3.ZERO);
        projectile.hurtMarked = true;
    }

    private static void deflect(Projectile projectile, LivingEntity protectedEntity) {
        Vec3 origin = protectedEntity.position().add(0.0D, protectedEntity.getBbHeight() * 0.55D, 0.0D);
        Vec3 direction = projectile.position().subtract(origin);
        if (direction.lengthSqr() <= 1.0E-8D) {
            direction = protectedEntity.getLookAngle().scale(-1.0D);
        }
        double speed = Math.max(0.45D, projectile.getDeltaMovement().length() * 0.6D);
        Vec3 velocity = direction.normalize().scale(speed).add(0.0D, 0.08D, 0.0D);
        projectile.setOwner(protectedEntity);
        projectile.setPos(projectile.position().add(direction.normalize().scale(0.2D)));
        projectile.setDeltaMovement(velocity);
        projectile.hurtMarked = true;
    }
}
