package net.zic.ascension.api.ascension.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum ScaledValueOperation {
    ADD,
    MULTIPLY,
    SET;

    public static final Codec<ScaledValueOperation> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                try {
                    return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException exception) {
                    return DataResult.error(() -> "Unknown scaled value operation: " + value);
                }
            },
            value -> value.name().toLowerCase(Locale.ROOT)
    );

    public double apply(double current, double value) {
        return switch (this) {
            case ADD -> current + value;
            case MULTIPLY -> current * value;
            case SET -> value;
        };
    }
}
