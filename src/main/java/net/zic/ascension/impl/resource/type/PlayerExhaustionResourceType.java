package net.zic.ascension.impl.resource.type;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.zic.ascension.api.ascension.core.resource.ResourceApplicationResult;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionStatus;
import net.zic.ascension.api.ascension.core.resource.ResourceType;
import net.zic.ascension.mixins.accessor.FoodDataAccessor;

public final class PlayerExhaustionResourceType implements ResourceType {
    private static final double MAXIMUM_EXHAUSTION = 40.0D;

    @Override
    public boolean supports(LivingEntity entity) {
        return entity instanceof Player;
    }

    @Override
    public boolean supports(ResourceOperation operation) {
        return operation == ResourceOperation.ACCUMULATE || operation == ResourceOperation.RESTORE || operation == ResourceOperation.DRAIN;
    }

    @Override
    public double getAmount(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return 0.0D;
        }

        return accessor(player.getFoodData()).ascension$getExhaustionLevel();
    }

    @Override
    public double getMaximum(LivingEntity entity) {
        return MAXIMUM_EXHAUSTION;
    }

    @Override
    public ResourceApplicationResult apply(LivingEntity entity, ResourceOperation operation, double amount, boolean simulate) {
        if (!(entity instanceof Player player)) {
            return ResourceApplicationResult.unsupported(0.0D);
        }

        double before = getAmount(player);
        double resolvedAmount = Math.max(0.0D, amount);
        double after;
        double applied;
        ResourceTransactionStatus status;

        switch (operation) {
            case ACCUMULATE -> {
                after = Math.min(MAXIMUM_EXHAUSTION, before + resolvedAmount);
                applied = Math.max(0.0D, after - before);
                status = applied < resolvedAmount ? ResourceTransactionStatus.PARTIAL : ResourceTransactionStatus.SUCCESS;
            }
            case RESTORE, DRAIN -> {
                applied = Math.min(before, resolvedAmount);
                after = Math.max(0.0D, before - applied);
                status = applied < resolvedAmount ? ResourceTransactionStatus.PARTIAL : ResourceTransactionStatus.SUCCESS;
            }
            default -> {
                return ResourceApplicationResult.unsupported(before);
            }
        }

        if (!simulate) {
            accessor(player.getFoodData()).ascension$setExhaustionLevel((float) after);
        }

        return new ResourceApplicationResult(status, before, after, applied);
    }

    private static FoodDataAccessor accessor(FoodData foodData) {
        return (FoodDataAccessor) foodData;
    }
}