package net.zic.ascension.api.core.resource;

import net.minecraft.world.entity.LivingEntity;

public interface ResourceType {
    boolean supports(LivingEntity entity);

    boolean supports(ResourceOperation operation);

    double getAmount(LivingEntity entity);

    double getMaximum(LivingEntity entity);

    ResourceApplicationResult apply(
            LivingEntity entity,
            ResourceOperation operation,
            double amount,
            boolean simulate
    );
}
