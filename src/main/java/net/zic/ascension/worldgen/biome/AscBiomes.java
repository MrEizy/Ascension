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
    public static final ResourceKey<Biome> GREYSTONE_FOOTHILLS = key("greystone_foothills");
    public static final ResourceKey<Biome> AZURE_CLOUD_RANGE = key("azure_cloud_range");
    public static final ResourceKey<Biome> HEAVENREACH_PEAKS = key("heavenreach_peaks");

    private AscBiomes() {
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(GREYSTONE_FOOTHILLS, AscBiomeBuilder.greystoneFoothills(placedFeatures, carvers));
        context.register(AZURE_CLOUD_RANGE, AscBiomeBuilder.azureCloudRange(placedFeatures, carvers));
        context.register(HEAVENREACH_PEAKS, AscBiomeBuilder.heavenreachPeaks(placedFeatures, carvers));
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, AscensionCraft.prefix(name));
    }
}
