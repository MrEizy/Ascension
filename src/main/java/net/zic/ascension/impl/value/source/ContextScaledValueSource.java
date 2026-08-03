package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.api.value.source.ScaledValueSource;

public record ContextScaledValueSource(Identifier key, double fallback) implements ScaledValueSource {
    public static final MapCodec<ContextScaledValueSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("key").forGetter(ContextScaledValueSource::key),
            Codec.DOUBLE.optionalFieldOf("fallback", 0.0D).forGetter(ContextScaledValueSource::fallback)
    ).apply(instance, ContextScaledValueSource::new));

    @Override
    public CodecType<ScaledValueSource> getType() {
        return AscensionScaledValueSourceTypes.CONTEXT.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        return context.getVariable(key, fallback);
    }
}
