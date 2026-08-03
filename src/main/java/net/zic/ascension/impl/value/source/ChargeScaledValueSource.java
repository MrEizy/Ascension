package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.api.value.source.ScaledValueSource;

public final class ChargeScaledValueSource implements ScaledValueSource {
    public static final MapCodec<ChargeScaledValueSource> CODEC = MapCodec.unit(ChargeScaledValueSource::new);

    @Override
    public CodecType<ScaledValueSource> getType() {
        return AscensionScaledValueSourceTypes.CHARGE.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        return Math.max(0.0D, Math.min(1.0D, context.charge()));
    }
}
