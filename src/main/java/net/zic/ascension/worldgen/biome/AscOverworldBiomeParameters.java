package net.zic.ascension.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AscOverworldBiomeParameters {
    private AscOverworldBiomeParameters() {
    }

    public static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters() {
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters = new ArrayList<>();

        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer = pair -> parameters.add(Pair.of(
                pair.getFirst(),
                AscOverworldBiomePlacement.remapSurfaceBiome(pair.getSecond())
        ));

        try {
            Constructor<OverworldBiomeBuilder> constructor = OverworldBiomeBuilder.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            OverworldBiomeBuilder builder = constructor.newInstance();

            Method addBiomes = findAddBiomesMethod();
            addBiomes.setAccessible(true);
            addBiomes.invoke(builder, consumer);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read vanilla Overworld biome parameters for Ascension datagen", exception);
        }

        return List.copyOf(parameters);
    }


    public static Climate.ParameterList<Holder<Biome>> parameterList(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> resolved = parameters().stream()
                .map(pair -> Pair.<Climate.ParameterPoint, Holder<Biome>>of(
                        pair.getFirst(),
                        biomes.getOrThrow(pair.getSecond())
                ))
                .toList();
        return new Climate.ParameterList<>(resolved);
    }

    private static Method findAddBiomesMethod() {
        for (Method method : OverworldBiomeBuilder.class.getDeclaredMethods()) {
            if (method.getName().equals("addBiomes") && method.getParameterCount() == 1) {
                return method;
            }
        }

        throw new IllegalStateException("Could not find OverworldBiomeBuilder#addBiomes for Ascension datagen");
    }
}
