package net.zic.ascension.configuration.biome.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.zic.ascension.configuration.biome.RawBiomeConfiguration;

import java.util.HashMap;
import java.util.Map;

//TODO setup a merger for data map
public record BiomeConfiguration(long capacity, long regenRate, Object2DoubleMap<Identifier> affinities){
    public static final Codec<BiomeConfiguration> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                     Codec.LONG.fieldOf("capacity").forGetter(BiomeConfiguration::capacity),
                    Codec.LONG.fieldOf("regen_rate").forGetter(BiomeConfiguration::regenRate),
                    Codec.unboundedMap(Identifier.CODEC,Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).xmap(
                            map-> Object2DoubleMaps.unmodifiable(
                                    new Object2DoubleOpenHashMap<>(map)
                            ),
                            Map::copyOf

                    ).forGetter(BiomeConfiguration::affinities)
            ).apply(instance, BiomeConfiguration::new)
    );
}
