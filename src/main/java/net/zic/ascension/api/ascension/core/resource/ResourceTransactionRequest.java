package net.zic.ascension.api.ascension.core.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.resource.source.ResourceSourceIdentity;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public record ResourceTransactionRequest(
        LivingEntity entity,
        Identifier resource,
        ResourceOperation operation,
        double amount,
        ResourceSourceIdentity source,
        Identifier skill,
        LivingEntity target,
        Map<Identifier, Double> values,
        Set<ResourceTransactionFlag> flags
) {
    public ResourceTransactionRequest {
        values = values == null ? Map.of() : Map.copyOf(values);
        flags = flags == null || flags.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(flags));
    }

    public static ResourceTransactionRequest of(
            LivingEntity entity,
            Identifier resource,
            ResourceOperation operation,
            double amount,
            ResourceSourceIdentity source
    ) {
        return new ResourceTransactionRequest(
                entity,
                resource,
                operation,
                amount,
                source,
                null,
                null,
                Map.of(),
                Set.of()
        );
    }

    public boolean hasFlag(ResourceTransactionFlag flag) {
        return flag != null && flags.contains(flag);
    }

    public ResourceTransactionRequest withSkill(Identifier skill) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }

    public ResourceTransactionRequest withTarget(LivingEntity target) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }

    public ResourceTransactionRequest withValues(Map<Identifier, Double> values) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }

    public ResourceTransactionRequest withFlags(Set<ResourceTransactionFlag> flags) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }
}
