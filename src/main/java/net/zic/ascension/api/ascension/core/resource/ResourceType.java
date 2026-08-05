package net.zic.ascension.api.ascension.core.resource;

import net.minecraft.world.entity.LivingEntity;

public interface ResourceType {
    boolean supports(LivingEntity entity);

    double getAmount(LivingEntity entity);

    double getMaximum(LivingEntity entity);

    void setAmount(LivingEntity entity, double amount);

    default double normalizeAmount(double amount) {
        return Math.max(0.0D, amount);
    }

    default void afterApply(
            LivingEntity entity,
            ResourceOperation operation,
            ResourceOperation.Application result
    ) {
    }
}
