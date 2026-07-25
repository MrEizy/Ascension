package net.zic.ascension.impl.resource.type;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class PlayerHungerResourceType extends AbstractBoundedResourceType {
    @Override
    public boolean supports(LivingEntity entity) {
        return entity instanceof Player;
    }

    @Override
    public double getAmount(LivingEntity entity) {
        return entity instanceof Player player ? player.getFoodData().getFoodLevel() : 0.0D;
    }

    @Override
    public double getMaximum(LivingEntity entity) {
        return 20.0D;
    }

    @Override
    protected double normalizeAmount(double amount) {
        return amount <= 0.0D ? 0.0D : Math.ceil(amount);
    }

    @Override
    protected void setAmount(LivingEntity entity, double amount) {
        if (entity instanceof Player player) {
            player.getFoodData().setFoodLevel(Math.clamp((int) amount, 0, 20));
        }
    }
}
