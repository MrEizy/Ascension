package net.zic.ascension.impl.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;

import net.zic.ascension.api.ascension.core.path.PathInstance;
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
            PathInstance pathData
    ) {
        if (killer == null || victim == null || source == null || pathData == null) {
            return;
        }

        //TODO redo for new technique system
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
            PathInstance pathData,
            KillProgressionTechniqueData data,
            double amount
    ) {
        //TODO redo with new technique system
    }

    private static double sanitizeNonNegative(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 0.0D;
    }
}
