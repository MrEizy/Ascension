package net.thejadeproject.ascension.mob_cultivation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;

import java.util.Comparator;

public final class MobCultivationResolver {

    /*
    Fix Armor Stands having Cultivation.
    Mobs with above mortal cultivation hit you back if you hit them (override ai?).
    */

    private static final double DEFAULT_PLAYER_SEARCH_RANGE = 32.0;

    private MobCultivationResolver() {
    }

    public static MobCultivationDefinition resolveDefinition(MobCultivationData data) {
        return MobCultivationList.get(data.getRealmId(), data.getStage());
    }

    public static MobBodyStatBias resolveBodyBias(LivingEntity entity) {
        return MobBiasList.getFor(entity);
    }

    public static MobCultivationStatProfile resolveFinalStats(LivingEntity entity, MobCultivationDefinition definition) {
        return definition.baseStats().add(resolveBodyBias(entity).asProfile());
    }


    public static MobCultivationDefinition resolveFromPlayer(Player player) {
        PathData strongest = getStrongestPath(player);
        if (strongest == null) {
            return MobCultivationList.getFirst();
        }

        String realmId = mapPathMajorRealmToMobRealm(strongest.getMajorRealm());
        int stage = mapPathMinorRealmToMobStage(strongest.getMinorRealm());

        return MobCultivationList.get(realmId, stage);
    }


    private static PathData getStrongestPath(Player player) {
        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (entityData == null) return null;

        PathData best = null;

        for (PathData candidate : entityData.getAllPathData()) {
            if (candidate == null) continue;

            if (best == null || isStronger(candidate, best)) {
                best = candidate;
            }
        }

        return best;
    }

    private static boolean isStronger(PathData candidate, PathData currentBest) {
        if (candidate.getMajorRealm() != currentBest.getMajorRealm()) {
            return candidate.getMajorRealm() > currentBest.getMajorRealm();
        }

        return candidate.getMinorRealm() > currentBest.getMinorRealm();
    }

    private static Player findNearestRelevantPlayer(LivingEntity entity, double range) {
        double rangeSqr = range * range;

        return entity.level()
                .getEntitiesOfClass(
                        Player.class,
                        entity.getBoundingBox().inflate(range),
                        player -> player.isAlive()
                                && !player.isSpectator()
                                && !player.isCreative()
                                && player.distanceToSqr(entity) <= rangeSqr
                )
                .stream()
                .min(Comparator.comparingDouble(a -> a.distanceToSqr(entity)))
                .orElse(null);
    }

    private static String mapPathMajorRealmToMobRealm(int majorRealm) {
        return switch (majorRealm) {
            case 0 -> "mortal";
            case 1 -> "qi_gathering";
            case 2 -> "formation_establishment";
            case 3 -> "golden_core";
            case 4 -> "nascent_soul";
            case 5 -> "soul_formation";
            case 6 -> "void_refinement";
            case 7 -> "body_integration";
            case 8 -> "tribulation_transcendence";
            case 9 -> "mahayana";
            case 10 -> "earth_immortal";
            default -> majorRealm < 0 ? "mortal" : "earth_immortal";
        };
    }

    private static int mapPathMinorRealmToMobStage(int minorRealm) {
        int clamped = Math.max(0, Math.min(9, minorRealm));

        if (clamped <= 2) return 1;
        if (clamped <= 5) return 2;
        return 3;
    }

    public static MobCultivationCategory resolveCategory(LivingEntity entity) {
        return MobCategoryResolver.resolve(entity);
    }

    // relative realm gap
    public static int resolveCombatPower(LivingEntity entity) {
        if (entity instanceof Player player) {
            return resolvePlayerCombatPower(player);
        }

        MobCultivationData data = entity.getData(ModAttachments.MOB_RANK);
        if (data == null || !data.isInitialized() || data.isUnranked()) {
            return 0;
        }

        return getRankPower(data.getRealmId(), data.getStage());
    }

    public static int resolvePlayerCombatPower(Player player) {
        PathData strongest = getStrongestPath(player);
        if (strongest == null) {
            return 0;
        }

        String realmId = mapPathMajorRealmToMobRealm(strongest.getMajorRealm());
        int stage = mapPathMinorRealmToMobStage(strongest.getMinorRealm());

        return getRankPower(realmId, stage);
    }

    public static int getRankPower(String realmId, int stage) {
        int realmIndex = MobCultivationList.getRealmIndex(realmId);
        if (realmIndex < 0) {
            return 0;
        }

        int clampedStage = Math.max(1, Math.min(3, stage));
        return realmIndex * 3 + (clampedStage - 1);
    }

    public static MobCultivationDefinition resolveAroundNearbyPlayer(LivingEntity entity) {
        Player player = findNearestRelevantPlayer(entity, DEFAULT_PLAYER_SEARCH_RANGE);
        if (player == null) return null;

        int playerPower = resolvePlayerCombatPower(player);
        int rolledPower = rollNearbyPower(entity, playerPower);

        return resolveFromPower(rolledPower);
    }

    private static int rollNearbyPower(LivingEntity entity, int playerPower) {
        int offset = rollWeightedOffset(entity);

        int maxPower = getMaxRankPower();
        return Math.max(0, Math.min(maxPower, playerPower + offset));
    }

    private static int rollWeightedOffset(LivingEntity entity) {
        int roll = entity.getRandom().nextInt(100);

        if (roll < 12) return -3;
        if (roll < 30) return -2;
        if (roll < 55) return -1;
        if (roll < 90) return 0;
        return 1;
    }

    public static MobCultivationDefinition resolveFromPower(int power) {
        int maxPower = getMaxRankPower();
        int clampedPower = Math.max(0, Math.min(maxPower, power));

        int realmIndex = clampedPower / 3;
        int stage = (clampedPower % 3) + 1;

        String realmId = MobCultivationList.getRealmIds().get(realmIndex);
        return MobCultivationList.get(realmId, stage);
    }

    private static int getMaxRankPower() {
        return (MobCultivationList.getRealmIds().size() * 3) - 1;
    }

}