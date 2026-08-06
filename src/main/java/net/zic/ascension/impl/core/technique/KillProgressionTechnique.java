package net.zic.ascension.impl.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.technique.data.KillProgressionTechniqueData;
import net.zic.ascension.impl.core.technique.realm.MajorRealmDefinitionOverride;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A technique where path progress is earned from kills
 */
public final class KillProgressionTechnique extends SimpleTechnique {
    private static final int MAX_BREAKTHROUGHS_PER_KILL = 64;
    private static final double PROGRESS_EPSILON = 1.0E-9D;

    private final double baseProgress;
    private final double maxHealthMultiplier;
    private final double playerMultiplier;

    public KillProgressionTechnique(
            Component name,
            Component description,
            Identifier path,
            List<Integer> milestoneRealms,
            List<String> techniqueFamilies,
            Integer maxMajorRealm,
            Integer maxMinorRealm,
            int minMajorRealm,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            ProgressActionHolder holder,
            Map<Integer, MajorRealmDefinitionOverride> majorRealmOverrides,
            double baseProgress,
            double maxHealthMultiplier,
            double playerMultiplier
    ) {
        super(
                name,
                description,
                path,
                milestoneRealms,
                techniqueFamilies,
                maxMajorRealm,
                maxMinorRealm,
                minMajorRealm,
                itemTooltip,
                holder,
                majorRealmOverrides
        );
        this.baseProgress = sanitizeNonNegative(baseProgress);
        this.maxHealthMultiplier = sanitizeNonNegative(maxHealthMultiplier);
        this.playerMultiplier = sanitizeNonNegative(playerMultiplier);
    }

    public double getBaseProgress() {
        return baseProgress;
    }

    public double getMaxHealthMultiplier() {
        return maxHealthMultiplier;
    }

    public double getPlayerMultiplier() {
        return playerMultiplier;
    }

    @Override
    public TechniqueType getType() {
        return AscensionTechniqueTypes.KILL_PROGRESSION_TECHNIQUE_TYPE.get();
    }

    @Override
    public boolean allowsCultivationProgress() {
        return false;
    }

    @Override
    public TechniqueData newData() {
        return new KillProgressionTechniqueData();
    }

    @Override
    public TechniqueData loadData(ValueInput input) {
        return KillProgressionTechniqueData.load(input);
    }

    @Override
    public TechniqueData loadData(ByteBuf buf) {
        return KillProgressionTechniqueData.decode(buf);
    }

    public void handleKill(
            LivingEntity killer,
            LivingEntity victim,
            OriginSource source,
            PathData pathData
    ) {
        if (killer == null || victim == null || source == null || pathData == null) {
            return;
        }
        if (!(pathData.getCurrentTechniqueData() instanceof KillProgressionTechniqueData data)) {
            return;
        }

        double earnedProgress = calculateProgress(victim);
        data.recordKill(earnedProgress);
        addProgress(killer, source, pathData, data, earnedProgress);
        AscensionOriginSourceHelper.markPathDirty(source, pathData.getPath());
    }

    private double calculateProgress(LivingEntity victim) {
        double progress = baseProgress + Math.max(0.0D, victim.getMaxHealth()) * maxHealthMultiplier;
        if (victim instanceof Player) {
            progress *= playerMultiplier;
        }
        return sanitizeNonNegative(progress);
    }

    private void addProgress(
            LivingEntity killer,
            OriginSource source,
            PathData pathData,
            KillProgressionTechniqueData data,
            double amount
    ) {
        double remaining = sanitizeNonNegative(amount);
        int breakthroughs = 0;

        while (remaining > PROGRESS_EPSILON && breakthroughs < MAX_BREAKTHROUGHS_PER_KILL) {
            int majorRealm = pathData.getMajorRealm();
            int minorRealm = pathData.getMinorRealm();
            double maximum = pathData.getMaxProgress(
                    majorRealm,
                    minorRealm,
                    source.getRegistryAccess()
            );
            if (!Double.isFinite(maximum) || maximum <= 0.0D) {
                return;
            }

            double current = Math.clamp(pathData.getProgress(), 0.0D, maximum);
            double applied = Math.min(remaining, Math.max(0.0D, maximum - current));
            pathData.setProgress(current + applied);
            remaining -= applied;

            if (pathData.getProgress() + PROGRESS_EPSILON < maximum) {
                return;
            }

            if (!tryBreakthrough(killer, source, majorRealm, minorRealm, pathData.getProgress(), data)) {
                return;
            }

            int maximumMinorRealm = pathData.getMaxMinorRealm(majorRealm, source.getRegistryAccess());
            if (minorRealm >= maximumMinorRealm) {
                pathData.handleRealmChange(source, majorRealm + 1, 0);
            } else {
                pathData.handleRealmChange(source, majorRealm, minorRealm + 1);
            }
            pathData.setProgress(0.0D);
            breakthroughs++;
        }
    }

    private static double sanitizeNonNegative(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 0.0D;
    }
}
