package net.zic.ascension.api.core.resource.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum ResourceModifierOperation {
    ADD,
    MULTIPLY_BASE,
    MULTIPLY_TOTAL,
    MINIMUM,
    MAXIMUM,
    CANCEL,
    IMMUNITY;

    public static final Codec<ResourceModifierOperation> CODEC = Codec.STRING.comapFlatMap(
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
