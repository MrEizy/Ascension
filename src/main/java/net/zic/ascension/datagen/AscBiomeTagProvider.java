package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.ModTags;
import net.zic.ascension.worldgen.biome.AscBiomes;

import java.util.concurrent.CompletableFuture;

public class AscBiomeTagProvider extends BiomeTagsProvider {
    public AscBiomeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AscensionCraft.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Ascension biomes
        tag(ModTags.Biomes.MOUNTAIN_BIOMES)
                .add(
                        AscBiomes.GREYSTONE_FOOTHILLS,
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS
                );

        tag(ModTags.Biomes.FOOTHILL_BIOMES)
                .add(AscBiomes.GREYSTONE_FOOTHILLS);

        tag(ModTags.Biomes.MOUNTAIN_SLOPE_BIOMES)
                .add(AscBiomes.AZURE_CLOUD_RANGE);

        tag(ModTags.Biomes.MOUNTAIN_PEAK_BIOMES)
                .add(AscBiomes.HEAVENREACH_PEAKS);

        tag(ModTags.Biomes.FOREST_BIOMES)
                .add(
                        AscBiomes.ANCIENT_GROVE,
                        AscBiomes.JADEBLOOM_FOREST,
                        AscBiomes.MISTY_WOODS
                );

        tag(ModTags.Biomes.OPEN_COUNTRY_BIOMES)
                .add(
                        AscBiomes.VERDANT_MOOR,
                        AscBiomes.GOLDEN_STEPPE
                );

        tag(BiomeTags.IS_OVERWORLD)
                .add(
                        AscBiomes.GREYSTONE_FOOTHILLS,
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS,
                        AscBiomes.ANCIENT_GROVE,
                        AscBiomes.JADEBLOOM_FOREST,
                        AscBiomes.MISTY_WOODS,
                        AscBiomes.VERDANT_MOOR,
                        AscBiomes.GOLDEN_STEPPE
                );

        tag(BiomeTags.IS_MOUNTAIN)
                .add(
                        AscBiomes.GREYSTONE_FOOTHILLS,
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS
                );

        tag(BiomeTags.IS_HILL)
                .add(AscBiomes.GREYSTONE_FOOTHILLS);

        tag(BiomeTags.IS_FOREST)
                .add(
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.ANCIENT_GROVE,
                        AscBiomes.JADEBLOOM_FOREST,
                        AscBiomes.MISTY_WOODS
                );

        tag(BiomeTags.IS_TAIGA)
                .add(
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.MISTY_WOODS
                );

        tag(BiomeTags.HAS_MINESHAFT)
                .add(
                        AscBiomes.GREYSTONE_FOOTHILLS,
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS,
                        AscBiomes.ANCIENT_GROVE,
                        AscBiomes.JADEBLOOM_FOREST,
                        AscBiomes.MISTY_WOODS,
                        AscBiomes.VERDANT_MOOR,
                        AscBiomes.GOLDEN_STEPPE
                );

        tag(BiomeTags.HAS_STRONGHOLD)
                .add(
                        AscBiomes.GREYSTONE_FOOTHILLS,
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS,
                        AscBiomes.ANCIENT_GROVE,
                        AscBiomes.JADEBLOOM_FOREST,
                        AscBiomes.MISTY_WOODS,
                        AscBiomes.VERDANT_MOOR,
                        AscBiomes.GOLDEN_STEPPE
                );

        tag(BiomeTags.HAS_TRIAL_CHAMBERS)
                .add(
                        AscBiomes.GREYSTONE_FOOTHILLS,
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS,
                        AscBiomes.ANCIENT_GROVE,
                        AscBiomes.JADEBLOOM_FOREST,
                        AscBiomes.MISTY_WOODS,
                        AscBiomes.VERDANT_MOOR,
                        AscBiomes.GOLDEN_STEPPE
                );

        tag(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN)
                .add(
                        AscBiomes.GREYSTONE_FOOTHILLS,
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS
                );

        tag(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS)
                .add(
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS
                );

        tag(BiomeTags.SPAWNS_WHITE_RABBITS)
                .add(
                        AscBiomes.AZURE_CLOUD_RANGE,
                        AscBiomes.HEAVENREACH_PEAKS
                );

        tag(BiomeTags.SPAWNS_SNOW_FOXES)
                .add(AscBiomes.AZURE_CLOUD_RANGE);
    }
}
