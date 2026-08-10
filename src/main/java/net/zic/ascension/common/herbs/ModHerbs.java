package net.zic.ascension.common.herbs;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.zic.ascension.AscensionCraft;

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
                            new HerbDefinition.AgeThreshold(1000, 0)
                    )
                    .wildAgeWeights(1000, 180, 24, 3, 1)
                    .naturalSupport(state -> state.is(BlockTags.GRASS_BLOCKS))
                    .qiCapacity(250.0D, 1100.0D)
                    .availableQiFraction(0.05D, 0.50D)
                    .spawnRule((level, pos, random) -> random.nextInt(6) == 0)
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
                            new HerbDefinition.AgeThreshold(1000, 0)
                    )
                    .wildAgeWeights(1200, 220, 28, 4, 1)
                    .naturalSupport(state -> state.is(BlockTags.GRASS_BLOCKS))
                    .qiCapacity(400.0D, 1400.0D)
                    .availableQiFraction(0.10D, 0.60D)
                    .spawnRule((level, pos, random) -> random.nextInt(10) == 0)
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
     * QUALITY
     *
     * This decides the herb's quality when the player harvests it.
     * Qualities: Feel Free to Change BTW
     *   POOR
     *   COMMON
     *   GOOD
     *   SUPERIOR
     *   PERFECT
     *
     * qiSuitability tells us how good the local Qi is for this herb.
     *   0.0 = terrible
     *   1.0 = perfect
     *
     * Example:
     * Better Qi = better herb.
     *
     * NOTE:
     * Replace MY_HERB with the actual herb definition.
     *
     * .quality((level, pos, state, wild) -> {
     *     double qiSuitability = MY_HERB.qiSuitability(level, pos);
     *
     *     if (wild && qiSuitability >= 0.95D) {
     *         return HerbDefinition.Quality.PERFECT;
     *     }
     *
     *     if (qiSuitability >= 0.80D) {
     *         return HerbDefinition.Quality.SUPERIOR;
     *     }
     *
     *     if (qiSuitability >= 0.55D) {
     *         return HerbDefinition.Quality.GOOD;
     *     }
     *
     *     if (qiSuitability >= 0.25D) {
     *         return HerbDefinition.Quality.COMMON;
     *     }
     *
     *     return HerbDefinition.Quality.POOR;
     * })
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
     * .qiAffinity(FIRE_PATH, 2.0D, 8.0D)
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
     * Ascension does not really consume chunk Qi yet,
     * so the chunk normally fills up and stays full.
     *
     * This will be more useful later when things start draining
     * atmospheric Qi.
     */

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
