package net.zic.ascension.api.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.tribulation.TribulationData;
import net.zic.ascension.api.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.core.tribulation.TribulationInstance;
import net.zic.ascension.api.core.tribulation.TribulationManager;
import net.zic.ascension.api.datapack.TypeRegistries;

import java.util.UUID;

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

    public abstract TribulationData newData(TribulationDefinition definition);

    public abstract void onAdded(OriginSource source, TribulationData tribulationData);
    public abstract void onRemoved(OriginSource source,TribulationData tribulationData);

    /**
     * runs every tick, will run even if the entity is not loaded,
     * in which case getPosition returns the last know pos
     */
    public abstract void tick(TribulationManager manager,UUID tribulation,TribulationInstance instance);

    //pass in a manager so we do not need to rely on a singleton
    public void onTargetDeath(TribulationManager manager, UUID tribulation, TribulationInstance instance){
        //default behaviour is to do nothing
    }
}
