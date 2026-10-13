package net.zic.ascension.api.ascension.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

/**
 * Defines how a group of related Tribulations are read and written.
 * All logic lives on the {@link Tribulation} itself, the type only provides codecs.
 * @param <T> the data used by tribulations of this type
 */
public abstract class TribulationType<T extends TribulationData> {
    public static final Codec<Tribulation<?>> TRIBULATION_CODEC = TypeRegistries.TRIBULATION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Tribulation::getType,
                    TribulationType::codec
            );

    //codec for the registry entry (the configuration)
    public abstract MapCodec<? extends Tribulation<T>> codec();

    //codec used to save TribulationData, both for active and completed tribulations
    public abstract Codec<T> dataCodec();

    /**
     * codec used to sync TribulationData to clients tracking the tribulation.
     * defaults to the data codec, override if you want a more compact network format
     */
    public StreamCodec<RegistryFriendlyByteBuf, T> dataStreamCodec() {
        return ByteBufCodecs.fromCodecWithRegistries(dataCodec());
    }
}
