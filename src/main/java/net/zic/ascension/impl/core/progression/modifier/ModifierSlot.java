package net.zic.ascension.impl.core.progression.modifier;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.value_containers.typed.Modifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A view over one modifier list (flat or multiplier) of some value container.
 * Kept free of stat/source types so it can move to zenithLib.
 */
public interface ModifierSlot<T extends Number> {

    /** @return the modifier with this id if it is of this slot's type, otherwise null */
    @Nullable Modifier<T> get(Identifier id);

    /** @return true if a modifier of any type has this id */
    boolean hasAny(Identifier id);

    void add(Modifier<T> modifier);

    /** removes a modifier with this id regardless of its type */
    void remove(Identifier id);

    T zero();

    /** @return existing + direction * incoming */
    T combine(T existing, T incoming, int direction);

    boolean isZero(T value);

    static ModifierSlot<Double> ofDoubles(
            Function<Identifier, Modifier<Double>> get,
            Predicate<Identifier> hasAny,
            Consumer<Modifier<Double>> add,
            Consumer<Identifier> remove
    ) {
        return new ModifierSlot<>() {
            @Override
            public @Nullable Modifier<Double> get(Identifier id) {
                return get.apply(id);
            }

            @Override
            public boolean hasAny(Identifier id) {
                return hasAny.test(id);
            }

            @Override
            public void add(Modifier<Double> modifier) {
                add.accept(modifier);
            }

            @Override
            public void remove(Identifier id) {
                remove.accept(id);
            }

            @Override
            public Double zero() {
                return 0.0;
            }

            @Override
            public Double combine(Double existing, Double incoming, int direction) {
                return existing + direction * incoming;
            }

            @Override
            public boolean isZero(Double value) {
                return Math.abs(value) < 1.0e-9;
            }
        };
    }
}
