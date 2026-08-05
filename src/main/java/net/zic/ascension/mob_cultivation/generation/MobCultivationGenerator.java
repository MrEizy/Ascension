package net.zic.ascension.mob_cultivation.generation;

import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.entity.AscensionStats;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.mob_cultivation.MobCultivationClassifier;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.profile.MobCultivationProfileManager;
import net.zic.ascension.mob_cultivation.profile.ResolvedMobCultivationProfile;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationVisuals;
import net.zic.zenithlib.stats.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MobCultivationGenerator {
    private static final double EPSILON = 0.000001D;

    private MobCultivationGenerator() {
    }

    public static void generateFreshCultivation(Mob mob, MobCultivationData data, OriginSource source) {
        clearGeneratedCultivation(data, source);
        data.setInitialized(true);
        data.setCultivated(true);
        data.setCategory(MobCultivationClassifier.classify(mob));

        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        data.setEliteTier(MobCultivationEliteGenerator.rollTier(mob, profile));
        data.setTraits(profile.traits());
        MobCultivationEliteGenerator.addEliteTraits(mob, data, profile);
        data.setLootProfile(profile.lootProfile());

        Identifier pathId = chooseFoundationPath(mob, profile);
        data.setFoundationPath(pathId);
        assignSkillPools(data, profile, pathId);
        MobCultivationSubPathGenerator.generate(mob, data, profile);

        if (!AscensionOriginSourceHelper.addPath(source, pathId, MobCultivationManager.MOB_CULTIVATION_OWNER) && !AscensionOriginSourceHelper.hasPath(source, pathId)) {
            data.clearGeneratedState();
            return;
        }

        PathData pathData = AscensionOriginSourceHelper.getPathData(source, pathId);
        if (pathData == null) {
            AscensionOriginSourceHelper.removePath(source, pathId, MobCultivationManager.MOB_CULTIVATION_OWNER);
            data.clearGeneratedState();
            return;
        }

        int[] realm = chooseInitialRealm(mob, data, profile, pathData, source);
        pathData.setMajorRealm(realm[0], source);
        pathData.setMinorRealm(realm[1]);
        pathData.setProgress(0.0D);
        MobCultivationManager.capturePathState(data, pathData);
        AscensionOriginSourceHelper.markPathDirty(source, pathId);

        rebuildGeneratedStats(mob, data, source, pathData);
        MobCultivationManager.refreshAttributesAndHealth(mob, true);
        MobCultivationVisuals.spawnAura(mob, 18);
        MobCultivationVisuals.applyDebugName(mob);
        MobCultivationVisuals.announceEliteSpawn(mob);
    }

    public static void repairPersistedCultivation(Mob mob, MobCultivationData data, OriginSource source) {
        Identifier pathId = data.getFoundationPath();
        Path path = pathId == null ? null : CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, mob.registryAccess());
        if (!(path instanceof FoundationPath)) {
            generateFreshCultivation(mob, data, source);
            return;
        }

        if (!AscensionOriginSourceHelper.hasPath(source, pathId)) AscensionOriginSourceHelper.addPath(source, pathId, MobCultivationManager.MOB_CULTIVATION_OWNER);
        PathData pathData = AscensionOriginSourceHelper.getPathData(source, pathId);
        if (pathData == null) {
            generateFreshCultivation(mob, data, source);
            return;
        }

        int majorRealm = Math.clamp(data.getMajorRealm(), 0, pathData.getMaxMajorRealm(source.getRegistryAccess()));
        int minorRealm = Math.clamp(data.getMinorRealm(), 0, pathData.getMaxMinorRealm(majorRealm, source.getRegistryAccess()));
        pathData.setMajorRealm(majorRealm, source);
        pathData.setMinorRealm(minorRealm);
        double maximumProgress = pathData.getMaxProgress(majorRealm, minorRealm, source.getRegistryAccess());
        pathData.setProgress(Math.clamp(data.getProgress(), 0.0D, Math.max(0.0D, maximumProgress)));
        MobCultivationManager.capturePathState(data, pathData);
        AscensionOriginSourceHelper.markPathDirty(source, pathId);

        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        if (data.getSubPaths().isEmpty()) MobCultivationSubPathGenerator.generate(mob, data, profile);
        if (data.getTraits().isEmpty()) {
            data.setTraits(profile.traits());
            MobCultivationEliteGenerator.addEliteTraits(mob, data, profile);
        }
        if (data.getSkillPools().isEmpty()) assignSkillPools(data, profile, pathId);
        if (data.getLootProfile() == null) data.setLootProfile(profile.lootProfile());

        boolean missingGeneratedStats = Math.abs(data.getGeneratedVitality()) < EPSILON
                && Math.abs(data.getGeneratedStrength()) < EPSILON
                && Math.abs(data.getGeneratedAgility()) < EPSILON
                && Math.abs(data.getGeneratedSpirit()) < EPSILON;

        if (missingGeneratedStats) {
            rebuildGeneratedStats(mob, data, source, pathData);
        } else {
            if (!data.areGeneratedStatsApplied()) restoreGeneratedStats(data, source);
            if (data.getAppliedProfileRevision() != MobCultivationProfileManager.revision()) {
                refreshProfileContent(mob, data, profile);
                rebuildGeneratedStats(mob, data, source, pathData);
            }
        }
    }

    public static void refreshProfileContent(Mob mob, MobCultivationData data, ResolvedMobCultivationProfile profile) {
        assignSkillPools(data, profile, data.getFoundationPath());
        data.setLootProfile(profile.lootProfile());
        profile.traits().forEach(data::addTrait);
        if (data.getSubPaths().isEmpty()) {
            MobCultivationSubPathGenerator.generate(mob, data, profile);
        }
    }


    public static void assignSkillPools(MobCultivationData data, ResolvedMobCultivationProfile profile, Identifier foundationPath) {
        java.util.LinkedHashSet<Identifier> pools = new java.util.LinkedHashSet<>(profile.skillPools());
        if (MobCultivationManager.BODY_PATH.equals(foundationPath)) {
            pools.add(net.zic.ascension.AscensionCraft.prefix("generic/body"));
        } else if (MobCultivationManager.ESSENCE_PATH.equals(foundationPath)) {
            pools.add(net.zic.ascension.AscensionCraft.prefix("generic/essence"));
        } else if (MobCultivationManager.SOUL_PATH.equals(foundationPath)) {
            pools.add(net.zic.ascension.AscensionCraft.prefix("generic/soul"));
        }
        data.setSkillPools(pools);
    }

    public static void rerollSubPaths(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        MobCultivationSubPathGenerator.generate(mob, data, profile);
        MobCultivationVisuals.spawnAura(mob, 12);
    }

    public static void clearGeneratedCultivation(MobCultivationData data, OriginSource source) {
        if (data.getFoundationPath() != null) {
            AscensionOriginSourceHelper.removePath(source, data.getFoundationPath(), MobCultivationManager.MOB_CULTIVATION_OWNER);
        }
        if (data.areGeneratedStatsApplied()) {
            removeGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
            removeGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
            removeGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
            removeGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        }
        data.clearGeneratedState();
    }

    public static void rebuildGeneratedStats(Mob mob, MobCultivationData data, OriginSource source, PathData pathData) {
        if (data.areGeneratedStatsApplied()) {
            removeGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
            removeGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
            removeGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
            removeGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        }

        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        int realmScore = pathData.getMajorRealm() * 3 + pathData.getMinorRealm();
        double realmFactor = 1.0D + pathData.getMajorRealm() * 3.0D + pathData.getMinorRealm() * 0.75D;
        double base = realmFactor * profile.statMultiplier() * data.getEliteTier().statMultiplier();

        long seed = mob.getUUID().getMostSignificantBits()
                ^ mob.getUUID().getLeastSignificantBits()
                ^ (long) data.getFoundationPath().hashCode() * 31L
                ^ (long) realmScore * 1_000_003L
                ^ (long) data.getEliteTier().ordinal() * 97_003L;
        RandomSource random = RandomSource.create(seed);

        double vitalityWeight = 1.0D;
        double strengthWeight = 1.0D;
        double agilityWeight = 1.0D;
        double spiritWeight = 1.0D;

        if (MobCultivationManager.BODY_PATH.equals(data.getFoundationPath())) {
            vitalityWeight = 1.35D; strengthWeight = 1.25D; agilityWeight = 0.85D; spiritWeight = 0.75D;
        } else if (MobCultivationManager.ESSENCE_PATH.equals(data.getFoundationPath())) {
            vitalityWeight = 0.90D; strengthWeight = 1.10D; agilityWeight = 1.10D; spiritWeight = 1.35D;
        } else if (MobCultivationManager.SOUL_PATH.equals(data.getFoundationPath())) {
            vitalityWeight = 0.85D; strengthWeight = 0.85D; agilityWeight = 1.0D; spiritWeight = 1.50D;
        }

        double vitality = roundStat(base * vitalityWeight * profile.vitalityBias() * randomVariation(random));
        double strength = roundStat(base * strengthWeight * profile.strengthBias() * randomVariation(random));
        double agility = roundStat(base * agilityWeight * profile.agilityBias() * randomVariation(random));
        double spirit = roundStat(base * spiritWeight * profile.spiritBias() * randomVariation(random));

        source.addStat(AscensionStats.VITALITY.get(), vitality);
        source.addStat(AscensionStats.STRENGTH.get(), strength);
        source.addStat(AscensionStats.AGILITY.get(), agility);
        source.addStat(AscensionStats.SPIRIT.get(), spirit);
        data.setGeneratedStats(vitality, strength, agility, spirit);
        data.setGeneratedStatsApplied(true);
        data.setAppliedProfileRevision(MobCultivationProfileManager.revision());
    }

    private static Identifier chooseFoundationPath(Mob mob, ResolvedMobCultivationProfile profile) {
        List<Map.Entry<Identifier, Double>> valid = new ArrayList<>();
        double total = 0.0D;
        for (Map.Entry<Identifier, Double> entry : profile.foundationPathWeights().entrySet()) {
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, entry.getKey(), mob.registryAccess());
            if (path instanceof FoundationPath && entry.getValue() > 0.0D) {
                valid.add(entry);
                total += entry.getValue();
            }
        }
        if (valid.isEmpty() || total <= 0.0D) {
            return MobCultivationManager.FOUNDATION_PATHS.get(mob.getRandom().nextInt(MobCultivationManager.FOUNDATION_PATHS.size()));
        }
        double roll = mob.getRandom().nextDouble() * total;
        for (Map.Entry<Identifier, Double> entry : valid) {
            roll -= entry.getValue();
            if (roll <= 0.0D) return entry.getKey();
        }
        return valid.getLast().getKey();
    }

    private static int[] chooseInitialRealm(
            Mob mob,
            MobCultivationData data,
            ResolvedMobCultivationProfile profile,
            PathData pathData,
            OriginSource source
    ) {
        int majorRealm;
        int minorRealm;
        if (profile.hasInitialRealmOverride()) {
            int majorMin = profile.initialMajorRealmMin() == null ? 0 : profile.initialMajorRealmMin();
            int majorMax = profile.initialMajorRealmMax() == null ? majorMin : profile.initialMajorRealmMax();
            if (majorMax < majorMin) { int swap = majorMax; majorMax = majorMin; majorMin = swap; }
            majorRealm = randomInclusive(mob.getRandom(), majorMin, majorMax);
            int minorMin = profile.initialMinorRealmMin() == null ? 0 : profile.initialMinorRealmMin();
            int minorMax = profile.initialMinorRealmMax() == null ? minorMin : profile.initialMinorRealmMax();
            if (minorMax < minorMin) { int swap = minorMax; minorMax = minorMin; minorMin = swap; }
            minorRealm = randomInclusive(mob.getRandom(), minorMin, minorMax);
        } else {
            switch (data.getCategory()) {
                case PASSIVE -> { majorRealm = 0; minorRealm = mob.getRandom().nextDouble() < 0.25D ? 1 : 0; }
                case HOSTILE -> {
                    majorRealm = mob.getRandom().nextDouble() < 0.08D ? 1 : 0;
                    minorRealm = majorRealm == 0 ? mob.getRandom().nextInt(3) : 0;
                }
                case BOSS -> { majorRealm = 1 + mob.getRandom().nextInt(2); minorRealm = mob.getRandom().nextInt(3); }
                default -> { majorRealm = 0; minorRealm = 0; }
            }
        }
        majorRealm += data.getEliteTier().majorRealmBonus();
        minorRealm += data.getEliteTier().minorRealmBonus();
        majorRealm = Math.clamp(majorRealm, 0, pathData.getMaxMajorRealm(source.getRegistryAccess()));
        minorRealm = Math.clamp(minorRealm, 0, pathData.getMaxMinorRealm(majorRealm, source.getRegistryAccess()));
        return new int[]{majorRealm, minorRealm};
    }

    private static int randomInclusive(RandomSource random, int minimum, int maximum) {
        return minimum >= maximum ? minimum : minimum + random.nextInt(maximum - minimum + 1);
    }

    private static void restoreGeneratedStats(MobCultivationData data, OriginSource source) {
        restoreGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
        restoreGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
        restoreGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
        restoreGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        data.setGeneratedStatsApplied(true);
    }

    private static void restoreGeneratedStat(OriginSource source, Stat stat, double amount) {
        if (Math.abs(amount) <= EPSILON) return;
        if (Math.abs(source.getBaseStat(stat)) <= EPSILON) source.addStat(stat, amount);
    }
    private static void removeGeneratedStat(OriginSource source, Stat stat, double amount) {
        if (Math.abs(amount) > EPSILON) source.removeStat(stat, amount);
    }
    private static double randomVariation(RandomSource random) { return 0.85D + random.nextDouble() * 0.30D; }
    private static double roundStat(double value) { return Math.round(value * 100.0D) / 100.0D; }
}
