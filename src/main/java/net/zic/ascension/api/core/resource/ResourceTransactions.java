package net.zic.ascension.api.core.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.resource.source.ResourceSourceIdentity;

public final class ResourceTransactions {
    private ResourceTransactions() {
    }

    public static ResourceTransactionResult consume(
            LivingEntity entity,
            Identifier resource,
            ResourceSourceIdentity source,
            double amount
    ) {
        return transact(entity, resource, ResourceOperation.CONSUME, source, amount);
    }

    public static ResourceTransactionResult accumulate(
            LivingEntity entity,
            Identifier resource,
            ResourceSourceIdentity source,
            double amount
    ) {
        return transact(entity, resource, ResourceOperation.ACCUMULATE, source, amount);
    }

    public static ResourceTransactionResult restore(
            LivingEntity entity,
            Identifier resource,
            ResourceSourceIdentity source,
            double amount
    ) {
        return transact(entity, resource, ResourceOperation.RESTORE, source, amount);
    }

    public static ResourceTransactionResult generate(
            LivingEntity entity,
            Identifier resource,
            ResourceSourceIdentity source,
            double amount
    ) {
        return transact(entity, resource, ResourceOperation.GENERATE, source, amount);
    }

    public static ResourceTransactionResult drain(
            LivingEntity entity,
            Identifier resource,
            ResourceSourceIdentity source,
            double amount
    ) {
        return transact(entity, resource, ResourceOperation.DRAIN, source, amount);
    }

    private static ResourceTransactionResult transact(
            LivingEntity entity,
            Identifier resource,
            ResourceOperation operation,
            ResourceSourceIdentity source,
            double amount
    ) {
        return ResourceTransactionService.transact(
                ResourceTransactionRequest.of(entity, resource, operation, amount, source)
        );
    }
}
