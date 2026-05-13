package net.thejadeproject.ascension.mob_cultivation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Random;

public final class MobCultivationRoller {
    private static final Random RANDOM = new Random();

    private static final List<WeightedMobCultivation> DEFAULT_WEIGHTS = List.of(
            new WeightedMobCultivation(MobCultivationList.get("mortal", 1), 29),
            new WeightedMobCultivation(MobCultivationList.get("mortal", 2), 18),
            new WeightedMobCultivation(MobCultivationList.get("mortal", 3), 12),

            new WeightedMobCultivation(MobCultivationList.get("qi_gathering", 1), 10),
            new WeightedMobCultivation(MobCultivationList.get("qi_gathering", 2), 6),
            new WeightedMobCultivation(MobCultivationList.get("qi_gathering", 3), 5),

            new WeightedMobCultivation(MobCultivationList.get("formation_establishment", 1), 4),
            new WeightedMobCultivation(MobCultivationList.get("formation_establishment", 2), 3),
            new WeightedMobCultivation(MobCultivationList.get("formation_establishment", 3), 2),

            new WeightedMobCultivation(MobCultivationList.get("golden_core", 1), 1),
            new WeightedMobCultivation(MobCultivationList.get("golden_core", 2), 1),
            new WeightedMobCultivation(MobCultivationList.get("golden_core", 3), 1)


    );

    private MobCultivationRoller() {
    }

    public static boolean canHaveRank(LivingEntity entity) {
        if (entity.level().isClientSide()) return false;
        if (entity instanceof Player) return false;
        return true;
    }

    public static MobCultivationDefinition rollRank(LivingEntity entity) {
        return rollFromWeights(DEFAULT_WEIGHTS);
    }

    private static MobCultivationDefinition rollFromWeights(List<WeightedMobCultivation> weights) {
        int totalWeight = 0;
        for (WeightedMobCultivation weighted : weights) {
            totalWeight += weighted.weight();
        }

        if (totalWeight <= 0) {
            return MobCultivationList.getFirst();
        }

        int roll = RANDOM.nextInt(totalWeight);
        int running = 0;

        for (WeightedMobCultivation weighted : weights) {
            running += weighted.weight();
            if (roll < running) {
                return weighted.definition();
            }
        }

        return MobCultivationList.getFirst();
    }
}