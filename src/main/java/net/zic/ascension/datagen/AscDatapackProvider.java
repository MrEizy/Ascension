package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.AscBiomeModifier;
import net.zic.ascension.worldgen.AscConfiguredFeatures;
import net.zic.ascension.worldgen.AscPlacedFeatures;
import net.zic.ascension.worldgen.biome.AscBiomes;
import net.zic.ascension.worldgen.density.AscDensityFunctions;
import net.zic.ascension.worldgen.dimension.AscDimensionTypes;
import net.zic.ascension.worldgen.dimension.AscNoiseSettings;
import net.zic.ascension.worldgen.dimension.AscWorldPresets;
import net.zic.ascension.worldgen.noise.AscNoises;

import java.util.Set;
import java.util.concurrent.CompletableFuture;


public class AscDatapackProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.NOISE, AscNoises::bootstrap)
            .add(Registries.DENSITY_FUNCTION, AscDensityFunctions::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, AscConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, AscPlacedFeatures::bootstrap)
            .add(Registries.BIOME, AscBiomes::bootstrap)
            .add(Registries.DIMENSION_TYPE, AscDimensionTypes::bootstrap)
            .add(Registries.NOISE_SETTINGS, AscNoiseSettings::bootstrap)
            .add(Registries.WORLD_PRESET, AscWorldPresets::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AscBiomeModifier::bootstrap);

    public AscDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(AscensionCraft.MOD_ID));
    }
}
