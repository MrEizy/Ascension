package net.zic.ascension.mob_cultivation;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;

import net.zic.ascension.api.ascension.core.path.Path;

import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiContainer;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.entity.AscensionStats;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;

import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteGenerator;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;
import net.zic.ascension.mob_cultivation.generation.MobCultivationGenerator;
import net.zic.ascension.mob_cultivation.generation.MobCultivationSubPathGenerator;
import net.zic.ascension.mob_cultivation.loot.MobCultivationLoot;
import net.zic.ascension.mob_cultivation.profile.MobCultivationProfileManager;
import net.zic.ascension.mob_cultivation.profile.ResolvedMobCultivationProfile;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationAi;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationGrowth;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationPersistence;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationVisuals;
import net.zic.ascension.mob_cultivation.skill.MobCultivationCastingController;
import net.zic.ascension.mob_cultivation.skill.MobCultivationSkillPoolManager;
import net.zic.ascension.mob_cultivation.skill.MobCultivationSkillService;
import net.zic.ascension.mob_cultivation.trait.MobCultivationTraitManager;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.mojang.math.Constants.EPSILON;

public final class MobCultivationManager {
    public static final Identifier BODY_PATH = AscensionCraft.prefix("foundation/body");
    public static final Identifier ESSENCE_PATH = AscensionCraft.prefix("foundation/essence");
    public static final Identifier SOUL_PATH = AscensionCraft.prefix("foundation/soul");
    public static final Identifier MOB_CULTIVATION_OWNER = AscensionCraft.prefix("mob_cultivation");

    public static final List<Identifier> FOUNDATION_PATHS = List.of(BODY_PATH, ESSENCE_PATH, SOUL_PATH);

    private MobCultivationManager() {
    }

    public static void initialize(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        MobCultivationData cultivationData = getCultivationData(mob);
        cultivationData.setInitialized(true);
        cultivationData.setCultivated(true);
        cultivationData.setCategory(MobCultivationClassifier.classify(mob));
        cultivationData.beginRuntimeSession(level.getGameTime());

        SimpleAscensionEntityData entityData = getEntityData(mob);
        OriginSource source = entityData.getSource();
        if (!source.isAttachedTo(mob)) source.attachToEntity(mob);
        entityData.initialize();

        if (cultivationData.getFoundationPath() == null) {
            MobCultivationGenerator.generateFreshCultivation(mob, cultivationData, source);
        } else {
            MobCultivationGenerator.repairPersistedCultivation(mob, cultivationData, source);
            entityData.initializeAttributes();
        }
        MobCultivationSkillService.synchronize(mob);
        MobCultivationPersistence.refresh(mob);
        MobCultivationVisuals.applyDebugName(mob);
    }

    public static void tick(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !mob.isAlive()) return;
        MobCultivationData data = getCultivationData(mob);
        if (!data.isInitialized() || data.getFoundationPath() == null) {
            initialize(mob);
            data = getCultivationData(mob);
        }
        if (!data.isCultivated() || data.getFoundationPath() == null) return;

