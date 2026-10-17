package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.MapCodec;
import net.zic.ascension.impl.core.tribulation.V3LightningTribulation;

//the same configuration and data as the lightning tribulation, its storm is built out of minecraft clouds
public class V3TribulationType extends LightningTribulationType {
    private static final MapCodec<V3LightningTribulation> CODEC = codec(V3LightningTribulation::new);

    @Override
    public MapCodec<V3LightningTribulation> codec() {
        return CODEC;
    }
}
