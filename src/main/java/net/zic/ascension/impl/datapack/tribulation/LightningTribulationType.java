package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.tribulation.LightningTribulation;
import net.zic.ascension.impl.core.tribulation.LightningTribulationData;

public class LightningTribulationType extends TribulationType<LightningTribulationData> {
    private static final MapCodec<LightningTribulation> CODEC = codec(LightningTribulation::new);

    private static final Codec<LightningTribulationData> DATA_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("strikes").forGetter(LightningTribulationData::getLightningSurvived),
                    Codec.INT.optionalFieldOf("ticks", 0).forGetter(LightningTribulationData::getTicks),
                    UUIDUtil.CODEC.fieldOf("id").forGetter(LightningTribulationData::getUUID),
                    //saved before storms formed, so a missing phase means it is already running
                    LightningTribulationData.Phase.CODEC.optionalFieldOf("phase", LightningTribulationData.Phase.ACTIVE).forGetter(LightningTribulationData::getPhase),
                    Codec.INT.optionalFieldOf("phase_ticks", 0).forGetter(LightningTribulationData::getPhaseTicks),
                    Codec.BOOL.optionalFieldOf("failing", false).forGetter(LightningTribulationData::isFailing)
            ).apply(instance, LightningTribulationData::new)
    );

    //the configuration fields every lightning tribulation shares, so variants that only draw differently can reuse them
    protected static <L extends LightningTribulation> MapCodec<L> codec(Function4<Integer, Double, Integer, TribulationCloudSettings, L> factory) {
        return RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Codec.INT.fieldOf("strikes").forGetter(LightningTribulation::strikes),
                        Codec.DOUBLE.fieldOf("damage").forGetter(LightningTribulation::damage),
                        Codec.INT.fieldOf("tick_delay").forGetter(LightningTribulation::tickDelay),
                        TribulationCloudSettings.CODEC.optionalFieldOf("clouds", TribulationCloudSettings.DEFAULT).forGetter(LightningTribulation::clouds)
                ).apply(instance, factory)
        );
    }

    @Override
    public MapCodec<? extends LightningTribulation> codec() {
        return CODEC;
    }

    @Override
    public Codec<LightningTribulationData> dataCodec() {
        return DATA_CODEC;
    }
}