        refreshCategoryAndProfile(mob, data);
        long gameTime = level.getGameTime();
        MobCultivationGrowth.tick(mob, gameTime);
        MobCultivationVisuals.tick(mob, gameTime);
        MobCultivationAi.tick(mob, gameTime);
        MobCultivationCastingController.tick(mob, gameTime);
    }

    public static void onLeaveLevel(Mob mob) {
        MobCultivationVisuals.clearDebugName(mob);
        getCultivationData(mob).endRuntimeSession();
        OriginSource source = getEntityData(mob).getSource();
        if (source.isAttachedTo(mob)) source.detachFromEntity(mob);
    }

    public static MobCultivationData getCultivationData(Mob mob) {
        return mob.getData(AscensionAttachments.MOB_CULTIVATION_DATA);
    }

    public static SimpleAscensionEntityData getEntityData(Mob mob) {
        return mob.getData(AscensionAttachments.SIMPLE_ENTITY_DATA);
    }

    public static boolean isCultivated(Mob mob) {
        return getCultivationData(mob).isCultivated();
    }

    public static void reroll(Mob mob) {
        if (!(mob.level() instanceof ServerLevel)) return;
        initialize(mob);
        MobCultivationData data = getCultivationData(mob);
        SimpleAscensionEntityData entityData = getEntityData(mob);
        ensureEntityDataInitialized(mob, entityData);
        MobCultivationGenerator.generateFreshCultivation(mob, data, entityData.getSource());
        MobCultivationSkillService.synchronize(mob);
        MobCultivationPersistence.refresh(mob);
    }

    public static boolean setCultivation(
            Mob mob,
            Identifier pathId,
            int requestedMajorRealm,
            int requestedMinorRealm,
            double progressPercentage
    ) {
        if (!(mob.level() instanceof ServerLevel)) return false;
        initialize(mob);
        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, mob.registryAccess());
        // TODO if (!(path instanceof FoundationPath)) return false;

        MobCultivationData data = getCultivationData(mob);
        SimpleAscensionEntityData entityData = getEntityData(mob);
        ensureEntityDataInitialized(mob, entityData);
        OriginSource source = entityData.getSource();
        MobCultivationGenerator.clearGeneratedCultivation(data, source);

        data.setInitialized(true);
        data.setCultivated(true);
        data.setCategory(MobCultivationClassifier.classify(mob));
        data.setFoundationPath(pathId);
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        data.setTraits(profile.traits());
        MobCultivationGenerator.assignSkillPools(data, profile, pathId);
        data.setLootProfile(profile.lootProfile());
        MobCultivationSubPathGenerator.generate(mob, data, profile);

        if (!AscensionOriginSourceHelper.addPath(source, pathId, MOB_CULTIVATION_OWNER) && !AscensionOriginSourceHelper.hasPath(source, pathId)) {
            data.clearGeneratedState();
            return false;
        }

        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source,pathId);
        if (pathInstance == null) {
            AscensionOriginSourceHelper.removePath(source,pathId, MOB_CULTIVATION_OWNER);
            data.setCultivated(false);
            data.clearGeneratedState();
            return false;
        }

        int majorRealm = Math.clamp(requestedMajorRealm, 0, path.getMaxMajorRealm());
        int minorRealm = Math.clamp(
                requestedMinorRealm,
                0,
                pathInstance.getMaxMinorRealm(majorRealm)
        );

        pathInstance.handleRealmChange(Realm.of(majorRealm,minorRealm),source);

        double maximumProgress = pathInstance.getMaxProgress();
        double clampedPercentage = Math.clamp(progressPercentage, 0.0D, 100.0D);

        AscensionOriginSourceHelper.markPathDirty(source, pathId);
        pathInstance.progressPath(pathId,maximumProgress * clampedPercentage / 100.0D,source,mob);
        capturePathState(data, pathInstance);
        AscensionOriginSourceHelper.markPathDirty(source,pathId);

        MobCultivationGenerator.rebuildGeneratedStats(mob, data, source, pathInstance);
        rebuildGeneratedStats(mob, data, source, pathInstance);
        refreshAttributesAndHealth(mob, true);
        MobCultivationSkillService.synchronize(mob);
        MobCultivationPersistence.refresh(mob);
        MobCultivationVisuals.spawnAura(mob, 18);
        MobCultivationVisuals.applyDebugName(mob);
        return true;
    }

    public static boolean setEliteTier(Mob mob, MobCultivationEliteTier tier) {
        initialize(mob);
        PathInstance pathData = getPathInstance(mob);
        if (pathData == null) return false;
        MobCultivationData data = getCultivationData(mob);
        data.setEliteTier(tier);
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        profile.traits().forEach(data::addTrait);
        MobCultivationEliteGenerator.addEliteTraits(mob, data, profile);
        OriginSource source = getEntityData(mob).getSource();
        MobCultivationGenerator.rebuildGeneratedStats(mob, data, source, pathData);
        refreshAttributesAndHealth(mob, false);
        MobCultivationSkillService.synchronize(mob);
        MobCultivationPersistence.refresh(mob);
        MobCultivationVisuals.spawnAura(mob, 20);
        MobCultivationVisuals.applyDebugName(mob);
        return true;
    }

    public static boolean addTrait(Mob mob, Identifier trait) {
        initialize(mob);
        return MobCultivationTraitManager.get(trait) != null && getCultivationData(mob).addTrait(trait);
    }

    public static boolean removeTrait(Mob mob, Identifier trait) {
        initialize(mob);
        return getCultivationData(mob).removeTrait(trait);
    }

    public static boolean addSkillPool(Mob mob, Identifier pool) {
        initialize(mob);
        if (MobCultivationSkillPoolManager.get(pool) == null) return false;
        MobCultivationData data = getCultivationData(mob);
        java.util.LinkedHashSet<Identifier> pools = new java.util.LinkedHashSet<>(data.getSkillPools());
        boolean changed = pools.add(pool);
        if (changed) {
            data.setSkillPools(pools);
            MobCultivationSkillService.synchronize(mob);
        }
        return changed;
    }

    public static boolean removeSkillPool(Mob mob, Identifier pool) {
        initialize(mob);
        MobCultivationData data = getCultivationData(mob);
        java.util.LinkedHashSet<Identifier> pools = new java.util.LinkedHashSet<>(data.getSkillPools());
        boolean changed = pools.remove(pool);
        if (changed) {
            data.setSkillPools(pools);
            MobCultivationSkillService.synchronize(mob);
        }
        return changed;
    }

    public static boolean addSubPath(Mob mob, Identifier path) {
        initialize(mob);
        return MobCultivationSubPathGenerator.isValidSubPath(mob, path)
                && getCultivationData(mob).addSubPath(path);
    }

    public static boolean removeSubPath(Mob mob, Identifier path) {
        initialize(mob);
        return getCultivationData(mob).removeSubPath(path);
    }

    public static void rerollSubPaths(Mob mob) {
        initialize(mob);
        MobCultivationGenerator.rerollSubPaths(mob);
    }

    public static void setGrowthFrozen(Mob mob, boolean frozen) {
        initialize(mob);
        getCultivationData(mob).setGrowthFrozen(frozen);
    }

    public static boolean copyCultivation(Mob sourceMob, Mob targetMob) {
        initialize(sourceMob);
        initialize(targetMob);
        PathInstance sourcePath = getPathInstance(sourceMob);
        if (sourcePath == null) return false;
        OriginSource sourceOrigin = getEntityData(sourceMob).getSource();
        double maximum = sourcePath.getPath().getMaxProgress(sourcePath.getCurrentMajorRealm(),sourcePath.getCurrentMinorRealm());

        double percentage = maximum <= 0.0D ? 0.0D : sourcePath.getProgress() / maximum * 100.0D;
        MobCultivationData sourceData = getCultivationData(sourceMob);
        if (!setCultivation(
                targetMob,
                sourceData.getFoundationPath(),
                sourcePath.getCurrentMajorRealm(),
                sourcePath.getCurrentMinorRealm(),
                percentage
        )) return false;

        MobCultivationData targetData = getCultivationData(targetMob);
        targetData.setEliteTier(sourceData.getEliteTier());
        targetData.setSubPaths(sourceData.getSubPaths());
        targetData.setTraits(sourceData.getTraits());
        targetData.setSkillPools(sourceData.getSkillPools());
        targetData.setLootProfile(sourceData.getLootProfile());
        targetData.setGrowthFrozen(sourceData.isGrowthFrozen());
        PathInstance targetPath = getPathInstance(targetMob);
        if (targetPath != null) {
            OriginSource targetOrigin = getEntityData(targetMob).getSource();
            MobCultivationGenerator.rebuildGeneratedStats(targetMob, targetData, targetOrigin, targetPath);
            refreshAttributesAndHealth(targetMob, true);
        }
        MobCultivationSkillService.synchronize(targetMob);
        MobCultivationPersistence.refresh(targetMob);
        return true;
    }

    public static PathInstance getPathInstance(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!data.isCultivated() || data.getFoundationPath() == null) {
            return null;
        }

        return AscensionOriginSourceHelper.getPathInstance(getEntityData(mob).getSource(),data.getFoundationPath());

    }

    public static int getRealmScore(Mob mob) {
        PathInstance pathInstance = getPathInstance(mob);
        return pathInstance == null ? -1 : pathInstance.getCurrentMajorRealm() * 3 + pathInstance.getCurrentMinorRealm();
    }

    public static int getHighestPlayerRealmScore(ServerPlayer player) {
        AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (holder == null) return -1;
        OriginSource source = holder.getData().getSource();
        int highest = -1;
        for (Identifier pathId : AscensionOriginSourceHelper.getPaths(source)) {
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, player.registryAccess());
            //TODO if (!(path instanceof FoundationPath)) continue;
            PathInstance pathData = AscensionOriginSourceHelper.getPathInstance(source, pathId);
            if (pathData != null) highest = Math.max(highest, pathData.getCurrentMajorRealm() * 3 + pathData.getCurrentMinorRealm());

            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source,pathId);
            if (pathInstance != null) {
                highest = Math.max(highest, pathInstance.getCurrentMajorRealm() * 3 + pathInstance.getCurrentMinorRealm());
            }
        }
        return highest;
    }


    /* TODO not sure what happened
    public static boolean areDebugNamesEnabled() {
        return debugNamesEnabled;
    }

    public static void setDebugNamesEnabled(boolean enabled) {
        debugNamesEnabled = enabled;
    }

     */

    public static double getAtmosphericQiRatio(Mob mob) { return MobCultivationGrowth.getAtmosphericQiRatio(mob); }
    public static List<ItemStack> rollBonusLoot(Mob mob) { return MobCultivationLoot.rollBonusLoot(mob); }
    public static boolean areDebugNamesEnabled() { return MobCultivationVisuals.areDebugNamesEnabled(); }
    public static void setDebugNamesEnabled(boolean enabled) { MobCultivationVisuals.setDebugNamesEnabled(enabled); }
    public static void applyDebugName(Mob mob) { MobCultivationVisuals.applyDebugName(mob); }
    public static void clearDebugName(Mob mob) { MobCultivationVisuals.clearDebugName(mob); }

    public static Component describe(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!data.isInitialized()) return Component.literal("Mob cultivation has not initialized yet.");
        OriginSource source = getEntityData(mob).getSource();
        PathInstance pathData = getPathInstance(mob);
        if (pathData == null) return Component.literal("Cultivated mob has missing path data.");
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        double maximum = pathData.getPath().getMaxProgress(pathData.getCurrentMajorRealm(),pathData.getCurrentMinorRealm());
        double percentage = maximum <= 0.0D ? 100.0D : pathData.getProgress() / maximum * 100.0D;
        PathInstance pathInstance = getPathInstance(mob);
        if (pathInstance == null) {
            return Component.literal("Cultivated mob has missing path data.");
        }



        String subPaths = joinIds(data.getSubPaths());
        String traits = joinIds(data.getTraits());
        String skillPools = joinIds(data.getSkillPools());
        String ownedSkills = joinIds(data.getAssignedSkills());
        String description = String.format(
                Locale.ROOT,
                "Cultivated %s [%s, %s]\n" +
                        "Path: %s\n" +
                        "Sub-paths: %s\n" +
                        "Traits: %s\n" +
                        "Skill pools: %s\n" +
                        "Owned mob skills: %s\n" +
                        "Loot profile: %s\n" +
                        "Realm: %s\n" +
                        "Progress: %.2f / %.2f (%.1f%%)\n" +
                        "Resolved stat multiplier: x%.2f\n" +
                        "Generated stats: vitality %.2f, strength %.2f, agility %.2f, spirit %.2f\n" +
                        "Effective stats: vitality %.2f, strength %.2f, agility %.2f, spirit %.2f\n" +
                        "Atmospheric qi: %.1f%%\n" +
                        "Distance growth: x%.2f\n" +
                        "Growth frozen: %s\n" +
                        "Cultivation persistence granted: %s",
                mob.getType().getDescription().getString(),
                data.getCategory().name().toLowerCase(Locale.ROOT),
                data.getEliteTier().name().toLowerCase(Locale.ROOT),
                data.getFoundationPath(),
                subPaths,
                traits,
                skillPools,
                ownedSkills,
                data.getLootProfile(),
                pathData.getPath().getRealmName(
                        pathData.getCurrentMajorRealm(),
                        pathData.getCurrentMinorRealm()
                ).getString(),
                pathData.getProgress(),
                maximum,
                percentage,
                profile.statMultiplier() * data.getEliteTier().statMultiplier(),
                data.getGeneratedVitality(),
                data.getGeneratedStrength(),
                data.getGeneratedAgility(),
                data.getGeneratedSpirit(),
                source.getStat(AscensionStats.VITALITY.get()),
                source.getStat(AscensionStats.STRENGTH.get()),
                source.getStat(AscensionStats.AGILITY.get()),
                source.getStat(AscensionStats.SPIRIT.get()),
                getAtmosphericQiRatio(mob) * 100.0D,
                MobCultivationGrowth.getDistanceGrowthMultiplier(mob),
                data.isGrowthFrozen(),
                data.isCultivationPersistenceGranted());
        return Component.literal("Cultivated ")
                .append(mob.getType().getDescription())
                .append(" [")
                .append(data.getCategory().name().toLowerCase(Locale.ROOT))
                .append("]\nPath: ")
                .append(String.valueOf(data.getFoundationPath()))
                .append(String.format(Locale.ROOT, "\nProgress: %.2f / %.2f (%.1f%%)", pathInstance.getProgress(), maximum, percentage))
                .append(String.format(
                        Locale.ROOT,
                        "\nCategory stat multiplier: x%.2f",
                        data.getCategory().statMultiplier()
                ))
                .append(String.format(
                        Locale.ROOT,
                        "\nGenerated stats: vitality %.2f, strength %.2f, agility %.2f, spirit %.2f",
                        data.getGeneratedVitality(),
                        data.getGeneratedStrength(),
                        data.getGeneratedAgility(),
                        data.getGeneratedSpirit()
                ))
                .append(String.format(
                        Locale.ROOT,
                        "\nEffective stats: vitality %.2f, strength %.2f, agility %.2f, spirit %.2f",
                        source.getStat(AscensionStats.VITALITY.get()),
                        source.getStat(AscensionStats.STRENGTH.get()),
                        source.getStat(AscensionStats.AGILITY.get()),
                        source.getStat(AscensionStats.SPIRIT.get())
                ))
                .append(String.format(
                        Locale.ROOT,
                        "\nAtmospheric qi: %.1f%%",
                        getAtmosphericQiRatio(mob) * 100.0D
                ));
    }

    private static void generateFreshCultivation(
            Mob mob,
            MobCultivationData data,
            OriginSource source,
            boolean forceCultivated
    ) {
        //TODO clearGeneratedCultivation(data, source);
        data.setInitialized(true);
        //TODO data.setCategory(getCategory(mob));
        //TODO data.setCultivated(forceCultivated || mob.getRandom().nextDouble() < data.getCategory().cultivationChance());

        if (!data.isCultivated()) {
            clearDebugName(mob);
            return;
        }

        Identifier pathId = chooseFoundationPath(mob, data.getCategory());
        data.setFoundationPath(pathId);

        if (!AscensionOriginSourceHelper.addPath(source,pathId, MOB_CULTIVATION_OWNER) && !AscensionOriginSourceHelper.hasPath(source,pathId)) {
            data.setCultivated(false);
            data.clearGeneratedState();
            return;
        }

        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source,pathId);
        if (pathInstance == null) {
            AscensionOriginSourceHelper.removePath(source,pathId, MOB_CULTIVATION_OWNER);
            data.setCultivated(false);
            data.clearGeneratedState();
            return;
        }

        int[] realm = chooseInitialRealm(mob, data.getCategory(), pathInstance, source);
        pathInstance.handleRealmChange(Realm.of(realm[0],realm[1]),source);
        capturePathState(data, pathInstance);
        AscensionOriginSourceHelper.markPathDirty(source,pathId);

        rebuildGeneratedStats(mob, data, source, pathInstance);
        refreshAttributesAndHealth(mob, true);
        //TODO spawnAura(mob, 18);
        applyDebugName(mob);
    }

    private static void repairPersistedCultivation(Mob mob, MobCultivationData data, OriginSource source) {
        Identifier pathId = data.getFoundationPath();
        if (pathId == null || !FOUNDATION_PATHS.contains(pathId)) {
            generateFreshCultivation(mob, data, source, true);
            return;
        }

        if (!AscensionOriginSourceHelper.hasPath(source,pathId)) {
            AscensionOriginSourceHelper.addPath(source,pathId, MOB_CULTIVATION_OWNER);
        }

        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source,pathId);
        if (pathInstance == null) {
            generateFreshCultivation(mob, data, source, true);
            return;
        }

        int majorRealm = Math.max(
                data.getMajorRealm(),
                0
        );
        int minorRealm = Math.clamp(
                data.getMinorRealm(),
                0,
                pathInstance.getMaxMinorRealm(majorRealm)
        );
        pathInstance.handleRealmChange(Realm.of(majorRealm,minorRealm),source);
        double maximumProgress = pathInstance.getMaxProgress();
        pathInstance.progressPath(pathId,Math.clamp(data.getProgress(), 0.0D, Math.max(0.0D, maximumProgress)),source,mob);
        capturePathState(data, pathInstance);
        AscensionOriginSourceHelper.markPathDirty(source,pathId);

        boolean missingGeneratedStats = Math.abs(data.getGeneratedVitality()) < EPSILON
                && Math.abs(data.getGeneratedStrength()) < EPSILON
                && Math.abs(data.getGeneratedAgility()) < EPSILON
                && Math.abs(data.getGeneratedSpirit()) < EPSILON;

        if (missingGeneratedStats) {
            rebuildGeneratedStats(mob, data, source, pathInstance);
        } else if (!data.areGeneratedStatsApplied()) {
            restoreGeneratedStats(data, source);
        }
    }

    private static String joinIds(Collection<Identifier> ids) {
        return ids.isEmpty() ? "none" : ids.stream().map(Identifier::toString).collect(Collectors.joining(", "));
    }

    private static void refreshCategoryAndProfile(Mob mob, MobCultivationData data) {
        MobCultivationCategory resolvedCategory = MobCultivationClassifier.classify(mob);
        boolean categoryChanged = data.getCategory() != resolvedCategory;
        boolean profileChanged = data.getAppliedProfileRevision() != MobCultivationProfileManager.revision();
        if (!categoryChanged && !profileChanged) return;
        data.setCategory(resolvedCategory);
        PathInstance pathData = getPathInstance(mob);
        if (pathData == null) return;
        /*TODO fix
        if (data.areGeneratedStatsApplied()) {
            removeGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
            removeGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
            removeGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
            removeGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        }

         */
        data.clearGeneratedState();
    }


    private static void removeGeneratedStat(OriginSource source, net.zic.zenithlib.stats.Stat stat, double amount) {
        if (Math.abs(amount) > EPSILON) {
            source.removeStat(stat, amount);
        }
    }

    private static void rebuildGeneratedStats(
            Mob mob,
            MobCultivationData data,
            OriginSource source,
            PathInstance pathInstance
    ) {
        if (data.areGeneratedStatsApplied()) {
            removeGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
            removeGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
            removeGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
            removeGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        }

        int realmScore = pathInstance.getCurrentMajorRealm() * 3 + pathInstance.getCurrentMinorRealm();
        double realmFactor = 1.0D + pathInstance.getCurrentMajorRealm() * 3.0D + pathInstance.getCurrentMinorRealm() * 0.75D;
        double base = realmFactor * data.getCategory().statMultiplier();

        long seed = mob.getUUID().getMostSignificantBits()
                ^ mob.getUUID().getLeastSignificantBits()
                ^ (long) data.getFoundationPath().hashCode() * 31L
                ^ (long) realmScore * 1_000_003L;
        RandomSource random = RandomSource.create(seed);

        double vitalityWeight = 1.0D;
        double strengthWeight = 1.0D;
        double agilityWeight = 1.0D;
        double spiritWeight = 1.0D;

        if (BODY_PATH.equals(data.getFoundationPath())) {
            vitalityWeight = 1.35D;
            strengthWeight = 1.25D;
            agilityWeight = 0.85D;
            spiritWeight = 0.75D;
        } else if (ESSENCE_PATH.equals(data.getFoundationPath())) {
            vitalityWeight = 0.90D;
            strengthWeight = 1.10D;
            agilityWeight = 1.10D;
            spiritWeight = 1.35D;
        } else if (SOUL_PATH.equals(data.getFoundationPath())) {
            vitalityWeight = 0.85D;
            strengthWeight = 0.85D;
            agilityWeight = 1.0D;
            spiritWeight = 1.50D;
        }

        double vitality = roundStat(base * vitalityWeight * randomVariation(random));
        double strength = roundStat(base * strengthWeight * randomVariation(random));
        double agility = roundStat(base * agilityWeight * randomVariation(random));
        double spirit = roundStat(base * spiritWeight * randomVariation(random));

        source.addStat(AscensionStats.VITALITY.get(), vitality);
        source.addStat(AscensionStats.STRENGTH.get(), strength);
        source.addStat(AscensionStats.AGILITY.get(), agility);
        source.addStat(AscensionStats.SPIRIT.get(), spirit);
        data.setGeneratedStats(vitality, strength, agility, spirit);
        data.setGeneratedStatsApplied(true);
    }

    private static void restoreGeneratedStats(MobCultivationData data, OriginSource source) {
        restoreGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
        restoreGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
        restoreGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
        restoreGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        data.setGeneratedStatsApplied(true);
    }

    private static void restoreGeneratedStat(OriginSource source, net.zic.zenithlib.stats.Stat stat, double amount) {
        if (Math.abs(amount) <= EPSILON) {
            return;
        }
        if (Math.abs(source.getBaseStat(stat)) <= EPSILON) {
            source.addStat(stat, amount);
        }
    }

    private static void capturePathState(MobCultivationData data, PathInstance pathInstance) {
        data.setPathState(pathInstance.getCurrentMajorRealm(), pathInstance.getCurrentMinorRealm(), pathInstance.getProgress());
    }

    private static double randomVariation(RandomSource random) {
        return 0.85D + random.nextDouble() * 0.30D;
    }

    private static double roundStat(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

    private static Identifier chooseFoundationPath(Mob mob, MobCultivationCategory category) {
        double roll = mob.getRandom().nextDouble();
        if (category == MobCultivationCategory.PASSIVE) {
            if (roll < 0.60D) {
                return BODY_PATH;
            }
            return roll < 0.80D ? ESSENCE_PATH : SOUL_PATH;
        }
        if (category == MobCultivationCategory.HOSTILE) {
            if (roll < 0.34D) {
                return BODY_PATH;
            }
            return roll < 0.67D ? ESSENCE_PATH : SOUL_PATH;
        }
        return FOUNDATION_PATHS.get(mob.getRandom().nextInt(FOUNDATION_PATHS.size()));
    }

    private static int[] chooseInitialRealm(
            Mob mob,
            MobCultivationCategory category,
            PathInstance pathInstance,
            OriginSource source
    ) {
        int majorRealm;
        int minorRealm;

        switch (category) {
            case PASSIVE -> {
                majorRealm = 0;
                minorRealm = mob.getRandom().nextDouble() < 0.25D ? 1 : 0;
            }
            case HOSTILE -> {
                majorRealm = mob.getRandom().nextDouble() < 0.08D ? 1 : 0;
                minorRealm = majorRealm == 0 ? mob.getRandom().nextInt(3) : 0;
            }
            case BOSS -> {
                majorRealm = 1 + mob.getRandom().nextInt(2);
                minorRealm = mob.getRandom().nextInt(3);
            }
            default -> {
                majorRealm = 0;
                minorRealm = 0;
            }
        }

        majorRealm = Math.max(majorRealm, 0);
        minorRealm = Math.clamp(
                minorRealm,
                0,
                pathInstance.getMaxMinorRealm(majorRealm)
        );
        return new int[]{majorRealm, minorRealm};
    }

    private static void growFromAtmosphericQi(Mob mob, int intervals) {
        MobCultivationData data = getCultivationData(mob);
        PathInstance PathInstance = getPathInstance(mob);
        if (PathInstance == null) {
            return;
        }
        //TODO fix constant
        double BASE_GROWTH_PER_INTERVAL = 1;
        double qiRatio = getAtmosphericQiRatio(mob);
        double qiMultiplier = 0.15D + qiRatio * 1.85D;
        double progress = BASE_GROWTH_PER_INTERVAL
                * intervals
                * qiMultiplier
                * data.getCategory().growthMultiplier();

        addProgressWithoutTribulation(mob, progress);
    }

    private static void addProgressWithoutTribulation(Mob mob, double amount) {
        MobCultivationData data = getCultivationData(mob);
        OriginSource source = getEntityData(mob).getSource();
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        MobCultivationGenerator.refreshProfileContent(mob, data, profile);
        //TODO MobCultivationGenerator.rebuildGeneratedStats(mob, data, source, pathData);
        MobCultivationSkillService.synchronize(mob);
        refreshAttributesAndHealth(mob, false);
        MobCultivationPersistence.refresh(mob);
    }

    static void ensureEntityDataInitialized(Mob mob, SimpleAscensionEntityData entityData) {
        OriginSource source = entityData.getSource();
        if (!source.isAttachedTo(mob)) source.attachToEntity(mob);
        entityData.initialize();
    }




    public static void refreshAttributesAndHealth(Mob mob, boolean healFully) {
        float oldMaximum = mob.getMaxHealth();
        float oldHealth = mob.getHealth();
        double healthRatio = oldMaximum <= 0.0F ? 1.0D : oldHealth / oldMaximum;
        getEntityData(mob).initializeAttributes();
        if (healFully) mob.setHealth(mob.getMaxHealth());
        else mob.setHealth((float) Math.clamp(mob.getMaxHealth() * healthRatio, 1.0D, mob.getMaxHealth()));
    }
}
