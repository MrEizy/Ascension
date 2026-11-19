package net.zic.ascension.common.command;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

// would have made it nested, but thought I may need it somewhere else
public final class CommandInspectionTarget {
    private CommandInspectionTarget() {
    }

    public static EntityHitResult find(Player player, double range, Predicate<Entity> filter) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1).scale(range);
        AABB area = player.getBoundingBox().expandTowards(direction).inflate(1.0D);
        return ProjectileUtil.getEntityHitResult(player, start, start.add(direction), area,
                entity -> !entity.isSpectator() && filter.test(entity), range * range);
    }
}
