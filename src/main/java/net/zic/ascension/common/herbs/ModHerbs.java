package net.zic.ascension.common.herbs;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyAffinities;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyProperties;
import net.zic.ascension.common.blocks.ModBlocks;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModHerbs {
    private static final Map<Identifier, HerbDefinition> HERBS = new LinkedHashMap<>();

    public static final HerbDefinition JADE_DEW_GRASS = register(
            HerbDefinition.builder(AscensionCraft.prefix("jade_dew_grass"))
                    .growthStages(8)
                    .baseGrowthChance(0.25F)
                    .planting(HerbDefinition.PlantingType.SEED)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 12),
                            new HerbDefinition.AgeThreshold(10, 48),
                            new HerbDefinition.AgeThreshold(100, 192),
                            new HerbDefinition.AgeThreshold(500, 768),
                            new HerbDefinition.AgeThreshold(1000, 7680),
                            new HerbDefinition.AgeThreshold(10000, 76800),
                            new HerbDefinition.AgeThreshold(100000, 768000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1000, 180, 24, 3, 1, 1, 1, 1)
                    .naturalSupport(state -> state.is(BlockTags.GRASS_BLOCKS))
                    .spawnRule((level, pos, random) -> random.nextInt(6) == 0)
                    .alchemyProperty(AlchemyProperties.CLEANSING, 1.5D)
                    .alchemyProperty(AlchemyProperties.RESTORATION, 0.5D)
                    .alchemyAffinity(AlchemyAffinities.WATER, 0.5D)
                    .alchemy(0.9D, 0.03D, 0.5D)
                    .build()
    );
    public static final HerbDefinition PEACH = register(
            HerbDefinition.builder(AscensionCraft.prefix("peach"))
                    .growthStages(4)
                    .baseGrowthChance(1F)
                    .planting(HerbDefinition.PlantingType.NONE)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 12),
                            new HerbDefinition.AgeThreshold(10, 48),
                            new HerbDefinition.AgeThreshold(100, 192),
                            new HerbDefinition.AgeThreshold(500, 768),
                            new HerbDefinition.AgeThreshold(1000, 7680),
                            new HerbDefinition.AgeThreshold(10000, 76800),
                            new HerbDefinition.AgeThreshold(100000, 768000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1000, 180, 24, 3, 1, 1, 1, 1)
                    .naturalSupport(state -> state.is(ModBlocks.PEACH_LEAVES.get()))
                    .alchemyProperty(AlchemyProperties.CLEANSING, 7.0D)
                    .alchemyProperty(AlchemyProperties.NOURISHMENT, 3.0D)
                    .alchemyProperty(AlchemyProperties.RESTORATION, 2.0D)
                    .alchemyAffinity(AlchemyAffinities.LIFE, 3.0D)
                    .alchemy(0.88D, 0.12D, 1.4D)
                    .build()
    );

    public static final HerbDefinition NINE_SUN_FIRE_ROOT = register(
            HerbDefinition.builder(AscensionCraft.prefix("nine_sun_fire_root"))
                    .growthStages(4)
                    .baseGrowthChance(0.12F)
                    .planting(HerbDefinition.PlantingType.DIRECT)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 24),
                            new HerbDefinition.AgeThreshold(10, 96),
                            new HerbDefinition.AgeThreshold(100, 384),
                            new HerbDefinition.AgeThreshold(500, 1536),
                            new HerbDefinition.AgeThreshold(1000, 15360),
                            new HerbDefinition.AgeThreshold(10000, 153600),
                            new HerbDefinition.AgeThreshold(100000, 1536000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1100, 170, 20, 3, 1, 1, 1, 1)
                    .qualityGrowth(32, 128, 512, 2048)
                    .wildQualityWeights(15, 900, 180, 20, 2)
                    .qualityAffinity(AlchemyAffinities.FIRE, 6.0D)
                    .qiCapacity(100.0D, 900.0D)
                    .availableQiFraction(0.02D, 0.15D)
                    .atmosphericQiCost(3.0D)
                    .naturalSupport(state -> state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.TERRACOTTA))
                    .spawnRule((level, pos, random) -> pos.getY() >= 62 && level.canSeeSky(pos) && random.nextInt(8) == 0)
                    .growthModifier((level, pos, state) -> {
                        boolean nearLava = hasFluidNearby(level, pos, FluidTags.LAVA, 4, 2);
                        long time = Math.floorMod(level.getGameTime(), 24000L);
                        boolean day = time < 12000L;

                        double multiplier = day ? 1.25D : 0.45D;
                        if (nearLava) {
                            multiplier *= 1.5D;
                        }
                        return multiplier;
                    })
                    .qualityModifier((level, pos, state, wild) -> {
                        double multiplier = hasFluidNearby(level, pos, FluidTags.LAVA, 4, 2) ? 1.5D : 0.8D;
                        if (wild) {
                            multiplier *= 1.15D;
                        }
                        return multiplier;
                    })
                    .alchemyProperty(AlchemyProperties.REINFORCEMENT, 4.0D)
                    .alchemyProperty(AlchemyProperties.CIRCULATION, 3.0D)
                    .alchemyAffinity(AlchemyAffinities.FIRE, 6.0D)
                    .alchemyAffinity(AlchemyAffinities.YANG, 2.0D)
                    .alchemy(0.82D, 0.22D, 2.0D)
                    .build()
    );

    public static final HerbDefinition MOONWELL_JADE_LOTUS = register(
            HerbDefinition.builder(AscensionCraft.prefix("moonwell_jade_lotus"))
                    .growthStages(4)
                    .baseGrowthChance(0.14F)
                    .planting(HerbDefinition.PlantingType.DIRECT)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 18),
                            new HerbDefinition.AgeThreshold(10, 72),
                            new HerbDefinition.AgeThreshold(100, 288),
                            new HerbDefinition.AgeThreshold(500, 1152),
                            new HerbDefinition.AgeThreshold(1000, 11520),
                            new HerbDefinition.AgeThreshold(10000, 115200),
                            new HerbDefinition.AgeThreshold(100000, 1152000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1000, 210, 35, 6, 2, 1, 1, 1)
                    .qualityGrowth(18, 72, 288, 1152)
                    .wildQualityWeights(8, 650, 260, 70, 12)
                    .qiCapacity(100.0D, 800.0D)
                    .qiAffinity(AlchemyAffinities.WATER, 0.5D, 2.0D)
                    .qualityAffinity(AlchemyAffinities.WATER, 2.0D)
                    .availableQiFraction(0.02D, 0.12D)
                    .atmosphericQiCost(2.0D)
                    .naturalSupport(state -> state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MUD) || state.is(Blocks.CLAY))
                    .spawnRule((level, pos, random) -> level.canSeeSky(pos) && hasFluidNearby(level, pos, FluidTags.WATER, 3, 1) && random.nextInt(5) == 0)
                    .growthModifier((level, pos, state) -> {
                        long time = Math.floorMod(level.getGameTime(), 24000L);
                        boolean night = time >= 13000L && time <= 23000L;

                        double multiplier = night ? 1.65D : 0.55D;
                        if (level.isRainingAt(pos.above())) {
                            multiplier *= 1.25D;
                        }
                        if (!hasFluidNearby(level, pos, FluidTags.WATER, 3, 1)) {
                            multiplier *= 0.5D;
                        }
                        return multiplier;
                    })
                    .qualityModifier((level, pos, state, wild) -> {
                        long time = Math.floorMod(level.getGameTime(), 24000L);
                        boolean moonlit = time >= 13000L && time <= 23000L && level.canSeeSky(pos);

                        double multiplier = moonlit ? 1.75D : 0.65D;
                        if (level.isRainingAt(pos.above())) {
                            multiplier += 0.25D;
                        }
                        if (wild) {
                            multiplier += 0.25D;
                        }
                        return multiplier;
                    })
                    .alchemyProperty(AlchemyProperties.CLEANSING, 4.0D)
                    .alchemyProperty(AlchemyProperties.NOURISHMENT, 4.0D)
                    .alchemyProperty(AlchemyProperties.RESTORATION, 3.0D)
                    .alchemyAffinity(AlchemyAffinities.WATER, 4.0D)
                    .alchemyAffinity(AlchemyAffinities.MOON, 2.0D)
                    .alchemyAffinity(AlchemyAffinities.YIN, 1.0D)
                    .alchemy(0.92D, 0.08D, 1.6D)
                    .build()
    );

    public static final HerbDefinition HEAVENLY_THUNDER_PEACH = register(
            HerbDefinition.builder(AscensionCraft.prefix("heavenly_thunder_peach"))
                    .growthStages(4)
                    .baseGrowthChance(0.16F)
                    .planting(HerbDefinition.PlantingType.NONE)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 36),
                            new HerbDefinition.AgeThreshold(10, 144),
                            new HerbDefinition.AgeThreshold(100, 576),
                            new HerbDefinition.AgeThreshold(500, 2304),
                            new HerbDefinition.AgeThreshold(1000, 23040),
                            new HerbDefinition.AgeThreshold(10000, 230400),
                            new HerbDefinition.AgeThreshold(100000, 2304000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(900, 220, 45, 8, 2, 1, 1, 1)
                    .qualityGrowth(48, 192, 768, 3072)
                    .wildQualityWeights(10, 700, 220, 60, 10)
                    .qualityAffinity(AlchemyAffinities.LIGHTNING, 8.0D)
                    .qiCapacity(250.0D, 1100.0D)
                    .availableQiFraction(0.05D, 0.35D)
                    .atmosphericQiCost(8.0D)
                    .naturalSupport(state -> state.is(ModBlocks.PEACH_LEAVES.get()))
                    .growthModifier((level, pos, state) -> {
                        if (level.isThundering()) {
                            return 3.0D;
                        }
                        if (level.isRaining()) {
                            return 1.0D;
                        }
                        return 0.20D;
                    })
                    .qualityModifier((level, pos, state, wild) -> {
                        double multiplier;
                        if (level.isThundering()) {
                            multiplier = 3.0D;
                        } else if (level.isRaining()) {
                            multiplier = 1.25D;
                        } else {
                            multiplier = 0.5D;
                        }

                        if (wild) {
                            multiplier *= 1.20D;
                        }
                        return multiplier;
                    })
                    .alchemyProperty(AlchemyProperties.CIRCULATION, 5.0D)
                    .alchemyProperty(AlchemyProperties.REINFORCEMENT, 3.0D)
                    .alchemyProperty(AlchemyProperties.NOURISHMENT, 3.0D)
                    .alchemyAffinity(AlchemyAffinities.LIGHTNING, 6.0D)
                    .alchemyAffinity(AlchemyAffinities.YANG, 1.5D)
                    .alchemy(0.84D, 0.3D, 2.4D)
                    .build()
    );


    public static final HerbDefinition GINSENG = register(
            HerbDefinition.builder(AscensionCraft.prefix("ginseng"))
                    .growthStages(4)
                    .baseGrowthChance(0.18F)
                    .planting(HerbDefinition.PlantingType.DIRECT)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 18),
                            new HerbDefinition.AgeThreshold(10, 72),
                            new HerbDefinition.AgeThreshold(100, 288),
                            new HerbDefinition.AgeThreshold(500, 1152),
                            new HerbDefinition.AgeThreshold(1000, 11520),
                            new HerbDefinition.AgeThreshold(10000, 115200),
                            new HerbDefinition.AgeThreshold(100000, 1152000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1200, 220, 28, 4, 1, 1, 1, 1)
                    .qualityGrowth(24, 96, 384, 1536)
                    .wildQualityWeights(20, 1000, 160, 12, 1)
                    .qualityAffinity(AlchemyAffinities.WOOD, 6.0D)
                    .naturalSupport(state -> state.is(BlockTags.GRASS_BLOCKS))
                    .spawnRule((level, pos, random) -> random.nextInt(10) == 0)
                    .alchemyProperty(AlchemyProperties.REINFORCEMENT, 6.0D)
                    .alchemyProperty(AlchemyProperties.CLEANSING, 4.0D)
                    .alchemyProperty(AlchemyProperties.NOURISHMENT, 2.0D)
                    .alchemyAffinity(AlchemyAffinities.WOOD, 3.0D)
                    .alchemyAffinity(AlchemyAffinities.LIFE, 1.0D)
                    .alchemy(0.86D, 0.08D, 1.0D)
                    .build()
    );
    public static final HerbDefinition FIRE_GINSENG = register(
            HerbDefinition.builder(AscensionCraft.prefix("fire_ginseng"))
                    .growthStages(4)
                    .baseGrowthChance(0.18F)
                    .planting(HerbDefinition.PlantingType.DIRECT)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 18),
                            new HerbDefinition.AgeThreshold(10, 72),
                            new HerbDefinition.AgeThreshold(100, 288),
                            new HerbDefinition.AgeThreshold(500, 1152),
                            new HerbDefinition.AgeThreshold(1000, 11520),
                            new HerbDefinition.AgeThreshold(10000, 115200),
                            new HerbDefinition.AgeThreshold(100000, 1152000),
                            new HerbDefinition.AgeThreshold(1000000, 0)

                    )
                    .wildAgeWeights(1200, 220, 28, 4, 1, 1, 1, 1)
                    .qualityGrowth(24, 96, 384, 1536)
                    .wildQualityWeights(20, 1000, 160, 12, 1)
                    .qualityAffinity(AlchemyAffinities.FIRE, 6.0D)
                    .naturalSupport(state -> state.is(Blocks.SAND))
                    .spawnRule((level, pos, random) -> random.nextInt(10) == 0)
                    .alchemyProperty(AlchemyProperties.CIRCULATION, 5.0D)
                    .alchemyProperty(AlchemyProperties.CLEANSING, 5.0D)
                    .alchemyAffinity(AlchemyAffinities.FIRE, 4.0D)
                    .alchemyAffinity(AlchemyAffinities.YANG, 1.0D)
                    .alchemy(0.84D, 0.15D, 1.25D)
                    .build()
    );
    public static final HerbDefinition SNOW_GINSENG = register(
            HerbDefinition.builder(AscensionCraft.prefix("snow_ginseng"))
                    .growthStages(4)
                    .baseGrowthChance(0.18F)
                    .planting(HerbDefinition.PlantingType.DIRECT)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 18),
                            new HerbDefinition.AgeThreshold(10, 72),
                            new HerbDefinition.AgeThreshold(100, 288),
                            new HerbDefinition.AgeThreshold(500, 1152),
                            new HerbDefinition.AgeThreshold(1000, 11520),
                            new HerbDefinition.AgeThreshold(10000, 115200),
                            new HerbDefinition.AgeThreshold(100000, 1152000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1200, 220, 28, 4, 1, 1, 1, 1)
                    .qualityGrowth(24, 96, 384, 1536)
                    .wildQualityWeights(20, 1000, 160, 12, 1)
                    .qualityAffinity(AlchemyAffinities.ICE, 6.0D)
                    .naturalSupport(state -> state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SNOW_BLOCK))
                    .spawnRule((level, pos, random) -> random.nextInt(10) == 0)
                    .alchemyProperty(AlchemyProperties.CIRCULATION, 4.0D)
                    .alchemyProperty(AlchemyProperties.NOURISHMENT, 3.0D)
                    .alchemyProperty(AlchemyProperties.CLEANSING, 6.0D)
                    .alchemyAffinity(AlchemyAffinities.ICE, 4.0D)
                    .alchemyAffinity(AlchemyAffinities.YIN, 1.0D)
                    .alchemy(0.88D, 0.12D, 1.25D)
                    .build()
    );
    public static final HerbDefinition WHITE_JADE_ORCHID = register(
            HerbDefinition.builder(AscensionCraft.prefix("white_jade_orchid"))
                    .growthStages(4)
                    .baseGrowthChance(0.18F)
                    .planting(HerbDefinition.PlantingType.DIRECT)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 18),
                            new HerbDefinition.AgeThreshold(10, 72),
                            new HerbDefinition.AgeThreshold(100, 288),
                            new HerbDefinition.AgeThreshold(500, 1152),
                            new HerbDefinition.AgeThreshold(1000, 11520),
                            new HerbDefinition.AgeThreshold(10000, 115200),
                            new HerbDefinition.AgeThreshold(100000, 1152000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1200, 220, 28, 4, 1, 1, 1, 1)
                    .naturalSupport(state -> state.is(Blocks.GRASS_BLOCK))
                    .spawnRule((level, pos, random) -> random.nextInt(10) == 0)
                    .alchemyProperty(AlchemyProperties.CLEANSING, 13.0D)
                    .alchemyAffinity(AlchemyAffinities.METAL, 2.0D)
                    .alchemy(0.95D, 0.04D, 1.1D)
                    .build()
    );


    public static final HerbDefinition LINGZHI_MUSHROOM = register(
            HerbDefinition.builder(AscensionCraft.prefix("lingzhi_mushroom"))
                    .growthStages(1)
                    .baseGrowthChance(1.0F)
                    .planting(HerbDefinition.PlantingType.NONE)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 24),
                            new HerbDefinition.AgeThreshold(10, 96),
                            new HerbDefinition.AgeThreshold(100, 384),
                            new HerbDefinition.AgeThreshold(500, 1536),
                            new HerbDefinition.AgeThreshold(1000, 15360),
                            new HerbDefinition.AgeThreshold(10000, 153600),
                            new HerbDefinition.AgeThreshold(100000, 1536000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1400, 180, 20, 2, 1, 1, 1, 1)
                    .naturalSupport(state -> state.is(BlockTags.LOGS))
                    .alchemyProperty(AlchemyProperties.RESTORATION, 5.0D)
                    .alchemyProperty(AlchemyProperties.NOURISHMENT, 6.0D)
                    .alchemyAffinity(AlchemyAffinities.LIFE, 3.0D)
                    .alchemyAffinity(AlchemyAffinities.WOOD, 1.0D)
                    .alchemy(0.86D, 0.06D, 1.0D)
                    .build()
    );

    public static final HerbDefinition BLOOD_LINGZHI_MUSHROOM = register(
            HerbDefinition.builder(AscensionCraft.prefix("blood_lingzhi_mushroom"))
                    .growthStages(1)
                    .baseGrowthChance(1.0F)
                    .planting(HerbDefinition.PlantingType.NONE)
                    .ageThresholds(
                            new HerbDefinition.AgeThreshold(1, 36),
                            new HerbDefinition.AgeThreshold(10, 144),
                            new HerbDefinition.AgeThreshold(100, 576),
                            new HerbDefinition.AgeThreshold(500, 2304),
                            new HerbDefinition.AgeThreshold(1000, 23040),
                            new HerbDefinition.AgeThreshold(10000, 230400),
                            new HerbDefinition.AgeThreshold(100000, 2304000),
                            new HerbDefinition.AgeThreshold(1000000, 0)
                    )
                    .wildAgeWeights(1600, 160, 16, 2, 1, 1, 1, 1)
                    .naturalSupport(state -> state.is(Blocks.BONE_BLOCK))
                    .alchemyProperty(AlchemyProperties.NOURISHMENT, 11.0D)
                    .alchemyProperty(AlchemyProperties.REINFORCEMENT, 3.0D)
                    .alchemyAffinity(AlchemyAffinities.BLOOD, 5.0D)
                    .alchemyAffinity(AlchemyAffinities.LIFE, 1.0D)
                    .alchemy(0.8D, 0.18D, 1.5D)
                    .build()
    );

    /*
     * EXTRA HERB RULE EXAMPLES
     *
     * You do NOT need these for every herb.
     * Only use these when the herb has special rules.
     *
     *
     * ---
     * GROWTH MODIFIER
     *
     * This changes how fast the herb grows.
     *
     * Return:
     *   0.0 = does not grow
     *   0.5 = grows half as fast
     *   1.0 = normal speed
     *   1.5 = 50% faster
     *   2.0 = twice as fast
     *
     * Example:
     * This herb likes water.
     *
     * .growthModifier((level, pos, state) -> {
     *
     *     boolean raining = level.isRainingAt(pos.above());
     *
     *     boolean nearWater = BlockPos.betweenClosedStream(
     *             pos.offset(-2, -1, -2),
     *             pos.offset(2, 1, 2)
     *     ).anyMatch(checkPos ->
     *             level.getBlockState(checkPos)
     *                     .getFluidState()
     *                     .is(FluidTags.WATER)
     *     );
     *
     *     if (raining && nearWater) {
     *         return 1.5D; // Very happy. Grows faster.
     *     }
     *
     *     if (!nearWater) {
     *         return 0.5D; // No water nearby. Grows slower.
     *     }
     *
     *     return 1.0D; // Normal growth speed.
     * })
     *
     *
     * ---
     * SPAWN RULE
     *
     * This decides where the herb is allowed to spawn naturally.
     *
     * true  = yes, it can spawn here
     * false = no, it cannot spawn here
     *
     * Example:
     * This herb only spawns high up and needs to see the sky.
     *
     * .spawnRule((level, pos, random) -> {
     *
     *     if (pos.getY() < 90) {
     *         return false; // Too low.
     *     }
     *
     *     if (!level.canSeeSky(pos)) {
     *         return false; // Something is above it.
     *     }
     *
     *     // 1 in 4 chance to spawn.
     *     return random.nextInt(4) == 0;
     * })
     *
     *
     * ---
     * QUALITY GROWTH
     *
     * Normal cultivated herbs start COMMON. Wild herbs use wildQualityWeights(...).
     *
     * Easy setup:
     *
     * .qualityGrowth(24, 96, 384, 1536)
     *
     * The four numbers are average RANDOM TICKS for:
     *   Poor -> Common
     *   Common -> Good
     *   Good -> Superior
     *   Superior -> Perfect
     *
     * All herbs already have these values as defaults, so this line is only needed
     * when a species should improve quality faster/slower.
     *
     * Wild starting quality:
     *
     * .wildQualityWeights(20, 1000, 160, 12, 1)
     *
     * Order is:
     *   Poor, Common, Good, Superior, Perfect
     *
     * The numbers are relative weights, exactly like wildAgeWeights(...).
     *
     * Optional quality-only Qi bonus:
     *
     * .qualityAffinity(AlchemyAffinities.FIRE, 6.0D)
     *
     * This is NOT a requirement. Zero Fire Qi still lets quality grow normally.
     * At 6+ Fire affinity, quality grows up to twice as fast.
     *
     * Optional custom quality modifier for weird herbs:
     *
     * .qualityModifier((level, pos, state, wild) -> wild ? 1.25D : 1.0D)
     *
     * 0.0 stops quality growth, 0.5 halves it, 2.0 doubles it, etc.
     *
     * Optional static cap:
     *
     * .qualityCap(HerbDefinition.Quality.SUPERIOR)
     *
     *
     * ---
     * QI CAPACITY
     *
     * Easy Qi setting.
     *
     * Example:
     *
     * .qiCapacity(400.0D, 1400.0D)
     *
     * First number:
     *   Minimum Qi the herb wants.
     *
     * Second number:
     *   Amount of Qi where the herb is completely happy.
     *
     * So:
     *   below 400  = bad
     *   400-1400   = gets better as Qi increases
     *   1400+      = perfect amount
     *
     *
     * ---
     * QI AFFINITY
     *
     * Use this when a herb likes a certain TYPE of Qi.
     *
     * Examples:
     *   Fire herb -> likes Fire Qi
     *   Yin herb  -> likes Yin Qi
     *   Soul herb -> likes Soul Qi
     *
     * Example:
     * .qiAffinity(AlchemyAffinities.FIRE, 2.0D, 8.0D)
     *
     * First number:
     *   Minimum amount of Fire Qi it wants.
     *
     * Second number:
     *   Amount where it is completely happy.
     *
     * So:
     *   below 2 Fire Qi = bad
     *   2-8 Fire Qi     = gets better
     *   8+ Fire Qi      = perfect
     *
     * ---
     * AVAILABLE QI FRACTION
     *
     * .availableQiFraction(minimum, ideal)
     *
     * IGNORE THIS FOR NOW.
     *
     * This checks how full the chunk's current Qi storage is.
     *
     * Most Ascension systems still do not consume chunk Qi yet. Herbs only drain it if
     * atmosphericQiCost(...) is explicitly configured, so this is still mostly future-facing.
     *
     *
     * ---
     * ATMOSPHERIC QI CONSUMPTION
     *
     * Optional. Leave this out for normal herbs.
     *
     * .atmosphericQiCost(2.0D)
     *
     * The herb consumes 2 raw atmospheric Qi only when a visual-growth, age, or quality
     * advancement roll SUCCEEDS. If the chunk cannot pay the cost, that advancement simply does not happen.
     */

    private static boolean hasFluidNearby(
            BlockGetter level,
            BlockPos center,
            TagKey<Fluid> fluidTag,
            int horizontalRadius,
            int verticalRadius
    ) {
        return BlockPos.betweenClosedStream(
                center.offset(-horizontalRadius, -verticalRadius, -horizontalRadius),
                center.offset(horizontalRadius, verticalRadius, horizontalRadius)
        ).anyMatch(pos -> level.getFluidState(pos).is(fluidTag));
    }

    private ModHerbs() {
    }

    private static HerbDefinition register(HerbDefinition definition) {
        HerbDefinition previous = HERBS.put(definition.id(), definition);
        if (previous != null) {
            throw new IllegalStateException("Duplicate herb definition: " + definition.id());
        }
        return definition;
    }

    public static HerbDefinition get(Identifier id) {
        return HERBS.get(id);
    }

    public static Map<Identifier, HerbDefinition> all() {
        return Collections.unmodifiableMap(HERBS);
    }
}
