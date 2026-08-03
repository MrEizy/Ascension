package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.ascension.value.ScaledValueSource;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;

public record StatScaledValueSource(Identifier stat, boolean base) implements ScaledValueSource {
    public static final MapCodec<StatScaledValueSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("stat").forGetter(StatScaledValueSource::stat),
            Codec.BOOL.optionalFieldOf("base", false).forGetter(StatScaledValueSource::base)
    ).apply(instance, StatScaledValueSource::new));

    @Override
    public CodecType<ScaledValueSource> getType() {
        return AscensionScaledValueSourceTypes.STAT.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        if (context.source() == null) {
            return 0.0D;
        }
        Stat statDefinition = ZenithRegistries.STAT_REGISTRY.getValue(stat);
        if (statDefinition == null) {
            return 0.0D;
        }
        return base ? context.source().getBaseStat(statDefinition) : context.source().getStat(statDefinition);
    }
}
