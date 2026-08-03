package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.ascension.value.ScaledValueSource;

public record ConstantScaledValueSource(double value) implements ScaledValueSource {
    public static final MapCodec<ConstantScaledValueSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf("value").forGetter(ConstantScaledValueSource::value)
    ).apply(instance, ConstantScaledValueSource::new));

    @Override
    public CodecType<ScaledValueSource> getType() {
        return AscensionScaledValueSourceTypes.CONSTANT.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        return value;
    }
}
