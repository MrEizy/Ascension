package net.zic.ascension.mob_cultivation;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiContainer;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.entity.AscensionStats;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.source.SourceHandler;

import java.util.List;
import java.util.Locale;

public final class MobCultivationManager {
    public static final Identifier BODY_PATH = AscensionCraft.prefix("foundation/body");
    public static final Identifier ESSENCE_PATH = AscensionCraft.prefix("foundation/essence");
    public static final Identifier SOUL_PATH = AscensionCraft.prefix("foundation/soul");
    public static final Identifier MOB_CULTIVATION_OWNER = AscensionCraft.prefix("mob_cultivation");

    private static final List<Identifier> FOUNDATION_PATHS = List.of(
            BODY_PATH,
            ESSENCE_PATH,
            SOUL_PATH
    );

    private static final int GROWTH_INTERVAL = 200;
    private static final double BASE_GROWTH_PER_INTERVAL = 20.0D;
    private static final int AURA_INTERVAL = 40;
    private static final int AI_INTERVAL = 10;
    private static final int FLEE_REALM_GAP = 3;
    private static final double EPSILON = 0.000001D;

    private static boolean debugNamesEnabled;

    private MobCultivationManager() {
    }

    public static void initialize(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }

        SourceHandler sourceHandler = AscensionCraft.getSourceHandler();
        if (sourceHandler == null) {
            return;
        }

        MobCultivationData cultivationData = getCultivationData(mob);
        cultivationData.setCategory(getCategory(mob));
        cultivationData.beginRuntimeSession(level.getGameTime());

        if (!cultivationData.isInitialized()) {
            cultivationData.setInitialized(true);
            cultivationData.setCultivated(
                    mob.getRandom().nextDouble() < cultivationData.getCategory().cultivationChance()
            );
        }

        if (!cultivationData.isCultivated()) {
            clearDebugName(mob);
            return;
        }

        SimpleAscensionEntityData entityData = getEntityData(mob);
        OriginSource source = entityData.getSource();

        if (!sourceHandler.isWatcher(mob)) {
            sourceHandler.addWatcher(mob, source);
        } else {
            sourceHandler.changeWatcherState(mob, true);
        }

        entityData.initialize();

        if (cultivationData.getFoundationPath() == null) {
            generateFreshCultivation(mob, cultivationData, source, true);
        } else {
            repairPersistedCultivation(mob, cultivationData, source);
            entityData.initializeAttributes();
        }

