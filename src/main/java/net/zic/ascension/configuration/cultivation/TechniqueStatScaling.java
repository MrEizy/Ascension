package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TechniqueStatScaling(double flat, double multiplier) {
    public static final Codec<TechniqueStatScaling> CODEC = RecordCodecBuilder.<TechniqueStatScaling>create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("flat").forGetter(TechniqueStatScaling::flat),
            Codec.DOUBLE.fieldOf("multiplier").forGetter(TechniqueStatScaling::multiplier)
    ).apply(instance, TechniqueStatScaling::new)).validate(value ->
            Double.isFinite(value.flat) && Double.isFinite(value.multiplier)
                    ? DataResult.success(value) : DataResult.error(() -> "Technique growth must be finite"));
}
