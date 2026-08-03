package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.ascension.value.ScaledValueSource;

import java.util.Optional;

public record AffinityScaledValueSource(Identifier path, Optional<Identifier> category, boolean base) implements ScaledValueSource {
    public static final MapCodec<AffinityScaledValueSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(AffinityScaledValueSource::path),
            Identifier.CODEC.optionalFieldOf("category").forGetter(AffinityScaledValueSource::category),
            Codec.BOOL.optionalFieldOf("base", false).forGetter(AffinityScaledValueSource::base)
    ).apply(instance, AffinityScaledValueSource::new));

    @Override
    public CodecType<ScaledValueSource> getType() {
        return AscensionScaledValueSourceTypes.AFFINITY.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        if (context.source() == null) {
            return 0.0D;
        }
        Identifier resolvedCategory = category
                .filter(value -> !value.equals(PathEffectValueUtil.NO_CATEGORY))
                .orElse(AscensionOriginSourceHelper.AFFINITY_CATEGORY);
        ValueContainer container = AscensionOriginSourceHelper.getPathBonusContainer(
                context.source(),
                resolvedCategory,
                path
        );
        if (container == null) {
            return 0.0D;
        }
        return base ? container.getBaseValue() : container.getValue();
    }
}
