package net.zic.ascension.impl.resource.type;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.core.resource.ResourceApplicationResult;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.impl.resource.stamina.StaminaService;

public final class StaminaResourceType extends AbstractBoundedResourceType {
    @Override
    public boolean supports(LivingEntity entity) {
        return entity instanceof Player;
    }

    @Override
    public double getAmount(LivingEntity entity) {
        return StaminaService.getStamina(entity);
    }

    @Override
    public double getMaximum(LivingEntity entity) {
        return StaminaService.getMaximumStamina(entity);
    }

    @Override
    public ResourceApplicationResult apply(LivingEntity entity, ResourceOperation operation, double amount, boolean simulate) {
        ResourceApplicationResult result = super.apply(entity, operation, amount, simulate);
        if (!simulate && result.appliedAmount() > 0.0D && (operation == ResourceOperation.CONSUME || operation == ResourceOperation.DRAIN)) {
            StaminaService.resetRegenerationDelay(entity);
        }
        return result;
    }

    @Override
    protected void setAmount(LivingEntity entity, double amount) {
        StaminaService.setStamina(entity, amount);
    }
}
