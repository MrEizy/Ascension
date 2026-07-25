package net.zic.ascension.api.ascension.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

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

    public abstract void onAdded(AscensionOriginSource source, TribulationDefinition definition, TribulationData tribulationData);
    public abstract void onRemoved(AscensionOriginSource source, TribulationDefinition definition, TribulationData tribulationData);


    /**
     * called when a saved tribulation definition type does not match the type we expect,
     * or when the definitions do not match(same type but different values)
     * in that scenario try to convert the TribulationData to match the new definition
     * if this is not possible create a new data instance
     * @param definition the new definition
     * @param oldDefinition the old definition
     * @param oldData the old data
     * @return the new data to be used in place of the old one
     */
    public abstract TribulationData validateAndCovert(TribulationDefinition definition,TribulationDefinition oldDefinition, TribulationData oldData);


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
