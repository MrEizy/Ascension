package net.zic.ascension.configuration.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.zic.ascension.configuration.ConfigurationDataMaps;

import java.util.Map;
//TODO add extra modifiers like a distance from center modifier
public record DimensionConfiguration(long capacity, long regenRate, Object2DoubleMap<Identifier> affinities){
    public static final Codec<DimensionConfiguration> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    Codec.LONG.fieldOf("capacity").forGetter(DimensionConfiguration::capacity),
                    Codec.LONG.fieldOf("regen_rate").forGetter(DimensionConfiguration::regenRate),
                    Codec.unboundedMap(Identifier.CODEC,Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).xmap(
                            map-> Object2DoubleMaps.unmodifiable(
                                    new Object2DoubleOpenHashMap<>(map)
                            ),
                            Map::copyOf

                    ).forGetter(DimensionConfiguration::affinities)
            ).apply(instance, DimensionConfiguration::new)
    );

    public static DimensionConfiguration getConfiguration(Level level){
        return level.registryAccess().lookupOrThrow(Registries.DIMENSION).getData(ConfigurationDataMaps.DIMENSION_CONFIGURATION,level.dimension());
    }
}
