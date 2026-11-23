package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.LevelChunk;
import net.zic.ascension.Config;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.chunks.atmospheric_qi.ChunkHelper;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiHandler;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.configuration.mobs.MobProgressionRules;
import net.zic.ascension.util.PathInteractionUtil;

import java.util.List;

public final class MobCultivationProgressionService {
    private static final Identifier RARITY_POOL = Identifier.parse("ascension:rarity");

    private MobCultivationProgressionService() {
    }

    public static void tick(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !mob.isAlive()
                || !Config.MOB_CULTIVATION_SERVER.ENABLED.get()
                || !mob.hasData(AscensionAttachments.MOB_CONFIG_HOLDER)) return;

        int interval = Config.MOB_CULTIVATION_SERVER.UPDATE_INTERVAL.get();
        if (Math.floorMod((long) mob.tickCount + mob.getId(), interval) != 0) return;

        double perSecond = Config.MOB_CULTIVATION_SERVER.BASE_PROGRESS_PER_SECOND.get();
        double effectiveness = Config.MOB_CULTIVATION_SERVER.EFFECTIVENESS.get();
        if (perSecond <= 0.0D || effectiveness <= 0.0D) return;

        MobConfiguration configuration = MobSpawnHelper.getConfig(mob);
        if (configuration == null) return;
        MobProgressionRules rules = configuration.progression();
        OriginSource source = MobSpawnHelper.getConfigHolder(mob).getSource();
        if (source == null || !source.isAttachedTo(mob)) return;
        PathHolder holder = source.getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
        if (holder == null || holder.getPaths().isEmpty()) return;

        LevelChunk chunk = level.getChunkSource().getChunkNow(mob.chunkPosition().x(), mob.chunkPosition().z());
        if (chunk == null) return;

        double density = 0.5D;
        if (chunk.hasData(AscensionAttachments.CHUNK_QI_HANDLER)) {
            ChunkQiHandler qi = ChunkHelper.getQiHandler(chunk);
            long capacity = qi.getCapacity();
            if (capacity > 0L) density = Math.clamp((double) qi.getQi() / (double) capacity, 0.0D, 1.0D);
        }
        double qiFactor = Math.clamp(1.0D + bounded(rules.qiSensitivity(), 0.0D, 4.0D)
                * (1.5D * density - 0.75D), 0.1D, 4.0D);

        String rarity = "normal";
        for (MobConfigurationInstance.PoolSelection selected : MobSpawnHelper.getConfigHolder(mob)
                .getConfigurationInstance().getSelections()) {
            if (RARITY_POOL.equals(selected.pool())) {
                rarity = selected.entry();
                break;
            }
        }
        double rarityFactor = bounded(rules.rarityMultipliers().getOrDefault(rarity, 1.0D), 0.0D, 20.0D);
        double baseGain = perSecond * (interval / 20.0D) * effectiveness
                * bounded(rules.rateMultiplier(), 0.0D, 100.0D)
                * bounded(rules.potentialMultiplier(), 0.0D, 100.0D)
                * rarityFactor * qiFactor;
        if (!Double.isFinite(baseGain) || baseGain <= 0.0D) return;

        for (PathInstance<?> path : List.copyOf(holder.getPathInstances())) {
            if (!path.canProgress()) continue;
            if (rules.maxMajorRealm() >= 0 && path.getCurrentMajorRealm() >= rules.maxMajorRealm()) continue;
            Identifier id = path.getPathId();
            boolean foundation = id.getPath().startsWith("foundation/");
            double weight = bounded(foundation ? rules.foundationWeight() : rules.subpathWeight(), 0.0D, 100.0D);
            if (weight <= 0.0D) continue;
            double affinity = Math.max(0.0D, PathInteractionUtil.getEffectiveAffinity(mob, id));
            double affinityFactor = Math.clamp(1.0D + bounded(rules.affinitySensitivity(), 0.0D, 4.0D)
                    * affinity, 0.25D, 5.0D);
            double gain = Math.min(bounded(rules.maxGainPerUpdate(), 0.0D, 10000000.0D), baseGain * weight * affinityFactor);
            if (!Double.isFinite(gain) || gain <= 0.0D) continue;
            path.progressPath(gain, source, mob);
            AscensionOriginSourceHelper.markPathDirty(source, id);
        }
    }

    private static double bounded(double value, double minimum, double maximum) {
        return Double.isFinite(value) ? Math.clamp(value, minimum, maximum) : minimum;
    }
}
