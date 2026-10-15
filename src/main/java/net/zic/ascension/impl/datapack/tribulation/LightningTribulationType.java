package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.tribulation.LightningTribulation;
import net.zic.ascension.impl.core.tribulation.LightningTribulationData;

public class LightningTribulationType extends TribulationType<LightningTribulationData> {
    private static final MapCodec<LightningTribulation> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("strikes").forGetter(LightningTribulation::strikes),
                    Codec.DOUBLE.fieldOf("damage").forGetter(LightningTribulation::damage),
                    Codec.INT.fieldOf("tick_delay").forGetter(LightningTribulation::tickDelay),
                    TribulationCloudSettings.CODEC.optionalFieldOf("clouds", TribulationCloudSettings.DEFAULT).forGetter(LightningTribulation::clouds)
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
    public MapCodec<LightningTribulation> codec() {
        return CODEC;
    }

    @Override
    public Codec<LightningTribulationData> dataCodec() {
        return DATA_CODEC;
    }
}