        applyDebugName(mob);
    }

    public static void tick(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !mob.isAlive()) {
            return;
        }

        MobCultivationData data = getCultivationData(mob);
        if (!data.isInitialized()
                || (data.isCultivated() && data.getFoundationPath() == null)) {
            initialize(mob);
            data = getCultivationData(mob);
        }

        if (!data.isCultivated()) {
            clearDebugName(mob);
            return;
        }

        long gameTime = level.getGameTime();

        if (gameTime - data.getLastGrowthGameTime() >= GROWTH_INTERVAL) {
            long elapsed = Math.min(gameTime - data.getLastGrowthGameTime(), GROWTH_INTERVAL * 6L);
            int intervals = Math.max(1, (int) (elapsed / GROWTH_INTERVAL));
            data.setLastGrowthGameTime(gameTime);
            growFromAtmosphericQi(mob, intervals);
        }

        if ((mob.getId() + gameTime) % AURA_INTERVAL == 0L) {
            spawnAura(mob, 2);
            applyDebugName(mob);
        }

        if (!mob.isNoAi() && (mob.getId() + gameTime) % AI_INTERVAL == 0L) {
            tickCultivationAi(mob);
        }
    }

    public static void onLeaveLevel(Mob mob) {
        clearDebugName(mob);
        getCultivationData(mob).endRuntimeSession();

        SourceHandler sourceHandler = AscensionCraft.getSourceHandler();
        if (sourceHandler != null && sourceHandler.isWatcher(mob)) {
            sourceHandler.changeWatcherState(mob, false);
        }
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
        if (!(mob.level() instanceof ServerLevel)) {
            return;
        }

        initialize(mob);
        MobCultivationData data = getCultivationData(mob);
        SimpleAscensionEntityData entityData = getEntityData(mob);
        ensureEntityDataInitialized(mob, entityData);
        generateFreshCultivation(mob, data, entityData.getSource(), true);
    }

    public static boolean setCultivation(
            Mob mob,
            Identifier pathId,
            int requestedMajorRealm,
            int requestedMinorRealm,
            double progressPercentage
    ) {
        if (!(mob.level() instanceof ServerLevel)) {
            return false;
        }

        initialize(mob);

        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, mob.registryAccess());
        if (!(path instanceof FoundationPath)) {
            return false;
        }

        MobCultivationData data = getCultivationData(mob);
        SimpleAscensionEntityData entityData = getEntityData(mob);
        ensureEntityDataInitialized(mob, entityData);
        OriginSource source = entityData.getSource();
        clearGeneratedCultivation(data, source);

        data.setInitialized(true);
        data.setCultivated(true);
        data.setCategory(getCategory(mob));
        data.setFoundationPath(pathId);

        if (!source.addPath(pathId, MOB_CULTIVATION_OWNER) && !source.hasPath(pathId)) {
            data.setCultivated(false);
            data.clearGeneratedState();
            return false;
        }

        PathData pathData = source.getPathData(pathId);
        if (pathData == null) {
            source.removePath(pathId, MOB_CULTIVATION_OWNER);
            data.setCultivated(false);
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
        source.markPathDirty(pathId);

        rebuildGeneratedStats(mob, data, source, pathData);
        refreshAttributesAndHealth(mob, true);
        spawnAura(mob, 18);
        applyDebugName(mob);
        return true;
    }

    public static PathData getPathData(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!data.isCultivated() || data.getFoundationPath() == null) {
            return null;
        }
        return getEntityData(mob).getSource().getPathData(data.getFoundationPath());
    }

    public static int getRealmScore(Mob mob) {
        PathData pathData = getPathData(mob);
        return pathData == null ? -1 : pathData.getMajorRealm() * 3 + pathData.getMinorRealm();
    }

    public static int getHighestPlayerRealmScore(ServerPlayer player) {
        AscensionEntityDataHolder holder = player.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY
        );
        if (holder == null) {
            return -1;
        }

        OriginSource source = holder.getData(player).getSource();
        int highest = -1;

        for (Identifier pathId : source.getPaths()) {
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, player.registryAccess());
            if (!(path instanceof FoundationPath)) {
                continue;
            }

            PathData pathData = source.getPathData(pathId);
            if (pathData != null) {
                highest = Math.max(highest, pathData.getMajorRealm() * 3 + pathData.getMinorRealm());
            }
        }

        return highest;
    }

    public static double getAtmosphericQiRatio(Mob mob) {
        ChunkAccess chunk = mob.level().getChunk(mob.blockPosition());
        ChunkQiContainer qi = chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);
        double cap = qi.getEnergyCap();
        if (cap <= 0.0D) {
            return 0.0D;
        }
        return Math.clamp(qi.getEnergy() / cap, 0.0D, 1.5D);
    }

    public static ItemStack rollBonusLoot(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!data.isCultivated()) {
            return ItemStack.EMPTY;
        }

        int realmScore = Math.max(0, getRealmScore(mob));
        double chance = Math.min(
                1.0D,
                0.18D + realmScore * 0.055D + data.getCategory().lootChanceBonus()
        );

        if (mob.getRandom().nextDouble() > chance) {
            return ItemStack.EMPTY;
        }

        int count = Math.clamp(1 + realmScore / 4, 1, 4);
        Identifier path = data.getFoundationPath();

        if (realmScore >= 9 && mob.getRandom().nextDouble() < 0.22D) {
            return new ItemStack(Items.DIAMOND, 1);
        }
        if (realmScore >= 6) {
            return new ItemStack(
                    mob.getRandom().nextBoolean() ? Items.EMERALD : Items.GLOWSTONE_DUST,
                    Math.min(count, 2)
            );
        }
        if (realmScore >= 3) {
            if (SOUL_PATH.equals(path)) {
                return new ItemStack(
                        mob.getRandom().nextBoolean() ? Items.AMETHYST_SHARD : Items.ENDER_PEARL,
                        Math.min(count, 2)
                );
            }
            if (ESSENCE_PATH.equals(path)) {
                return new ItemStack(
                        mob.getRandom().nextBoolean() ? Items.LAPIS_LAZULI : Items.REDSTONE,
                        count
                );
            }
            return new ItemStack(
                    mob.getRandom().nextBoolean() ? Items.GOLD_NUGGET : Items.IRON_NUGGET,
                    count
            );
        }

        if (SOUL_PATH.equals(path)) {
            return new ItemStack(Items.AMETHYST_SHARD, count);
        }
        if (ESSENCE_PATH.equals(path)) {
            return new ItemStack(mob.getRandom().nextBoolean() ? Items.REDSTONE : Items.LAPIS_LAZULI, count);
        }
        return new ItemStack(mob.getRandom().nextBoolean() ? Items.IRON_NUGGET : Items.BONE_MEAL, count);
    }

    public static boolean areDebugNamesEnabled() {
        return debugNamesEnabled;
    }

    public static void setDebugNamesEnabled(boolean enabled) {
        debugNamesEnabled = enabled;
    }

    public static void applyDebugName(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!debugNamesEnabled || !data.isCultivated()) {
            clearDebugName(mob);
            return;
        }

        PathData pathData = getPathData(mob);
        if (pathData == null) {
            return;
        }

        Component name = Component.empty()
                .append(mob.getType().getDescription())
                .append(" • ")
                .append(pathData.getRealmName(
                        pathData.getMajorRealm(),
                        pathData.getMinorRealm(),
                        mob.registryAccess()
                ));
        String generatedName = name.getString();

        if (data.isDebugNameApplied()) {
            Component currentName = mob.getCustomName();
            if (currentName != null && !currentName.getString().equals(data.getLastDebugName())) {
                data.clearDebugNameState();
                return;
            }
        } else if (mob.getCustomName() != null) {
            return;
        }

        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        data.setDebugName(generatedName);
    }

    public static void clearDebugName(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!data.isDebugNameApplied()) {
            return;
        }

        Component currentName = mob.getCustomName();
        if (currentName != null && currentName.getString().equals(data.getLastDebugName())) {
            mob.setCustomName(null);
            mob.setCustomNameVisible(false);
        }
        data.clearDebugNameState();
    }

    public static Component describe(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (!data.isInitialized()) {
            return Component.literal("Mob cultivation has not initialized yet.");
        }
        if (!data.isCultivated()) {
            return Component.literal("Mundane ")
                    .append(mob.getType().getDescription())
                    .append(" [")
                    .append(data.getCategory().name().toLowerCase(Locale.ROOT))
                    .append("]");
        }

        OriginSource source = getEntityData(mob).getSource();
        PathData pathData = getPathData(mob);
        if (pathData == null) {
            return Component.literal("Cultivated mob has missing path data.");
        }

        double maximum = pathData.getMaxProgress(
                pathData.getMajorRealm(),
                pathData.getMinorRealm(),
                source.getRegistryAccess()
        );
        double percentage = maximum <= 0.0D ? 100.0D : pathData.getProgress() / maximum * 100.0D;

        return Component.literal("Cultivated ")
                .append(mob.getType().getDescription())
                .append(" [")
                .append(data.getCategory().name().toLowerCase(Locale.ROOT))
                .append("]\nPath: ")
                .append(String.valueOf(data.getFoundationPath()))
                .append("\nRealm: ")
                .append(pathData.getRealmName(
                        pathData.getMajorRealm(),
                        pathData.getMinorRealm(),
                        source.getRegistryAccess()
                ))
                .append(String.format(Locale.ROOT, "\nProgress: %.2f / %.2f (%.1f%%)", pathData.getProgress(), maximum, percentage))
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
                        source.getValue(AscensionStats.VITALITY.get()),
                        source.getValue(AscensionStats.STRENGTH.get()),
                        source.getValue(AscensionStats.AGILITY.get()),
                        source.getValue(AscensionStats.SPIRIT.get())
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
        clearGeneratedCultivation(data, source);
        data.setInitialized(true);
        data.setCategory(getCategory(mob));
        data.setCultivated(forceCultivated || mob.getRandom().nextDouble() < data.getCategory().cultivationChance());

        if (!data.isCultivated()) {
            clearDebugName(mob);
            return;
        }

        Identifier pathId = chooseFoundationPath(mob, data.getCategory());
        data.setFoundationPath(pathId);

        if (!source.addPath(pathId, MOB_CULTIVATION_OWNER) && !source.hasPath(pathId)) {
            data.setCultivated(false);
            data.clearGeneratedState();
            return;
        }

        PathData pathData = source.getPathData(pathId);
        if (pathData == null) {
            source.removePath(pathId, MOB_CULTIVATION_OWNER);
            data.setCultivated(false);
            data.clearGeneratedState();
            return;
        }

        int[] realm = chooseInitialRealm(mob, data.getCategory(), pathData, source);
        pathData.setMajorRealm(realm[0], source);
        pathData.setMinorRealm(realm[1]);
        pathData.setProgress(0.0D);
        capturePathState(data, pathData);
        source.markPathDirty(pathId);

        rebuildGeneratedStats(mob, data, source, pathData);
        refreshAttributesAndHealth(mob, true);
        spawnAura(mob, 18);
        applyDebugName(mob);
    }

    private static void repairPersistedCultivation(Mob mob, MobCultivationData data, OriginSource source) {
        Identifier pathId = data.getFoundationPath();
        if (pathId == null || !FOUNDATION_PATHS.contains(pathId)) {
            generateFreshCultivation(mob, data, source, true);
            return;
        }

        if (!source.hasPath(pathId)) {
            source.addPath(pathId, MOB_CULTIVATION_OWNER);
        }

        PathData pathData = source.getPathData(pathId);
        if (pathData == null) {
            generateFreshCultivation(mob, data, source, true);
            return;
        }

        int majorRealm = Math.clamp(
                data.getMajorRealm(),
                0,
                pathData.getMaxMajorRealm(source.getRegistryAccess())
        );
        int minorRealm = Math.clamp(
                data.getMinorRealm(),
                0,
                pathData.getMaxMinorRealm(majorRealm, source.getRegistryAccess())
        );
        pathData.setMajorRealm(majorRealm, source);
        pathData.setMinorRealm(minorRealm);
        double maximumProgress = pathData.getMaxProgress(majorRealm, minorRealm, source.getRegistryAccess());
        pathData.setProgress(Math.clamp(data.getProgress(), 0.0D, Math.max(0.0D, maximumProgress)));
        capturePathState(data, pathData);
        source.markPathDirty(pathId);

        boolean missingGeneratedStats = Math.abs(data.getGeneratedVitality()) < EPSILON
                && Math.abs(data.getGeneratedStrength()) < EPSILON
                && Math.abs(data.getGeneratedAgility()) < EPSILON
                && Math.abs(data.getGeneratedSpirit()) < EPSILON;

        if (missingGeneratedStats) {
            rebuildGeneratedStats(mob, data, source, pathData);
        } else if (!data.areGeneratedStatsApplied()) {
            restoreGeneratedStats(data, source);
        }
    }

    private static void clearGeneratedCultivation(MobCultivationData data, OriginSource source) {
        if (data.getFoundationPath() != null) {
            source.removePath(data.getFoundationPath(), MOB_CULTIVATION_OWNER);
        }

        if (data.areGeneratedStatsApplied()) {
            removeGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
            removeGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
            removeGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
            removeGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        }
        data.clearGeneratedState();
    }

    private static void ensureEntityDataInitialized(Mob mob, SimpleAscensionEntityData entityData) {
        SourceHandler sourceHandler = AscensionCraft.getSourceHandler();
        if (sourceHandler == null) {
            return;
        }

        if (!sourceHandler.isWatcher(mob)) {
            sourceHandler.addWatcher(mob, entityData.getSource());
        } else {
            sourceHandler.changeWatcherState(mob, true);
        }
        entityData.initialize();
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
            PathData pathData
    ) {
        if (data.areGeneratedStatsApplied()) {
            removeGeneratedStat(source, AscensionStats.VITALITY.get(), data.getGeneratedVitality());
            removeGeneratedStat(source, AscensionStats.STRENGTH.get(), data.getGeneratedStrength());
            removeGeneratedStat(source, AscensionStats.AGILITY.get(), data.getGeneratedAgility());
            removeGeneratedStat(source, AscensionStats.SPIRIT.get(), data.getGeneratedSpirit());
        }

        int realmScore = pathData.getMajorRealm() * 3 + pathData.getMinorRealm();
        double realmFactor = 1.0D + pathData.getMajorRealm() * 3.0D + pathData.getMinorRealm() * 0.75D;
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
        if (Math.abs(source.getBaseValue(stat)) <= EPSILON) {
            source.addStat(stat, amount);
        }
    }

    private static void capturePathState(MobCultivationData data, PathData pathData) {
        data.setPathState(pathData.getMajorRealm(), pathData.getMinorRealm(), pathData.getProgress());
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
            PathData pathData,
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

        majorRealm = Math.clamp(majorRealm, 0, pathData.getMaxMajorRealm(source.getRegistryAccess()));
        minorRealm = Math.clamp(
                minorRealm,
                0,
                pathData.getMaxMinorRealm(majorRealm, source.getRegistryAccess())
        );
        return new int[]{majorRealm, minorRealm};
    }

    private static void growFromAtmosphericQi(Mob mob, int intervals) {
        MobCultivationData data = getCultivationData(mob);
        PathData pathData = getPathData(mob);
        if (pathData == null) {
            return;
        }

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
        PathData pathData = getPathData(mob);
        if (pathData == null || amount <= 0.0D) {
            return;
        }

        int originalMajor = pathData.getMajorRealm();
        int originalMinor = pathData.getMinorRealm();
        double progress = pathData.getProgress() + amount;
        boolean reachedMaximum = false;

        for (int safety = 0; safety < 32; safety++) {
            int major = pathData.getMajorRealm();
            int minor = pathData.getMinorRealm();
            double needed = pathData.getMaxProgress(major, minor, source.getRegistryAccess());

            if (needed <= 0.0D || progress < needed) {
                break;
            }

            int maxMinor = pathData.getMaxMinorRealm(major, source.getRegistryAccess());
            int maxMajor = pathData.getMaxMajorRealm(source.getRegistryAccess());
            progress -= needed;

            if (minor < maxMinor) {
                pathData.setMinorRealm(minor + 1);
            } else if (major < maxMajor) {
                pathData.setMajorRealm(major + 1, source);
                pathData.setMinorRealm(0);
            } else {
                progress = needed;
                reachedMaximum = true;
                break;
            }
        }

        if (!reachedMaximum) {
            double newMaximum = pathData.getMaxProgress(
                    pathData.getMajorRealm(),
                    pathData.getMinorRealm(),
                    source.getRegistryAccess()
            );
            pathData.setProgress(Math.clamp(progress, 0.0D, Math.max(0.0D, newMaximum)));
        } else {
            pathData.setProgress(progress);
        }

        capturePathState(data, pathData);
        source.markPathDirty(data.getFoundationPath());

        boolean realmChanged = originalMajor != pathData.getMajorRealm()
                || originalMinor != pathData.getMinorRealm();
        if (!realmChanged) {
            return;
        }

        rebuildGeneratedStats(mob, data, source, pathData);
        refreshAttributesAndHealth(mob, false);
        spawnAura(mob, originalMajor == pathData.getMajorRealm() ? 10 : 24);
        applyDebugName(mob);

        if (originalMajor != pathData.getMajorRealm() && mob.level() instanceof ServerLevel level) {
            Component announcement = Component.literal("A nearby ")
                    .append(mob.getType().getDescription())
                    .append(" has undergone a major cultivation breakthrough.");

            for (ServerPlayer player : level.getPlayers(candidate -> candidate.distanceToSqr(mob) <= 64.0D * 64.0D)) {
                player.sendSystemMessage(announcement);
            }
        }
    }

    private static void refreshAttributesAndHealth(Mob mob, boolean healFully) {
        float oldMaximum = mob.getMaxHealth();
        float oldHealth = mob.getHealth();
        double healthRatio = oldMaximum <= 0.0F ? 1.0D : oldHealth / oldMaximum;

        getEntityData(mob).initializeAttributes();

        if (healFully) {
            mob.setHealth(mob.getMaxHealth());
        } else {
            mob.setHealth((float) Math.clamp(mob.getMaxHealth() * healthRatio, 1.0D, mob.getMaxHealth()));
        }
    }

    private static void tickCultivationAi(Mob mob) {
        MobCultivationData data = getCultivationData(mob);
        if (data.getCategory() == MobCultivationCategory.PASSIVE) {
            tickPassiveRetaliation(mob, data);
        } else if (data.getCategory() == MobCultivationCategory.HOSTILE) {
            tickHostileFleeing(mob, data);
        }
    }

    private static void tickPassiveRetaliation(Mob mob, MobCultivationData data) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }

        int hurtTimestamp = mob.getLastHurtByMobTimestamp();
        LivingEntity attacker = mob.getLastHurtByMob();
        int realmScore = Math.max(0, getRealmScore(mob));
        double spirit = getEntityData(mob).getSource().getValue(AscensionStats.SPIRIT.get());

        if (attacker != null
                && attacker.isAlive()
                && hurtTimestamp != data.getLastProcessedHurtTimestamp()) {
            data.setLastProcessedHurtTimestamp(hurtTimestamp);
            double retaliationChance = Math.min(0.95D, 0.10D + realmScore * 0.12D + spirit * 0.0125D);
            if (realmScore >= 1 && mob.getRandom().nextDouble() < retaliationChance) {
                data.startRetaliating(attacker.getUUID(), 200 + realmScore * 20);
            }
        }

        if (data.getRetaliationTarget() == null || data.getRetaliationTicks() <= 0) {
            data.stopRetaliating();
            mob.setAggressive(false);
            if (mob.getTarget() != null && mob.getTarget() == attacker) {
                mob.setTarget(null);
            }
            return;
        }

        Entity targetEntity = level.getEntityInAnyDimension(data.getRetaliationTarget());
        if (!(targetEntity instanceof LivingEntity target)
                || target.level() != mob.level()
                || !target.isAlive()
                || target.distanceToSqr(mob) > 32.0D * 32.0D) {
            if (mob.getTarget() == targetEntity) {
                mob.setTarget(null);
            }
            mob.setAggressive(false);
            data.stopRetaliating();
            return;
        }

        mob.setTarget(target);
        mob.setAggressive(true);
        mob.getNavigation().moveTo(target, 1.10D + Math.min(0.35D, realmScore * 0.025D));

        if (data.getRetaliationAttackCooldown() <= 0 && mob.isWithinMeleeAttackRange(target)) {
            if (mob.doHurtTarget(level, target)) {
                data.setRetaliationAttackCooldown(Math.max(10, 22 - realmScore));
            }
        }

        data.tickRetaliation(AI_INTERVAL);
        if (data.getRetaliationTicks() <= 0) {
            if (mob.getTarget() == target) {
                mob.setTarget(null);
            }
            mob.setAggressive(false);
            data.stopRetaliating();
        }
    }

    private static void tickHostileFleeing(Mob mob, MobCultivationData data) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }

        if (data.getFleeFrom() == null) {
            LivingEntity currentTarget = mob.getTarget();
            if (!(currentTarget instanceof ServerPlayer player)) {
                return;
            }

            int playerScore = getHighestPlayerRealmScore(player);
            int mobScore = getRealmScore(mob);
            double spirit = getEntityData(mob).getSource().getValue(AscensionStats.SPIRIT.get());
            double sensingChance = Math.min(0.90D, 0.25D + spirit * 0.02D);

            if (playerScore - mobScore >= FLEE_REALM_GAP && mob.getRandom().nextDouble() < sensingChance) {
                data.startFleeing(player.getUUID(), 80);
            }
        }

        if (data.getFleeFrom() == null || data.getFleeTicks() <= 0) {
            data.stopFleeing();
            return;
        }

        Entity sourceEntity = level.getEntityInAnyDimension(data.getFleeFrom());
        if (!(sourceEntity instanceof ServerPlayer player)
                || player.level() != mob.level()
                || !player.isAlive()
                || player.distanceToSqr(mob) > 40.0D * 40.0D) {
            data.stopFleeing();
            return;
        }

        Vec3 away = mob.position().subtract(player.position());
        if (away.lengthSqr() < 0.0001D) {
            away = new Vec3(mob.getRandom().nextDouble() - 0.5D, 0.0D, mob.getRandom().nextDouble() - 0.5D);
        }
        away = away.normalize();
        Vec3 destination = mob.position().add(away.scale(12.0D));

        mob.setTarget(null);
        mob.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.25D);
        data.tickFleeing(AI_INTERVAL);
    }

    private static void spawnAura(Mob mob, int count) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }

        ParticleOptions particle = getAuraParticle(getCultivationData(mob).getFoundationPath());
        level.sendParticles(
                particle,
                mob.getX(),
                mob.getY() + mob.getBbHeight() * 0.55D,
                mob.getZ(),
                count,
                mob.getBbWidth() * 0.35D,
                mob.getBbHeight() * 0.35D,
                mob.getBbWidth() * 0.35D,
                0.01D
        );
    }

    private static ParticleOptions getAuraParticle(Identifier path) {
        if (BODY_PATH.equals(path)) {
            return ParticleTypes.CRIT;
        }
        if (SOUL_PATH.equals(path)) {
            return ParticleTypes.SOUL;
        }
        return ParticleTypes.ENCHANT;
    }

    private static MobCultivationCategory getCategory(Mob mob) {
        if (mob instanceof EnderDragon || mob instanceof WitherBoss) {
            return MobCultivationCategory.BOSS;
        }
        if (mob instanceof Enemy) {
            return MobCultivationCategory.HOSTILE;
        }
        return MobCultivationCategory.PASSIVE;
    }
}
