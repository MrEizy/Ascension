package net.zic.ascension.api.core.targeting;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public record SkillTarget(LivingEntity entity, Vec3 position) {
    public SkillTarget {
        if (position == null && entity != null) {
            position = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        }
    }

    public static SkillTarget entity(LivingEntity entity) {
        return new SkillTarget(entity, null);
    }

    public static SkillTarget position(Vec3 position) {
        return new SkillTarget(null, position);
    }
}
