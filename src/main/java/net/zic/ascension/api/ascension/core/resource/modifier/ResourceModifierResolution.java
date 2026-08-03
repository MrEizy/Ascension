package net.zic.ascension.api.ascension.core.resource.modifier;

public record ResourceModifierResolution(
        double amount,
        boolean cancelled,
        boolean immune
) {
}
