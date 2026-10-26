package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.core.tribulation.GoldenCoreTribulation;
import net.zic.ascension.impl.core.tribulation.GoldenCoreTribulationData;

import java.util.Optional;

public class GoldenCoreTribulationType extends TribulationType<GoldenCoreTribulationData> {
    private static final MapCodec<GoldenCoreTribulation> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("strikes").forGetter(GoldenCoreTribulation::strikes),
                    Codec.DOUBLE.fieldOf("damage").forGetter(GoldenCoreTribulation::damage),
                    Codec.INT.fieldOf("tick_delay").forGetter(GoldenCoreTribulation::tickDelay)
            ).apply(instance, GoldenCoreTribulation::new)
    );

    private static final Codec<GoldenCoreTribulationData> DATA_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("strikes").forGetter(GoldenCoreTribulationData::getLightningSurvived),
                    Codec.INT.optionalFieldOf("ticks", 0).forGetter(GoldenCoreTribulationData::getTicks),
                    UUIDUtil.CODEC.fieldOf("id").forGetter(GoldenCoreTribulationData::getUUID),
                    Identifier.CODEC.optionalFieldOf("golden_core").forGetter(data -> Optional.ofNullable(data.getGoldenCore())),
                    UUIDUtil.CODEC.optionalFieldOf("golden_core_id").forGetter(data -> Optional.ofNullable(data.getGoldenCoreId())),
                    Identifier.CODEC.optionalFieldOf("path").forGetter(data -> Optional.ofNullable(data.getPath())),
                    Realm.CODEC.optionalFieldOf("acquisition_realm").forGetter(data -> Optional.ofNullable(data.getAcquisitionRealm()))
            ).apply(instance, GoldenCoreTribulationData::new)
    );

    @Override
    public MapCodec<GoldenCoreTribulation> codec() {
        return CODEC;
    }

    @Override
    public Codec<GoldenCoreTribulationData> dataCodec() {
        return DATA_CODEC;
    }
}
