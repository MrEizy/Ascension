package net.zic.ascension.api.ascension.core.targeting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum TargetSort {
    NEAREST,
    FURTHEST,
    LOWEST_HEALTH,
    HIGHEST_HEALTH,
    CLOSEST_TO_VIEW;

    public static final Codec<TargetSort> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                try {
                    return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException exception) {
                    return DataResult.error(() -> "Unknown target sort: " + value);
                }
            },
            value -> value.name().toLowerCase(Locale.ROOT)
    );
}
