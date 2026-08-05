package net.zic.ascension.mob_cultivation;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.entity.AscensionStats;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteGenerator;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;
import net.zic.ascension.mob_cultivation.generation.MobCultivationGenerator;
import net.zic.ascension.mob_cultivation.generation.MobCultivationSubPathGenerator;
import net.zic.ascension.mob_cultivation.loot.MobCultivationLoot;
import net.zic.ascension.mob_cultivation.profile.MobCultivationProfileManager;
import net.zic.ascension.mob_cultivation.profile.ResolvedMobCultivationProfile;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationAi;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationGrowth;
import net.zic.ascension.mob_cultivation.runtime.MobCultivationVisuals;
import net.zic.ascension.mob_cultivation.skill.MobCultivationCastingController;
import net.zic.ascension.mob_cultivation.skill.MobCultivationSkillPoolManager;
import net.zic.ascension.mob_cultivation.skill.MobCultivationSkillService;
import net.zic.ascension.mob_cultivation.trait.MobCultivationTraitManager;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
        if (!(path instanceof FoundationPath)) return false;

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
        PathData pathData = AscensionOriginSourceHelper.getPathData(source, pathId);
        if (pathData == null) {
            AscensionOriginSourceHelper.removePath(source, pathId, MOB_CULTIVATION_OWNER);
            data.clearGeneratedState();
            return false;
        }

        int majorRealm = Math.clamp(requestedMajorRealm, 0, pathData.getMaxMajorRealm(source.getRegistryAccess()));
        int minorRealm = Math.clamp(
                requestedMinorRealm,
                0,
                pathData.getMaxMinorRealm(majorRealm, source.getRegistryAccess())
        );
        pathData.setMajorRealm(majorRealm, source);
        pathData.setMinorRealm(minorRealm);
        double maximumProgress = pathData.getMaxProgress(majorRealm, minorRealm, source.getRegistryAccess());
        double clampedPercentage = Math.clamp(progressPercentage, 0.0D, 100.0D);
        pathData.setProgress(maximumProgress * clampedPercentage / 100.0D);
        capturePathState(data, pathData);
        AscensionOriginSourceHelper.markPathDirty(source, pathId);

        MobCultivationGenerator.rebuildGeneratedStats(mob, data, source, pathData);
        refreshAttributesAndHealth(mob, true);
        MobCultivationSkillService.synchronize(mob);
        MobCultivationVisuals.spawnAura(mob, 18);
        MobCultivationVisuals.applyDebugName(mob);
        return true;
    }

    public static boolean setEliteTier(Mob mob, MobCultivationEliteTier tier) {
        initialize(mob);
        PathData pathData = getPathData(mob);
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
        PathData sourcePath = getPathData(sourceMob);
        if (sourcePath == null) return false;
        OriginSource sourceOrigin = getEntityData(sourceMob).getSource();
        double maximum = sourcePath.getMaxProgress(
                sourcePath.getMajorRealm(),
                sourcePath.getMinorRealm(),
                sourceOrigin.getRegistryAccess()
        );
        double percentage = maximum <= 0.0D ? 0.0D : sourcePath.getProgress() / maximum * 100.0D;
        MobCultivationData sourceData = getCultivationData(sourceMob);
        if (!setCultivation(
                targetMob,
                sourceData.getFoundationPath(),
                sourcePath.getMajorRealm(),
                sourcePath.getMinorRealm(),
                percentage
        )) return false;

        MobCultivationData targetData = getCultivationData(targetMob);
        targetData.setEliteTier(sourceData.getEliteTier());
        targetData.setSubPaths(sourceData.getSubPaths());
        targetData.setTraits(sourceData.getTraits());
        targetData.setSkillPools(sourceData.getSkillPools());
        targetData.setLootProfile(sourceData.getLootProfile());
        targetData.setGrowthFrozen(sourceData.isGrowthFrozen());
        PathData targetPath = getPathData(targetMob);
        if (targetPath != null) {
            OriginSource targetOrigin = getEntityData(targetMob).getSource();
            MobCultivationGenerator.rebuildGeneratedStats(targetMob, targetData, targetOrigin, targetPath);
            refreshAttributesAndHealth(targetMob, true);
        }
        MobCultivationSkillService.synchronize(targetMob);
        return true;
    }

    public static PathData getPathData(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!data.isCultivated() || data.getFoundationPath() == null) return null;
        return AscensionOriginSourceHelper.getPathData(getEntityData(mob).getSource(), data.getFoundationPath());
    }

    public static int getRealmScore(Mob mob) {
        PathData pathData = getPathData(mob);
        return pathData == null ? -1 : pathData.getMajorRealm() * 3 + pathData.getMinorRealm();
    }

    public static int getHighestPlayerRealmScore(ServerPlayer player) {
        AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (holder == null) return -1;
        OriginSource source = holder.getData(player).getSource();
        int highest = -1;
        for (Identifier pathId : AscensionOriginSourceHelper.getPaths(source)) {
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, player.registryAccess());
            if (!(path instanceof FoundationPath)) continue;
            PathData pathData = AscensionOriginSourceHelper.getPathData(source, pathId);
            if (pathData != null) highest = Math.max(highest, pathData.getMajorRealm() * 3 + pathData.getMinorRealm());
        }
        return highest;
    }

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
        PathData pathData = getPathData(mob);
        if (pathData == null) return Component.literal("Cultivated mob has missing path data.");
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        double maximum = pathData.getMaxProgress(pathData.getMajorRealm(), pathData.getMinorRealm(), source.getRegistryAccess());
        double percentage = maximum <= 0.0D ? 100.0D : pathData.getProgress() / maximum * 100.0D;

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
                        "Growth frozen: %s",
                mob.getType().getDescription().getString(),
                data.getCategory().name().toLowerCase(Locale.ROOT),
                data.getEliteTier().name().toLowerCase(Locale.ROOT),
                data.getFoundationPath(),
                subPaths,
                traits,
                skillPools,
                ownedSkills,
                data.getLootProfile(),
                pathData.getRealmName(
                        pathData.getMajorRealm(),
                        pathData.getMinorRealm(),
                        source.getRegistryAccess()
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
                data.isGrowthFrozen()
        );
        return Component.literal(description);
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
        PathData pathData = getPathData(mob);
        if (pathData == null) return;
        OriginSource source = getEntityData(mob).getSource();
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        MobCultivationGenerator.refreshProfileContent(mob, data, profile);
        MobCultivationGenerator.rebuildGeneratedStats(mob, data, source, pathData);
        MobCultivationSkillService.synchronize(mob);
        refreshAttributesAndHealth(mob, false);
    }

    static void ensureEntityDataInitialized(Mob mob, SimpleAscensionEntityData entityData) {
        OriginSource source = entityData.getSource();
        if (!source.isAttachedTo(mob)) source.attachToEntity(mob);
        entityData.initialize();
    }

    public static void capturePathState(MobCultivationData data, PathData pathData) {
        data.setPathState(pathData.getMajorRealm(), pathData.getMinorRealm(), pathData.getProgress());
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
