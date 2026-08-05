package net.zic.ascension.api.ascension.core.effect;

import net.minecraft.world.entity.LivingEntity;

public interface BuildupChannel {
    double apply(LivingEntity entity, double amount, int decayDelay);

    double reduce(LivingEntity entity, double amount);

    void clear(LivingEntity entity);
}
