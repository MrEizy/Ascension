package net.zic.ascension.api.core.resource.modifier;

public record ResourceModifierResolution(
        double amount,
        boolean cancelled,
        boolean immune
) {
}
