package net.zic.ascension.impl.value.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.path.PathEffectValueUtil;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.api.value.source.ScaledValueSource;
import net.zic.ascension.api.value.source.ScaledValueSourceType;

import java.util.Optional;

public record AffinityScaledValueSource(Identifier path, Optional<Identifier> category, boolean base) implements ScaledValueSource {
    public static final MapCodec<AffinityScaledValueSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(AffinityScaledValueSource::path),
            Identifier.CODEC.optionalFieldOf("category").forGetter(AffinityScaledValueSource::category),
            Codec.BOOL.optionalFieldOf("base", false).forGetter(AffinityScaledValueSource::base)
    ).apply(instance, AffinityScaledValueSource::new));

    @Override
    public ScaledValueSourceType getType() {
        return AscensionScaledValueSourceTypes.AFFINITY.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        if (context.source() == null) {
            return 0.0D;
        }
        if (category.isEmpty() || category.get().equals(PathEffectValueUtil.NO_CATEGORY)) {
            return base ? context.source().getBaseAffinity(path) : context.source().getAffinity(path);
        }
        return base ? context.source().getBaseAffinity(category.get(), path) : context.source().getAffinity(category.get(), path);
    }
}
