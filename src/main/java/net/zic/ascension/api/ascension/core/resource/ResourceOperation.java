package net.zic.ascension.api.ascension.core.resource;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

public final class ResourceOperation {
    public static final Codec<ResourceOperation> CODEC = ResourceRegistries.RESOURCE_OPERATION_REGISTRY.byNameCodec();

    public static final ResourceOperation CONSUME = of(ResourceOperation::strictRemoval);
    public static final ResourceOperation DRAIN = of(ResourceOperation::partialRemoval);
    public static final ResourceOperation RESTORE = of(ResourceOperation::boundedAddition);
    public static final ResourceOperation GENERATE = of(ResourceOperation::boundedAddition);
    public static final ResourceOperation ACCUMULATE = of(ResourceOperation::boundedAddition);

    private final Handler handler;

    private ResourceOperation(Handler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    public static ResourceOperation of(Handler handler) {
        return new ResourceOperation(handler);
    }

    public Application apply(ResourceType resource, LivingEntity entity, double amount, boolean simulate) {
        return handler.apply(this, resource, entity, Math.max(0.0D, amount), simulate);
    }

    private static Application strictRemoval(
            ResourceOperation operation,
            ResourceType resource,
            LivingEntity entity,
            double amount,
            boolean simulate
    ) {
        double before = resource.getAmount(entity);
        double resolved = resource.normalizeAmount(amount);
        if (resolved > before) {
            return Application.rejected(before);
        }
        double after = Math.max(0.0D, before - resolved);
        Application result = new Application(ResourceTransactionService.Status.SUCCESS, before, after, resolved);
        apply(resource, entity, operation, result, simulate);
        return result;
    }

    private static Application partialRemoval(
            ResourceOperation operation,
            ResourceType resource,
            LivingEntity entity,
            double amount,
            boolean simulate
    ) {
        double before = resource.getAmount(entity);
        double resolved = resource.normalizeAmount(amount);
        double applied = Math.min(before, resolved);
        double after = Math.max(0.0D, before - applied);
        Application result = new Application(
                applied < resolved ? ResourceTransactionService.Status.PARTIAL : ResourceTransactionService.Status.SUCCESS,
                before,
                after,
                applied
        );
        apply(resource, entity, operation, result, simulate);
        return result;
    }

    private static Application boundedAddition(
            ResourceOperation operation,
            ResourceType resource,
            LivingEntity entity,
            double amount,
            boolean simulate
    ) {
        double before = resource.getAmount(entity);
        double resolved = resource.normalizeAmount(amount);
        double maximum = Math.max(0.0D, resource.getMaximum(entity));
        double after = Math.min(maximum, before + resolved);
        double applied = Math.max(0.0D, after - before);
        Application result = new Application(
                applied < resolved ? ResourceTransactionService.Status.PARTIAL : ResourceTransactionService.Status.SUCCESS,
                before,
                after,
                applied
        );
        apply(resource, entity, operation, result, simulate);
        return result;
    }

    private static void apply(
            ResourceType resource,
            LivingEntity entity,
            ResourceOperation operation,
            Application result,
            boolean simulate
    ) {
        if (!simulate) {
            resource.setAmount(entity, result.amountAfter());
            resource.afterApply(entity, operation, result);
        }
    }

    public record Application(
            ResourceTransactionService.Status status,
            double amountBefore,
            double amountAfter,
            double appliedAmount
    ) {
        public static Application rejected(double current) {
            return new Application(ResourceTransactionService.Status.REJECTED, current, current, 0.0D);
        }

        public static Application unsupported(double current) {
            return new Application(ResourceTransactionService.Status.UNSUPPORTED, current, current, 0.0D);
        }
    }

    @FunctionalInterface
    public interface Handler {
        Application apply(
                ResourceOperation operation,
                ResourceType resource,
                LivingEntity entity,
                double amount,
                boolean simulate
        );
    }
}
