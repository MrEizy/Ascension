package net.zic.ascension.impl.core.progression.modifier;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Decides what happens when a modifier is applied (direction 1) or reverted (direction -1)
 * and the target container may already hold a modifier with the same id.
 */
public enum ModifierConflictAction implements StringRepresentable {
    /** salts the modifier id so every caller salt gets its own modifier */
    SALT("salt") {
        @Override
        public <T extends Number> void apply(ModifierSlot<T> slot, Modifier<T> modifier, int direction, String salt) {
            Identifier saltedId = saltId(modifier.id(), salt);
            if (slot.hasAny(saltedId)) slot.remove(saltedId);
            if (direction > 0) slot.add(withId(modifier, saltedId));
        }
    },
    /** sums the value into an existing modifier of the same type */
    MERGE("merge") {
        @Override
        public <T extends Number> void apply(ModifierSlot<T> slot, Modifier<T> modifier, int direction, String salt) {
            Modifier<T> existing = slot.get(modifier.id());
            if (existing == null && slot.hasAny(modifier.id())) {
                LOGGER.error("Cannot merge modifier {}: an existing modifier with this id is of a different type", modifier.id());
                return;
            }
            T value = slot.combine(existing == null ? slot.zero() : existing.value(), modifier.value(), direction);
            if (existing != null) slot.remove(modifier.id());
            if (!slot.isZero(value)) slot.add(withValue(modifier, value));
        }
    },
    /** removes any modifier with the same id, then adds the new one */
    REPLACE("replace") {
        @Override
        public <T extends Number> void apply(ModifierSlot<T> slot, Modifier<T> modifier, int direction, String salt) {
            if (slot.hasAny(modifier.id())) slot.remove(modifier.id());
            if (direction > 0) slot.add(modifier);
        }
    },
    /** removes an exactly matching modifier, and adds it back when reverted */
    REMOVE("remove") {
        @Override
        public <T extends Number> void apply(ModifierSlot<T> slot, Modifier<T> modifier, int direction, String salt) {
            if (direction > 0) {
                if (modifier.equals(slot.get(modifier.id()))) slot.remove(modifier.id());
            } else if (!slot.hasAny(modifier.id())) {
                slot.add(modifier);
            }
        }
    };

    public static final Codec<ModifierConflictAction> CODEC = StringRepresentable.fromEnum(ModifierConflictAction::values);
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String serializedName;

    ModifierConflictAction(String serializedName) {
        this.serializedName = serializedName;
    }

    public abstract <T extends Number> void apply(ModifierSlot<T> slot, Modifier<T> modifier, int direction, String salt);

    /**
     * applies every flat and multiplier modifier of the holder, after passing each one through the transform
     */
    public <T extends Number> void applyAll(
            ModifierHolder<T> holder,
            ModifierSlot<T> flat,
            ModifierSlot<Double> multiplier,
            UnaryOperator<Modifier<T>> flatTransform,
            UnaryOperator<Modifier<Double>> multiplierTransform,
            int direction,
            String salt
    ) {
        for (Modifier<T> modifier : holder.flat()) {
            apply(flat, flatTransform.apply(modifier), direction, salt);
        }
        for (Modifier<Double> modifier : holder.multiplier()) {
            apply(multiplier, multiplierTransform.apply(modifier), direction, salt);
        }
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static <T extends Number> Modifier<T> withValue(Modifier<T> modifier, T value) {
        return new Modifier<>(modifier.type(), modifier.id(), modifier.group(), modifier.operationGroup(), value);
    }

    private static <T extends Number> Modifier<T> withId(Modifier<T> modifier, Identifier id) {
        return new Modifier<>(modifier.type(), id, modifier.group(), modifier.operationGroup(), modifier.value());
    }

    private static Identifier saltId(Identifier id, String salt) {
        StringBuilder path = new StringBuilder(id.getPath()).append('_');
        for (char c : salt.toLowerCase(Locale.ROOT).toCharArray()) {
            boolean valid = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/';
            path.append(valid ? c : '_');
        }
        return Identifier.fromNamespaceAndPath(id.getNamespace(), path.toString());
    }
}
