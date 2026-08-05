package net.zic.ascension.configuration.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The raw configuration directly read from the datapack
 * @param dimensions
 * @param energyCap
 * @param energyRegen
 * @param affinities
 */
public record RawDimensionConfiguration(List<Identifier> dimensions, double energyCap, double energyRegen,
                                        Object2DoubleOpenHashMap<Identifier> affinities) {

    public static final Codec<RawDimensionConfiguration> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    Identifier.CODEC.listOf().fieldOf("dimensions").forGetter(RawDimensionConfiguration::dimensions),
                    Codec.DOUBLE.fieldOf("energy_cap").forGetter(RawDimensionConfiguration::energyCap),
                    Codec.DOUBLE.fieldOf("energy_regen").forGetter(RawDimensionConfiguration::energyRegen),
                    Codec.unboundedMap(Identifier.CODEC,Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).xmap(
                            Object2DoubleOpenHashMap::new,
                            HashMap::new

                    ).forGetter(RawDimensionConfiguration::affinities)
            ).apply(instance, RawDimensionConfiguration::new)
    );
}