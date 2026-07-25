package net.zic.ascension.api.core.resource.modifier;

import net.minecraft.resources.Identifier;

public record ResourceModifier(
        Identifier id,
        ResourceModifierOperation operation,
        double value,
        int priority
) {
    public ResourceModifier {
        if (id == null) {
            throw new IllegalArgumentException("Resource modifier id cannot be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Resource modifier operation cannot be null");
        }
        if (!Double.isFinite(value)) {
            value = 0.0D;
        }
    }
}
