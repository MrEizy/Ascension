package net.zic.ascension.impl.resource.type;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.resource.ResourceApplicationResult;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionStatus;
import net.zic.ascension.api.ascension.core.resource.ResourceType;

public abstract class AbstractBoundedResourceType implements ResourceType {
    @Override
    public boolean supports(ResourceOperation operation) {
        return operation == ResourceOperation.CONSUME
                || operation == ResourceOperation.RESTORE
                || operation == ResourceOperation.GENERATE
                || operation == ResourceOperation.DRAIN;
    }

    @Override
    public ResourceApplicationResult apply(
            LivingEntity entity,
            ResourceOperation operation,
            double amount,
            boolean simulate
    ) {
        double before = getAmount(entity);
        double maximum = Math.max(0.0D, getMaximum(entity));
        double resolvedAmount = normalizeAmount(Math.max(0.0D, amount));
        double after;
        double applied;
        ResourceTransactionStatus status;

        switch (operation) {
            case CONSUME -> {
                if (resolvedAmount > before) {
                    return ResourceApplicationResult.rejected(before);
                }
                after = Math.max(0.0D, before - resolvedAmount);
                applied = resolvedAmount;
                status = ResourceTransactionStatus.SUCCESS;
            }
            case DRAIN -> {
                applied = Math.min(before, resolvedAmount);
                after = Math.max(0.0D, before - applied);
                status = applied < resolvedAmount
                        ? ResourceTransactionStatus.PARTIAL
                        : ResourceTransactionStatus.SUCCESS;
            }
            case RESTORE, GENERATE -> {
                after = Math.min(maximum, before + resolvedAmount);
                applied = Math.max(0.0D, after - before);
                status = applied < resolvedAmount
                        ? ResourceTransactionStatus.PARTIAL
                        : ResourceTransactionStatus.SUCCESS;
            }
            default -> {
                return ResourceApplicationResult.unsupported(before);
            }
        }

        if (!simulate) {
            setAmount(entity, after);
        }
        return new ResourceApplicationResult(status, before, after, applied);
    }

    protected double normalizeAmount(double amount) {
        return amount;
    }

    protected abstract void setAmount(LivingEntity entity, double amount);
}
