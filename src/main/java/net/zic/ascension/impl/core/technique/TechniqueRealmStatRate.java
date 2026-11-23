package net.zic.ascension.impl.core.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record TechniqueRealmStatRate(
        Optional<Identifier> path,
        double flat,
        double multiplier
) {
    public static final Codec<TechniqueRealmStatRate> CODEC =
            RecordCodecBuilder.<TechniqueRealmStatRate>create(instance ->
                    instance.group(
                            Identifier.CODEC.optionalFieldOf("path")
                                    .forGetter(TechniqueRealmStatRate::path),
                            Codec.DOUBLE.optionalFieldOf("flat", 0.0D)
                                    .forGetter(TechniqueRealmStatRate::flat),
                            Codec.DOUBLE.optionalFieldOf("multiplier", 0.0D)
                                    .forGetter(TechniqueRealmStatRate::multiplier)
                    ).apply(instance, TechniqueRealmStatRate::new)
            ).validate(rate ->
                    Double.isFinite(rate.flat()) && Double.isFinite(rate.multiplier())
                            ? DataResult.success(rate)
                            : DataResult.error(() -> "Technique stat growth must be finite")
            );

    public TechniqueRealmStatRate {
        path = path == null ? Optional.empty() : path;
    }
}
