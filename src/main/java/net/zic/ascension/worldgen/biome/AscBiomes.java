package net.zic.ascension.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.zic.ascension.AscensionCraft;

public final class AscBiomes {
    // Mountains
    public static final ResourceKey<Biome> GREYSTONE_FOOTHILLS = key("greystone_foothills");
    public static final ResourceKey<Biome> AZURE_CLOUD_RANGE = key("azure_cloud_range");
    public static final ResourceKey<Biome> HEAVENREACH_PEAKS = key("heavenreach_peaks");

    // Forests
    public static final ResourceKey<Biome> ANCIENT_GROVE = key("ancient_grove");
    public static final ResourceKey<Biome> JADEBLOOM_FOREST = key("jadebloom_forest");
    public static final ResourceKey<Biome> MISTY_WOODS = key("misty_woods");

    // Open country
    public static final ResourceKey<Biome> VERDANT_MOOR = key("verdant_moor");
    public static final ResourceKey<Biome> GOLDEN_STEPPE = key("golden_steppe");

    private AscBiomes() {
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(GREYSTONE_FOOTHILLS, AscBiomeBuilder.greystoneFoothills(placedFeatures, carvers));
        context.register(AZURE_CLOUD_RANGE, AscBiomeBuilder.azureCloudRange(placedFeatures, carvers));
        context.register(HEAVENREACH_PEAKS, AscBiomeBuilder.heavenreachPeaks(placedFeatures, carvers));

        context.register(ANCIENT_GROVE, AscBiomeBuilder.ancientGrove(placedFeatures, carvers));
        context.register(JADEBLOOM_FOREST, AscBiomeBuilder.jadebloomForest(placedFeatures, carvers));
        context.register(MISTY_WOODS, AscBiomeBuilder.mistyWoods(placedFeatures, carvers));

        context.register(VERDANT_MOOR, AscBiomeBuilder.verdantMoor(placedFeatures, carvers));
        context.register(GOLDEN_STEPPE, AscBiomeBuilder.goldenSteppe(placedFeatures, carvers));
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, AscensionCraft.prefix(name));
    }
}
