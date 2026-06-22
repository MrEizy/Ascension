package net.zic.ascension.biome.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The raw configuration directly read from the datapack
 * @param biomes
 * @param energyCap
 * @param energyRegen
 * @param affinities
 */
public record RawBiomeConfiguration(HolderSet<Biome> biomes, double energyCap, double energyRegen,
                                    Object2DoubleOpenHashMap<Identifier> affinities) {

    public static final Codec<RawBiomeConfiguration> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(RawBiomeConfiguration::biomes),
                    Codec.DOUBLE.fieldOf("energy_cap").forGetter(RawBiomeConfiguration::energyCap),
                    Codec.DOUBLE.fieldOf("energy_regen").forGetter(RawBiomeConfiguration::energyRegen),
                    Codec.unboundedMap(Identifier.CODEC,Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).xmap(
                            Object2DoubleOpenHashMap::new,
                            HashMap::new

                    ).forGetter(RawBiomeConfiguration::affinities)
            ).apply(instance, RawBiomeConfiguration::new)
    );
}
