package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.tribulation.LightningTribulation;
import net.zic.ascension.impl.core.tribulation.LightningTribulationData;

public class LightningTribulationType extends TribulationType {
    private static final MapCodec<LightningTribulation> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("strikes").forGetter(LightningTribulation::strikes),
                    Codec.DOUBLE.fieldOf("damage").forGetter(LightningTribulation::damage),
                    Codec.INT.fieldOf("tick_delay").forGetter(LightningTribulation::tickDelay)
            ).apply(instance, LightningTribulation::new)
    );

    private static final Codec<LightningTribulationData> DATA_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("strikes").forGetter(LightningTribulationData::getLightningSurvived),
                    Codec.INT.optionalFieldOf("ticks", 0).forGetter(LightningTribulationData::getTicks),
                    UUIDUtil.CODEC.fieldOf("id").forGetter(LightningTribulationData::getUUID)
            ).apply(instance, LightningTribulationData::new)
    );

    @Override
    public MapCodec<? extends Tribulation> codec() {
        return CODEC;
    }

    @Override
    public Codec<? extends TribulationData> dataCodec() {
        return DATA_CODEC;
    }
}
