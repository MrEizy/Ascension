package net.zic.ascension.api.core.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum ResourceOperation {
    CONSUME,
    ACCUMULATE,
    RESTORE,
    GENERATE,
    DRAIN;

    public static final Codec<ResourceOperation> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                try {
                    return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException exception) {
                    return DataResult.error(() -> "Unknown resource operation: " + value);
                }
            },
            value -> value.name().toLowerCase(Locale.ROOT)
    );
}
