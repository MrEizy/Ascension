package net.zic.ascension.impl.resource.type;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class PlayerSaturationResourceType extends AbstractBoundedResourceType {
    @Override
    public boolean supports(LivingEntity entity) {
        return entity instanceof Player;
    }

    @Override
    public double getAmount(LivingEntity entity) {
        return entity instanceof Player player ? player.getFoodData().getSaturationLevel() : 0.0D;
    }

    @Override
    public double getMaximum(LivingEntity entity) {
        return entity instanceof Player player ? player.getFoodData().getFoodLevel() : 0.0D;
    }

    @Override
    protected void setAmount(LivingEntity entity, double amount) {
        if (entity instanceof Player player) {
            player.getFoodData().setSaturation((float) Math.clamp(amount, 0.0D, getMaximum(entity)));
        }
    }
}
