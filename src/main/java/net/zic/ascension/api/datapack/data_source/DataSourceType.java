package net.zic.ascension.api.datapack.data_source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.data_source.DataSource;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;

public abstract class DataSourceType {
    public abstract MapCodec<? extends DataSource> codec();


    public static Codec<DataSource> DATA_SOURCE_CODEC = TypeRegistries.DATA_SOURCE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    DataSource::getType,
                    DataSourceType::codec
            );

}
