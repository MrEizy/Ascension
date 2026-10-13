package net.zic.ascension.worldgen.surface;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.zic.ascension.worldgen.biome.AscBiomes;
import net.zic.ascension.worldgen.density.AscDensityFunctions;
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
    private static final SurfaceRules.RuleSource DEEPSLATE = state(Blocks.DEEPSLATE);
    private static final SurfaceRules.RuleSource CALCITE = state(Blocks.CALCITE);
    private static final SurfaceRules.RuleSource GRAVEL = state(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource COBBLESTONE = state(Blocks.COBBLESTONE);
    private static final SurfaceRules.RuleSource MOSSY_COBBLESTONE = state(Blocks.MOSSY_COBBLESTONE);
    private static final SurfaceRules.RuleSource SNOW = state(Blocks.SNOW_BLOCK);

    private AscSurfaceRules() {
    }

    public static SurfaceRules.RuleSource overworld(
            SurfaceRules.RuleSource vanillaFallback,
            HolderGetter<DensityFunction> densityFunctions
    ) {
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
        SurfaceRules.ConditionSource snowBroad = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_SNOW, -0.22D
        );
        SurfaceRules.ConditionSource snowMedium = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_SNOW, 0.14D
        );
        SurfaceRules.ConditionSource snowSparse = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_SNOW, 0.5D
        );
        SurfaceRules.ConditionSource strataLight = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_STRATA, 0.58D
        );
        SurfaceRules.ConditionSource strataDark = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_STRATA, -0.72D, -0.30D
        );
        SurfaceRules.ConditionSource broadStrataLight = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_BROAD_STRATA, 0.42D
        );
        SurfaceRules.ConditionSource broadStrataDark = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_BROAD_STRATA, -0.64D, -0.20D
        );
        SurfaceRules.ConditionSource provincePale = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_PROVINCES, 0.34D
        );
        SurfaceRules.ConditionSource provinceDark = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_PROVINCES, -1.0D, -0.34D
        );
        SurfaceRules.ConditionSource provinceWeathered = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_PROVINCES, -0.16D, 0.18D
        );
        SurfaceRules.ConditionSource weatherDark = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_WEATHERING, -0.72D, -0.28D
        );
        SurfaceRules.ConditionSource weatherLight = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_WEATHERING, 0.46D
        );
        SurfaceRules.ConditionSource scree = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_SCREE, 0.30D
        );
        SurfaceRules.ConditionSource heavyScree = SurfaceRules.noiseCondition(
                AscNoises.SURFACE_SCREE, 0.58D
        );

        SurfaceRules.ConditionSource mountain = density(
                densityFunctions, AscDensityFunctions.MOUNTAIN_MASK, 0.24D, 1.01D
        );
        SurfaceRules.ConditionSource mountainCore = density(
                densityFunctions, AscDensityFunctions.MOUNTAIN_MASK, 0.52D, 1.01D
        );
        SurfaceRules.ConditionSource ridge = density(
                densityFunctions, AscDensityFunctions.RIDGE_SCORE, 0.28D, 1.01D
        );
        SurfaceRules.ConditionSource strongRidge = density(
                densityFunctions, AscDensityFunctions.RIDGE_SCORE, 0.55D, 1.01D
        );
        SurfaceRules.ConditionSource valley = density(
                densityFunctions, AscDensityFunctions.VALLEY_SCORE, 0.58D, 1.01D
        );
        SurfaceRules.ConditionSource terrace = density(
                densityFunctions, AscDensityFunctions.TERRACE_MASK, 0.18D, 1.01D
        );

        SurfaceRules.RuleSource geology = geologicalSurface(
                mountain,
                mountainCore,
                ridge,
                strongRidge,
                valley,
                broadPositive,
                broadNegative,
                rock,
                strongRock,
                detail,
                snowBroad,
                snowMedium,
                snowSparse,
                strataLight,
                strataDark,
                broadStrataLight,
                broadStrataDark,
                provincePale,
                provinceDark,
                provinceWeathered,
                weatherDark,
                weatherLight,
                scree,
                heavyScree,
                terrace
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
                        SurfaceRules.sequence(geology, customBiomes)
                ),
                vanillaFallback
        );
    }

    private static SurfaceRules.RuleSource geologicalSurface(
            SurfaceRules.ConditionSource mountain,
            SurfaceRules.ConditionSource mountainCore,
            SurfaceRules.ConditionSource ridge,
            SurfaceRules.ConditionSource strongRidge,
            SurfaceRules.ConditionSource valley,
            SurfaceRules.ConditionSource broadPositive,
            SurfaceRules.ConditionSource broadNegative,
            SurfaceRules.ConditionSource rock,
            SurfaceRules.ConditionSource strongRock,
            SurfaceRules.ConditionSource detail,
            SurfaceRules.ConditionSource snowBroad,
            SurfaceRules.ConditionSource snowMedium,
            SurfaceRules.ConditionSource snowSparse,
            SurfaceRules.ConditionSource strataLight,
            SurfaceRules.ConditionSource strataDark,
            SurfaceRules.ConditionSource broadStrataLight,
            SurfaceRules.ConditionSource broadStrataDark,
            SurfaceRules.ConditionSource provincePale,
            SurfaceRules.ConditionSource provinceDark,
            SurfaceRules.ConditionSource provinceWeathered,
            SurfaceRules.ConditionSource weatherDark,
            SurfaceRules.ConditionSource weatherLight,
            SurfaceRules.ConditionSource scree,
            SurfaceRules.ConditionSource heavyScree,
            SurfaceRules.ConditionSource terrace
    ) {
        SurfaceRules.RuleSource paleRock = SurfaceRules.sequence(
                SurfaceRules.ifTrue(provinceDark, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(broadStrataDark, DEEPSLATE),
                        SurfaceRules.ifTrue(weatherDark, TUFF),
                        ANDESITE
                )),
                SurfaceRules.ifTrue(provinceWeathered, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, ANDESITE),
                        SurfaceRules.ifTrue(broadStrataDark, TUFF),
                        STONE
                )),
                SurfaceRules.ifTrue(provincePale, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(broadStrataLight, CALCITE),
                        SurfaceRules.ifTrue(weatherLight, CALCITE),
                        SurfaceRules.ifTrue(strataLight, ANDESITE),
                        STONE
                )),
                SurfaceRules.ifTrue(broadStrataLight, CALCITE),
                SurfaceRules.ifTrue(weatherLight, ANDESITE),
                STONE
        );

        SurfaceRules.RuleSource darkRock = SurfaceRules.sequence(
                SurfaceRules.ifTrue(provinceDark, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, DEEPSLATE),
                        SurfaceRules.ifTrue(broadStrataDark, DEEPSLATE),
                        SurfaceRules.ifTrue(strataDark, TUFF),
                        TUFF
                )),
                SurfaceRules.ifTrue(provinceWeathered, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, TUFF),
                        SurfaceRules.ifTrue(broadStrataDark, ANDESITE),
                        STONE
                )),
                SurfaceRules.ifTrue(provincePale, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(broadStrataLight, CALCITE),
                        SurfaceRules.ifTrue(weatherDark, ANDESITE),
                        STONE
                )),
                SurfaceRules.ifTrue(weatherDark, TUFF),
                SurfaceRules.ifTrue(strataDark, ANDESITE),
                STONE
        );

        SurfaceRules.RuleSource mixedRock = SurfaceRules.sequence(
                SurfaceRules.ifTrue(provinceDark, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, DEEPSLATE),
                        SurfaceRules.ifTrue(broadStrataLight, TUFF),
                        SurfaceRules.ifTrue(strataDark, DEEPSLATE),
                        ANDESITE
                )),
                SurfaceRules.ifTrue(provinceWeathered, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, TUFF),
                        SurfaceRules.ifTrue(broadStrataDark, ANDESITE),
                        SurfaceRules.ifTrue(detail, GRAVEL),
                        STONE
                )),
                SurfaceRules.ifTrue(provincePale, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(broadStrataLight, CALCITE),
                        SurfaceRules.ifTrue(weatherLight, ANDESITE),
                        SurfaceRules.ifTrue(strataDark, TUFF),
                        STONE
                )),
                SurfaceRules.ifTrue(weatherDark, ANDESITE),
                SurfaceRules.ifTrue(strataLight, TUFF),
                SurfaceRules.ifTrue(strataDark, ANDESITE),
                STONE
        );

        SurfaceRules.RuleSource cliff = stratifiedCliff(paleRock, darkRock, mixedRock);

        SurfaceRules.RuleSource ridgeRock = SurfaceRules.sequence(
                SurfaceRules.ifTrue(provinceDark, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, DEEPSLATE),
                        SurfaceRules.ifTrue(broadStrataDark, TUFF),
                        SurfaceRules.ifTrue(strongRock, DEEPSLATE),
                        STONE
                )),
                SurfaceRules.ifTrue(provinceWeathered, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, TUFF),
                        SurfaceRules.ifTrue(strongRock, ANDESITE),
                        SurfaceRules.ifTrue(detail, GRAVEL),
                        STONE
                )),
                SurfaceRules.ifTrue(provincePale, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(broadStrataLight, CALCITE),
                        SurfaceRules.ifTrue(weatherLight, ANDESITE),
                        SurfaceRules.ifTrue(strongRock, ANDESITE),
                        STONE
                )),
                SurfaceRules.ifTrue(weatherDark, TUFF),
                SurfaceRules.ifTrue(strataLight, ANDESITE),
                SurfaceRules.ifTrue(strongRock, ANDESITE),
                SurfaceRules.ifTrue(detail, GRAVEL),
                STONE
        );

        SurfaceRules.RuleSource alpineRock = SurfaceRules.sequence(
                SurfaceRules.ifTrue(provinceDark, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, DEEPSLATE),
                        SurfaceRules.ifTrue(strongRock, TUFF),
                        SurfaceRules.ifTrue(rock, ANDESITE),
                        STONE
                )),
                SurfaceRules.ifTrue(provinceWeathered, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(weatherDark, TUFF),
                        SurfaceRules.ifTrue(weatherLight, ANDESITE),
                        SurfaceRules.ifTrue(strongRock, ANDESITE),
                        SurfaceRules.ifTrue(detail, GRAVEL),
                        STONE
                )),
                SurfaceRules.ifTrue(provincePale, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(strongRock, CALCITE),
                        SurfaceRules.ifTrue(weatherLight, ANDESITE),
                        SurfaceRules.ifTrue(rock, STONE),
                        STONE
                )),
                SurfaceRules.ifTrue(weatherDark, TUFF),
                SurfaceRules.ifTrue(weatherLight, ANDESITE),
                SurfaceRules.ifTrue(strongRock, ANDESITE),
                SurfaceRules.ifTrue(rock, STONE),
                SurfaceRules.ifTrue(detail, GRAVEL),
                STONE
        );

        SurfaceRules.RuleSource screeTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(provinceDark, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(heavyScree, COBBLESTONE),
                        SurfaceRules.ifTrue(weatherDark, TUFF),
                        GRAVEL
                )),
                SurfaceRules.ifTrue(provinceWeathered, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(heavyScree, GRAVEL),
                        SurfaceRules.ifTrue(weatherDark, COBBLESTONE),
                        SurfaceRules.ifTrue(detail, COARSE_DIRT),
                        GRAVEL
                )),
                SurfaceRules.ifTrue(provincePale, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(heavyScree, GRAVEL),
                        SurfaceRules.ifTrue(weatherLight, ANDESITE),
                        GRAVEL
                )),
                SurfaceRules.ifTrue(heavyScree, GRAVEL),
                SurfaceRules.ifTrue(weatherDark, COBBLESTONE),
                SurfaceRules.ifTrue(detail, COARSE_DIRT),
                GRAVEL
        );

        SurfaceRules.RuleSource screeUnder = SurfaceRules.sequence(
                SurfaceRules.ifTrue(provinceDark, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(heavyScree, COBBLESTONE),
                        SurfaceRules.ifTrue(weatherDark, DEEPSLATE),
                        STONE
                )),
                SurfaceRules.ifTrue(provinceWeathered, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(heavyScree, GRAVEL),
                        SurfaceRules.ifTrue(weatherDark, COBBLESTONE),
                        STONE
                )),
                SurfaceRules.ifTrue(heavyScree, GRAVEL),
                SurfaceRules.ifTrue(weatherDark, COBBLESTONE),
                STONE
        );

        SurfaceRules.RuleSource shelfTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(weatherDark, COARSE_DIRT),
                SurfaceRules.ifTrue(broadPositive, MOSS),
                SurfaceRules.ifTrue(strataLight, GRASS),
                GRASS
        );

        SurfaceRules.RuleSource valleyTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(detail, GRAVEL),
                SurfaceRules.ifTrue(broadNegative, COARSE_DIRT),
                SurfaceRules.ifTrue(broadPositive, MOSS),
                GRASS
        );

        SurfaceRules.RuleSource top = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), cliff),
                snowRule(310, snowBroad),
                snowRule(260, snowMedium),
                snowRule(215, snowSparse),
                SurfaceRules.ifTrue(strongRidge, ridgeRock),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(150), 0),
                        SurfaceRules.ifTrue(ridge, ridgeRock)
                ),
                SurfaceRules.ifTrue(valley, valleyTop),
                SurfaceRules.ifTrue(terrace, shelfTop),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(108), 0),
                        SurfaceRules.ifTrue(
                                mountain,
                                SurfaceRules.ifTrue(scree, screeTop)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(180), 0),
                        SurfaceRules.ifTrue(mountainCore, alpineRock)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(248), 0),
                        SurfaceRules.ifTrue(mountain, alpineRock)
                )
        );

        SurfaceRules.RuleSource under = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.steep(), cliff),
                SurfaceRules.ifTrue(strongRidge, ridgeRock),
                SurfaceRules.ifTrue(terrace, DIRT),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(108), 0),
                        SurfaceRules.ifTrue(
                                mountain,
                                SurfaceRules.ifTrue(scree, screeUnder)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(156), 0),
                        SurfaceRules.ifTrue(mountainCore, mixedRock)
                ),
                SurfaceRules.ifTrue(valley, DIRT)
        );

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, top),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, under)
        );
    }

    private static SurfaceRules.RuleSource stratifiedCliff(
            SurfaceRules.RuleSource paleRock,
            SurfaceRules.RuleSource darkRock,
            SurfaceRules.RuleSource mixedRock
    ) {
        return SurfaceRules.sequence(
                strataBand(390, paleRock),
                strataBand(362, mixedRock),
                strataBand(324, darkRock),
                strataBand(300, mixedRock),
                strataBand(252, paleRock),
                strataBand(224, mixedRock),
                strataBand(176, darkRock),
                strataBand(150, mixedRock),
                strataBand(108, paleRock),
                strataBand(84, mixedRock),
                STONE
        );
    }

    private static SurfaceRules.RuleSource strataBand(int y, SurfaceRules.RuleSource material) {
        return SurfaceRules.ifTrue(
                SurfaceRules.yStartCheck(VerticalAnchor.absolute(y), 0),
                material
        );
    }

    private static SurfaceRules.RuleSource snowRule(int y, SurfaceRules.ConditionSource snowNoise) {
        return SurfaceRules.ifTrue(
                SurfaceRules.yStartCheck(VerticalAnchor.absolute(y), 0),
                SurfaceRules.ifTrue(
                        SurfaceRules.temperature(),
                        SurfaceRules.ifTrue(snowNoise, SNOW)
                )
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

    private static SurfaceRules.ConditionSource density(
            HolderGetter<DensityFunction> densityFunctions,
            ResourceKey<DensityFunction> key,
            double min,
            double max
    ) {
        return new DensitySurfaceCondition(
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key)),
                min,
                max
        );
    }

    private static SurfaceRules.RuleSource biome(
            ResourceKey<net.minecraft.world.level.biome.Biome> biome,
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

    private static SurfaceRules.RuleSource state(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
