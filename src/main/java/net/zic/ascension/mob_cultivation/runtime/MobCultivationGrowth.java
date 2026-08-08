package net.zic.ascension.mob_cultivation.runtime;

import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiContainer;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.generation.MobCultivationGenerator;
import net.zic.ascension.mob_cultivation.profile.MobCultivationProfileManager;
import net.zic.ascension.mob_cultivation.profile.ResolvedMobCultivationProfile;
import net.zic.ascension.mob_cultivation.skill.MobCultivationSkillService;

public final class MobCultivationGrowth {
    private static final int GROWTH_INTERVAL = 200;
    private static final double BASE_GROWTH_PER_INTERVAL = 20.0D;

    private MobCultivationGrowth() {
    }

    public static void tick(Mob mob, long gameTime) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (data.isGrowthFrozen()) {
            data.setLastGrowthGameTime(gameTime);
            return;
        }
        if (gameTime - data.getLastGrowthGameTime() < GROWTH_INTERVAL) {
            return;
        }
        long elapsed = Math.min(gameTime - data.getLastGrowthGameTime(), GROWTH_INTERVAL * 6L);
        int intervals = Math.max(1, (int) (elapsed / GROWTH_INTERVAL));
        data.setLastGrowthGameTime(gameTime);
        growFromAtmosphericQi(mob, intervals);
    }

    public static double getAtmosphericQiRatio(Mob mob) {
        return getAtmosphericQiRatio(mob, mob.blockPosition());
    }

    public static double getAtmosphericQiRatio(Mob mob, BlockPos position) {
        if (!mob.level().hasChunkAt(position)) {
            return 0.0D;
        }
        ChunkAccess chunk = mob.level().getChunk(position);
        ChunkQiContainer qi = chunk.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);
        double cap = qi.getEnergyCap();
        if (cap <= 0.0D) {
            return 0.0D;
        }
        return Math.clamp(qi.getEnergy() / cap, 0.0D, 1.5D);
    }

    private static void growFromAtmosphericQi(Mob mob, int intervals) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (MobCultivationManager.getPathData(mob) == null) {
            return;
        }
        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        double qiRatio = getAtmosphericQiRatio(mob);
        double fullQiMultiplier = 0.15D + qiRatio * 1.85D;
        double qiMultiplier = Math.max(0.05D, 1.0D + (fullQiMultiplier - 1.0D) * profile.atmosphericQiSensitivity());
        double progress = BASE_GROWTH_PER_INTERVAL
                * intervals
                * qiMultiplier
                * profile.growthMultiplier()
                * data.getEliteTier().growthMultiplier()
                * getDistanceGrowthMultiplier(mob);
        addProgressWithoutTribulation(mob, progress);
    }

    public static double getDistanceGrowthMultiplier(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !Config.MOB_CULTIVATION.DISTANCE_GROWTH_ENABLED.get()) {
            return 1.0D;
        }

        boolean overworld = level.dimension().equals(Level.OVERWORLD);
        if (!overworld && !Config.MOB_CULTIVATION.DISTANCE_GROWTH_OTHER_DIMENSIONS.get()) {
            return 1.0D;
        }

        BlockPos origin = overworld ? level.getRespawnData().pos() : BlockPos.ZERO;
        double deltaX = mob.getX() - (origin.getX() + 0.5D);
        double deltaZ = mob.getZ() - (origin.getZ() + 0.5D);
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double start = Config.MOB_CULTIVATION.DISTANCE_GROWTH_START.get();
        if (distance <= start) {
            return 1.0D;
        }

        double blocksPerMultiplier = Config.MOB_CULTIVATION.DISTANCE_BLOCKS_PER_MULTIPLIER.get();
        double maximum = Config.MOB_CULTIVATION.MAXIMUM_DISTANCE_GROWTH_MULTIPLIER.get();
        return Math.clamp(1.0D + (distance - start) / blocksPerMultiplier, 1.0D, maximum);
    }

    public static void addProgressWithoutTribulation(Mob mob, double amount) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        OriginSource source = MobCultivationManager.getEntityData(mob).getSource();
        PathData pathData = MobCultivationManager.getPathData(mob);
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

        MobCultivationManager.capturePathState(data, pathData);
        AscensionOriginSourceHelper.markPathDirty(source, data.getFoundationPath());
        boolean realmChanged = originalMajor != pathData.getMajorRealm() || originalMinor != pathData.getMinorRealm();
        if (!realmChanged) {
            return;
        }

        MobCultivationGenerator.rebuildGeneratedStats(mob, data, source, pathData);
        MobCultivationManager.refreshAttributesAndHealth(mob, false);
        MobCultivationSkillService.synchronize(mob);
        MobCultivationPersistence.refresh(mob);
        boolean majorBreakthrough = originalMajor != pathData.getMajorRealm();
        MobCultivationVisuals.onBreakthrough(mob, majorBreakthrough);
        MobCultivationVisuals.applyDebugName(mob);

        if (majorBreakthrough && mob.level() instanceof ServerLevel level) {
            Component announcement = Component.literal("A nearby ")
                    .append(mob.getType().getDescription())
                    .append(" has disturbed the surrounding qi during a major breakthrough.");
            for (ServerPlayer player : level.getPlayers(candidate -> candidate.distanceToSqr(mob) <= 64.0D * 64.0D)) {
                player.sendSystemMessage(announcement);
            }
        }
    }
}
