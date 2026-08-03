package net.zic.ascension.api.ascension.core.resource.modifier;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResourceModifierCollector {
    private static final Comparator<ResourceModifier> ORDER = Comparator
            .comparingInt(ResourceModifier::priority)
            .thenComparing(modifier -> modifier.id().toString());

    private final Map<Identifier, ResourceModifier> modifiers = new LinkedHashMap<>();

    public void add(ResourceModifier modifier) {
        if (modifier != null) {
            modifiers.put(modifier.id(), modifier);
        }
    }

    public void remove(Identifier modifierId) {
        if (modifierId != null) {
            modifiers.remove(modifierId);
        }
    }

    public List<ResourceModifier> modifiers() {
        List<ResourceModifier> ordered = new ArrayList<>(modifiers.values());
        ordered.sort(ORDER);
        return List.copyOf(ordered);
    }

    public ResourceModifierResolution resolve(double baseAmount) {
        List<ResourceModifier> ordered = modifiers();
        boolean immune = ordered.stream().anyMatch(modifier -> modifier.operation() == ResourceModifierOperation.IMMUNITY);
        boolean cancelled = ordered.stream().anyMatch(modifier -> modifier.operation() == ResourceModifierOperation.CANCEL);
        if (immune || cancelled) {
            return new ResourceModifierResolution(0.0D, cancelled, immune);
        }

        double flat = 0.0D;
        double baseMultiplier = 0.0D;
        double minimum = 0.0D;
        double maximum = Double.POSITIVE_INFINITY;

        for (ResourceModifier modifier : ordered) {
            switch (modifier.operation()) {
                case ADD -> flat += modifier.value();
                case MULTIPLY_BASE -> baseMultiplier += modifier.value();
                case MINIMUM -> minimum = Math.max(minimum, modifier.value());
                case MAXIMUM -> maximum = Math.min(maximum, modifier.value());
                default -> {
                }
            }
        }

        double amount = baseAmount + flat + baseAmount * baseMultiplier;
        for (ResourceModifier modifier : ordered) {
            if (modifier.operation() == ResourceModifierOperation.MULTIPLY_TOTAL) {
                amount *= Math.max(0.0D, 1.0D + modifier.value());
            }
        }

        if (!Double.isFinite(amount)) {
            amount = 0.0D;
        }
        if (minimum > maximum) {
            maximum = minimum;
        }
        amount = Math.max(minimum, Math.min(maximum, amount));
        return new ResourceModifierResolution(Math.max(0.0D, amount), false, false);
    }
}
