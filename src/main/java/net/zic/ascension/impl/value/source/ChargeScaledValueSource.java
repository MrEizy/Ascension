package net.zic.ascension.impl.value.source;

import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.api.value.source.ScaledValueSource;
import net.zic.ascension.api.value.source.ScaledValueSourceType;

public final class ChargeScaledValueSource implements ScaledValueSource {
    public static final MapCodec<ChargeScaledValueSource> CODEC = MapCodec.unit(ChargeScaledValueSource::new);

    @Override
    public ScaledValueSourceType getType() {
        return AscensionScaledValueSourceTypes.CHARGE.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        return Math.max(0.0D, Math.min(1.0D, context.charge()));
    }
}
