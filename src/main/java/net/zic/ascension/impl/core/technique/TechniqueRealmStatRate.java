package net.zic.ascension.impl.core.technique;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.configuration.ConfigurationRegistries;
import net.zic.ascension.impl.core.progression.ScalingProfiles;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record TechniqueRealmStatRate(
        Optional<Identifier> path,
        double flat,
        double multiplier,
        Optional<Identifier> profile
) {
    public static final Codec<TechniqueRealmStatRate> CODEC =
            RecordCodecBuilder.<TechniqueRealmStatRate>create(instance ->
                    instance.group(
                            Identifier.CODEC.optionalFieldOf("path")
                                    .forGetter(TechniqueRealmStatRate::path),
                            Codec.DOUBLE.optionalFieldOf("flat", 0.0D)
                                    .forGetter(TechniqueRealmStatRate::flat),
                            Codec.DOUBLE.optionalFieldOf("multiplier", 0.0D)
                                    .forGetter(TechniqueRealmStatRate::multiplier),
                            Identifier.CODEC.optionalFieldOf("profile").forGetter(TechniqueRealmStatRate::profile)
                    ).apply(instance, TechniqueRealmStatRate::new)
            ).validate(rate ->
                    Double.isFinite(rate.flat()) && Double.isFinite(rate.multiplier())
                            && (rate.profile().isEmpty() || (rate.flat() == 0 && rate.multiplier() == 0))
                            ? DataResult.success(rate)
                            : DataResult.error(() -> "Technique stat growth must be finite; use either profile or literal rates")
            );

    public TechniqueRealmStatRate(Optional<Identifier> path, double flat, double multiplier) {
        this(path, flat, multiplier, Optional.empty());
    }

    public TechniqueRealmStatRate resolve(RegistryAccess access) {
        if (profile.isEmpty()) return this;
        var rates = ScalingProfiles.resolve(access, ConfigurationRegistries.TECHNIQUE_STAT_SCALING_REGISTRY,
                Either.left(profile.orElseThrow()));
        return new TechniqueRealmStatRate(path, rates.flat(), rates.multiplier());
    }

    public TechniqueRealmStatRate {
        path = path == null ? Optional.empty() : path;
        profile = profile == null ? Optional.empty() : profile;
    }
}
