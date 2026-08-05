package net.zic.ascension.api.ascension.core.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ResourceModifiers {
    private ResourceModifiers() {
    }

    public enum Operation {
        ADD,
        MULTIPLY_BASE,
        MULTIPLY_TOTAL,
        MINIMUM,
        MAXIMUM,
        CANCEL,
        IMMUNITY;

        public static final Codec<Operation> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown resource modifier operation: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }

    public record Entry(Identifier id, Operation operation, double value, int priority) {
        public Entry {
            if (id == null) {
                throw new IllegalArgumentException("Resource modifier id cannot be null");
            }
            if (operation == null) {
                throw new IllegalArgumentException("Resource modifier operation cannot be null");
            }
            value = Double.isFinite(value) ? value : 0.0D;
        }
    }

    public record Resolution(double amount, boolean cancelled, boolean immune) {
    }

    public record Definition(
            Identifier id,
            ResourceTransactionRequest.Selector selector,
            Operation operation,
            ScaledValue value,
            int priority
    ) {
        public static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(Definition::id),
                ResourceTransactionRequest.Selector.CODEC.fieldOf("selector").forGetter(Definition::selector),
                Operation.CODEC.fieldOf("operation").forGetter(Definition::operation),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("value", ScaledValue.constant(0.0D)).forGetter(Definition::value),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Definition::priority)
        ).apply(instance, Definition::new));

        public boolean matches(ResourceTransactionService.Context context) {
            return selector.matches(context);
        }

        public Entry resolve(ResourceTransactionService.Context context, OriginSource source, Identifier skillId) {
            Identifier resolvedId = Identifier.fromNamespaceAndPath(
                    skillId.getNamespace(),
                    "resource_modifier/" + skillId.getPath() + "/" + id.getNamespace() + "/" + id.getPath()
            );
            return new Entry(
                    resolvedId,
                    operation,
                    value.resolve(context.scaledValueContext(source, skillId)),
                    priority
            );
        }
    }

    public static final class Collector {
        private static final Comparator<Entry> ORDER = Comparator
                .comparingInt(Entry::priority)
                .thenComparing(modifier -> modifier.id().toString());

        private final Map<Identifier, Entry> modifiers = new LinkedHashMap<>();

        public void add(Entry modifier) {
            if (modifier != null) {
                modifiers.put(modifier.id(), modifier);
            }
        }

        public void remove(Identifier modifierId) {
            if (modifierId != null) {
                modifiers.remove(modifierId);
            }
        }

        public List<Entry> modifiers() {
            List<Entry> ordered = new ArrayList<>(modifiers.values());
            ordered.sort(ORDER);
            return List.copyOf(ordered);
        }

        public Resolution resolve(double baseAmount) {
            List<Entry> ordered = modifiers();
            boolean immune = ordered.stream().anyMatch(modifier -> modifier.operation() == Operation.IMMUNITY);
            boolean cancelled = ordered.stream().anyMatch(modifier -> modifier.operation() == Operation.CANCEL);
            if (immune || cancelled) {
                return new Resolution(0.0D, cancelled, immune);
            }
            double flat = 0.0D;
            double baseMultiplier = 0.0D;
            double minimum = 0.0D;
            double maximum = Double.POSITIVE_INFINITY;
            for (Entry modifier : ordered) {
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
            for (Entry modifier : ordered) {
                if (modifier.operation() == Operation.MULTIPLY_TOTAL) {
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
            return new Resolution(Math.max(0.0D, amount), false, false);
        }
    }
}
