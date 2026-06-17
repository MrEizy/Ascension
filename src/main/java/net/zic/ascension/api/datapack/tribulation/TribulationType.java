package net.zic.ascension.api.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.tribulation.TribulationData;
import net.zic.ascension.api.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.datapack.TypeRegistries;

/**
 * Defines a group of related Tribulations
 * unlike others this type also has logic attached to it, with a tribulation definition being used to handle variations
 */
public abstract class TribulationType {
    public abstract MapCodec<? extends TribulationDefinition> codec();
    public abstract MapCodec<? extends TribulationData> dataCodec();
    public static Codec<TribulationDefinition> TRIBULATION_CODEC = TypeRegistries.TRIBULATION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    TribulationDefinition::getType,
                    TribulationType::codec
            );
    public static Codec<TribulationData> TRIBULATION_DATA_CODEC = TypeRegistries.TRIBULATION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    TribulationData::getType,
                    TribulationType::dataCodec
            );

    public abstract TribulationData newData();

    public abstract void onAdded(OriginSource source, TribulationData tribulationData);
    public abstract void onRemoved(OriginSource source,TribulationData tribulationData);

}
