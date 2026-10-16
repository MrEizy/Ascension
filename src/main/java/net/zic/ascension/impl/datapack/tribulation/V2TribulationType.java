package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.MapCodec;
import net.zic.ascension.impl.core.tribulation.V2LightningTribulation;

//the same configuration and data as the lightning tribulation, it only draws its storm differently
public class V2TribulationType extends LightningTribulationType {
    private static final MapCodec<V2LightningTribulation> CODEC = codec(V2LightningTribulation::new);

    @Override
    public MapCodec<V2LightningTribulation> codec() {
        return CODEC;
    }
}
