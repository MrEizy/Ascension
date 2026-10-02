package net.zic.ascension.worldgen.surface;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.zic.ascension.worldgen.biome.AscBiomes;
import net.zic.ascension.worldgen.noise.AscNoises;


public final class AscSurfaceRules {
    private static final SurfaceRules.RuleSource GRASS = state(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource DIRT = state(Blocks.DIRT);
    private static final SurfaceRules.RuleSource COARSE_DIRT = state(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource PODZOL = state(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource MOSS = state(Blocks.MOSS_BLOCK);
    private static final SurfaceRules.RuleSource STONE = state(Blocks.STONE);
    private static final SurfaceRules.RuleSource ANDESITE = state(Blocks.ANDESITE);
    private static final SurfaceRules.RuleSource TUFF = state(Blocks.TUFF);
    private static final SurfaceRules.RuleSource CALCITE = state(Blocks.CALCITE);
    private static final SurfaceRules.RuleSource GRAVEL = state(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource SNOW = state(Blocks.SNOW_BLOCK);

    private AscSurfaceRules() {
    }

    public static SurfaceRules.RuleSource overworld(SurfaceRules.RuleSource vanillaFallback) {
        SurfaceRules.ConditionSource broadPositive = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_PATCHES, 0.28D
        );
        SurfaceRules.ConditionSource broadStrong = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_PATCHES, 0.58D
        );
        SurfaceRules.ConditionSource broadNegative = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_PATCHES, -0.62D, -0.24D
        );
        SurfaceRules.ConditionSource rock = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_ROCK, 0.38D
        );
        SurfaceRules.ConditionSource strongRock = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_ROCK, 0.62D
        );
        SurfaceRules.ConditionSource detail = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_DETAIL, 0.48D
        );

        SurfaceRules.RuleSource customBiomes = SurfaceRules.sequence(
                greystoneFoothills(broadPositive, broadNegative, rock, strongRock),
                azureCloudRange(broadPositive, rock, detail),
                heavenreachPeaks(rock, strongRock, detail),
                ancientGrove(broadPositive, broadStrong, broadNegative, detail),
                jadebloomForest(broadPositive, broadStrong, broadNegative),
                mistyWoods(broadPositive, broadStrong, broadNegative, detail),
                verdantMoor(broadPositive, broadNegative, strongRock),
                goldenSteppe(broadPositive, broadNegative, rock, detail)
        );

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.abovePreliminarySurface(),
                        customBiomes
                ),
                vanillaFallback
        );
    }

    private static SurfaceRules.RuleSource greystoneFoothills(
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource broadNegative,
            SurfaceRules.ConditionSource rock,
            SurfaceRules.ConditionSource strongRock
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), STONE),
                SurfaceRules.ifTrue(strongRock, ANDESITE),
                SurfaceRules.ifTrue(rock, STONE),
                SurfaceRules.ifTrue(broadPositive, MOSS),
                SurfaceRules.ifTrue(broadNegative, COARSE_DIRT),
                GRASS
        );

        SurfaceRules.RuleSource under = SurfaceRules.sequence(
                SurfaceRules.ifTrue(strongRock, ANDESITE),
                SurfaceRules.ifTrue(rock, STONE),
                DIRT
        );

        return biome(AscBiomes.GREYSTONE_FOOTHILLS, top, under);
    }

    private static SurfaceRules.RuleSource azureCloudRange(
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource rock,
            SurfaceRules.ConditionSource detail
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), STONE),
                SurfaceRules.ifTrue(rock, ANDESITE),
                SurfaceRules.ifTrue(detail, TUFF),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(220), 0),
                        SurfaceRules.ifTrue(SurfaceRules.temperature(), SNOW)
                ),
                SurfaceRules.ifTrue(broadPositive, PODZOL),
                GRASS
        );

        SurfaceRules.RuleSource under = SurfaceRules.sequence(
                SurfaceRules.ifTrue(rock, STONE),
                DIRT
        );

        return biome(AscBiomes.AZURE_CLOUD_RANGE, top, under);
    }

    private static SurfaceRules.RuleSource heavenreachPeaks(
            SurfaceRules.ConditionSource rock,
            SurfaceRules.ConditionSource strongRock,
            SurfaceRules.ConditionSource detail
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), STONE),
                SurfaceRules.ifTrue(strongRock, CALCITE),
                SurfaceRules.ifTrue(rock, TUFF),
                SurfaceRules.ifTrue(detail, ANDESITE),
                SurfaceRules.ifTrue(SurfaceRules.temperature(), SNOW),
                STONE
        );

        SurfaceRules.RuleSource under = SurfaceRules.sequence(
                SurfaceRules.ifTrue(strongRock, CALCITE),
                SurfaceRules.ifTrue(rock, TUFF),
                STONE
        );

        return biome(AscBiomes.HEAVENREACH_PEAKS, top, under);
    }

    private static SurfaceRules.RuleSource ancientGrove(
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource broadStrong,
            SurfaceRules.ConditionSource broadNegative,
            SurfaceRules.ConditionSource detail
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(broadStrong, MOSS),
                SurfaceRules.ifTrue(detail, PODZOL),
                SurfaceRules.ifTrue(broadPositive, MOSS),
                SurfaceRules.ifTrue(broadNegative, COARSE_DIRT),
                GRASS
        );

        return biome(AscBiomes.ANCIENT_GROVE, top, DIRT);
    }

    private static SurfaceRules.RuleSource jadebloomForest(
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource broadStrong,
            SurfaceRules.ConditionSource broadNegative
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(broadStrong, MOSS),
                SurfaceRules.ifTrue(broadPositive, GRASS),
                SurfaceRules.ifTrue(broadNegative, COARSE_DIRT),
                GRASS
        );

        return biome(AscBiomes.JADEBLOOM_FOREST, top, DIRT);
    }

    private static SurfaceRules.RuleSource mistyWoods(
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource broadStrong,
            SurfaceRules.ConditionSource broadNegative,
            SurfaceRules.ConditionSource detail
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(broadStrong, MOSS),
                SurfaceRules.ifTrue(detail, PODZOL),
                SurfaceRules.ifTrue(broadPositive, PODZOL),
                SurfaceRules.ifTrue(broadNegative, COARSE_DIRT),
                GRASS
        );

        return biome(AscBiomes.MISTY_WOODS, top, DIRT);
    }

    private static SurfaceRules.RuleSource verdantMoor(
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource broadNegative,
            SurfaceRules.ConditionSource strongRock
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), STONE),
                SurfaceRules.ifTrue(strongRock, STONE),
                SurfaceRules.ifTrue(broadPositive, MOSS),
                SurfaceRules.ifTrue(broadNegative, COARSE_DIRT),
                GRASS
        );

        return biome(AscBiomes.VERDANT_MOOR, top, DIRT);
    }

    private static SurfaceRules.RuleSource goldenSteppe(
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource broadNegative,
            SurfaceRules.ConditionSource rock,
            SurfaceRules.ConditionSource detail
    ) {
        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), STONE),
                SurfaceRules.ifTrue(rock, ANDESITE),
                SurfaceRules.ifTrue(broadPositive, COARSE_DIRT),
                SurfaceRules.ifTrue(detail, GRAVEL),
                SurfaceRules.ifTrue(broadNegative, COARSE_DIRT),
                GRASS
        );

        SurfaceRules.RuleSource under = SurfaceRules.sequence(
                SurfaceRules.ifTrue(rock, STONE),
                DIRT
        );

        return biome(AscBiomes.GOLDEN_STEPPE, top, under);
    }

    private static SurfaceRules.RuleSource biome(
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biome,
            SurfaceRules.RuleSource top,
            SurfaceRules.RuleSource under
    ) {
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(biome),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, top),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, under)
                )
        );
    }

    private static SurfaceRules.RuleSource state(net.minecraft.world.level.block.Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
