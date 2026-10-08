package net.zic.ascension.impl.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModifierActionHelper {
    private ModifierActionHelper() {
    }

    public static void apply(
            ModifierHolder<Double> modifiers,
            Supplier<ValueContainer<Double>> container,
            Consumer<Modifier<Double>> addFlat,
            Consumer<Modifier<Double>> addMultiplier,
            Consumer<Identifier> remove,
            ProgressDirection direction,
            ModifierMergeMode mergeMode,
            boolean perPurity,
            int purity
    ) {
        ModifierMergeMode effective = mergeMode.resolve(perPurity);
        if (perPurity && effective != ModifierMergeMode.REPLACE) {
            throw new IllegalArgumentException("Per-purity modifiers require replace mode");
        }
        for (Modifier<Double> modifier : modifiers.flat()) {
            applyModifier(modifier, true, container, addFlat, remove, direction, effective, perPurity, purity);
        }
        for (Modifier<Double> modifier : modifiers.multiplier()) {
            applyModifier(modifier, false, container, addMultiplier, remove, direction, effective, perPurity, purity);
        }
    }

    private static void applyModifier(
            Modifier<Double> modifier,
            boolean flat,
            Supplier<ValueContainer<Double>> containerSupplier,
            Consumer<Modifier<Double>> add,
            Consumer<Identifier> remove,
            ProgressDirection direction,
            ModifierMergeMode mode,
            boolean perPurity,
            int purity
    ) {
        ValueContainer<Double> container = containerSupplier.get();
        Modifier<Double> existing = container == null ? null : flat ? container.getFlatModifier(modifier.id()) : container.getMultiplierModifier(modifier.id());
        boolean otherType = container != null && container.hasModifier(modifier.id()) && existing == null;

        if (perPurity) {
            Modifier<Double> target = withValue(modifier, modifier.value() * purity);
            if (purity > 0 && target.equals(existing)) return;
            if (existing != null || otherType) remove.accept(modifier.id());
            if (purity > 0) add.accept(target);
            return;
        }

        if (mode == ModifierMergeMode.ACCUMULATE) {
            if (otherType) {
                throw new IllegalArgumentException("Cannot accumulate across modifier types: " + modifier.id());
            }
            if (existing != null && (!Objects.equals(existing.group(), modifier.group()) || existing.operationGroup() != modifier.operationGroup())) {
                throw new IllegalArgumentException("Cannot accumulate modifiers with different groups: " + modifier.id());
            }
            if (direction == ProgressDirection.DOWN && existing == null) return;
            double value = (existing == null ? 0 : existing.value()) + (direction == ProgressDirection.UP ? modifier.value() : -modifier.value());
            if (existing != null) remove.accept(modifier.id());
            if (Math.abs(value) > 1.0e-9) add.accept(withValue(modifier, value));
            return;
        }

        if (direction == ProgressDirection.DOWN) {
            if (existing != null || otherType) remove.accept(modifier.id());
            return;
        }
        if (mode == ModifierMergeMode.KEEP && (existing != null || otherType)) return;
        if (modifier.equals(existing)) return;
        if (existing != null || otherType) remove.accept(modifier.id());
        add.accept(modifier);
    }

    private static Modifier<Double> withValue(Modifier<Double> modifier, double value) {
        return new Modifier<>(modifier.type(), modifier.id(), modifier.group(), modifier.operationGroup(), value);
    }
}
