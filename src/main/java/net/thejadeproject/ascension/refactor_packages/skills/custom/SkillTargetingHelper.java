package net.thejadeproject.ascension.refactor_packages.skills.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class SkillTargetingHelper {
    private SkillTargetingHelper() {}

    // I didn't want to keep writing ray-casting in each class that needed it.
    // If someone has a better method please replace this lol - sortofSmart

    public static LivingEntity findLookTarget(Player player, double range, double inflate) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));

        AABB box = player.getBoundingBox()
                .expandTowards(look.scale(range))
                .inflate(inflate);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                eye,
                end,
                box,
                entity -> entity instanceof LivingEntity living && living.isAlive() && entity != player
        );

        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }
}