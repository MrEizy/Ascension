package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CultivationUtil {
    public static final Identifier CULTIVATION_CATEGORY = AscensionCraft.prefix("cultivation");
    public static final Identifier FOUNDATION_CATEGORY = AscensionCraft.prefix("foundation");
    private static final double SECONDARY_AFFINITY_WEIGHT = 0.25D;

    private CultivationUtil() {
    }

    public static void cultivate(
            LivingEntity caster,
            OriginSource source,
            PathData pathData,
            List<Identifier> secondaryPaths,
            double baseRate
    ) {
        if (source == null || pathData == null || pathData.getCurrentTechnique() == null) {
            return;
        }

        Technique technique = CoreRegistries.safeAccess(
                CoreRegistries.TECHNIQUE_REGISTRY,
                pathData.getCurrentTechnique(),
                source.getRegistryAccess()
        );
        if (technique == null) {
            return;
        }

        TechniqueData data = pathData.getCurrentTechniqueData();
        double cultivationAmount = calculateCultivationRate(
                source,
                pathData.getPath(),
                secondaryPaths,
                baseRate
        );
        double maximumProgress = pathData.getMaxProgress(
                pathData.getMajorRealm(),
                pathData.getMinorRealm(),
                source.getRegistryAccess()
        );

        pathData.setProgress(Math.min(maximumProgress, pathData.getProgress() + cultivationAmount));

        if (technique.tryBreakthrough(
                caster,
                source,
                pathData.getMajorRealm(),
                pathData.getMinorRealm(),
                pathData.getProgress(),
                data
        )) {
            if (pathData.getMinorRealm() == pathData.getMaxMinorRealm(
                    pathData.getMajorRealm(),
                    source.getRegistryAccess()
            )) {
                pathData.handleRealmChange(source, pathData.getMajorRealm() + 1, 0);
            } else {
                pathData.handleRealmChange(source, pathData.getMajorRealm(), pathData.getMinorRealm() + 1);
            }
            pathData.setProgress(0.0D);
        }

        AscensionOriginSourceHelper.markPathDirty(source, pathData.getPath());
    }

    public static void cultivateFoundation(
            LivingEntity entity,
            OriginSource source,
            FoundationPathData foundationPathData,
            double baseRate
    ) {
        if (source == null || foundationPathData == null) {
            return;
        }

        Path path = CoreRegistries.safeAccess(
                CoreRegistries.PATH_REGISTRY,
                foundationPathData.getPath(),
                source.getRegistryAccess()
        );
        if (!(path instanceof FoundationPath foundationPath)) {
            return;
        }

        double affinity = finiteAffinity(AscensionOriginSourceHelper.getPathBonus(
                source,
                FOUNDATION_CATEGORY,
                foundationPathData.getPath()
        ));
        double rate = Math.max(0.0D, baseRate * Math.max(0.0D, 1.0D + affinity));
        int majorRealm = foundationPathData.getMajorRealm();
        int foundationRealm = foundationPathData.getFoundationRealm(majorRealm);
        double newProgress = foundationPathData.getFoundationRealmProgress(majorRealm) + rate;

        foundationPathData.setFoundationRealmProgress(majorRealm, newProgress);

        if (foundationPath.tryBreakthroughFoundation(
                entity,
                source,
                majorRealm,
                foundationRealm,
                newProgress
        )) {
            foundationPathData.handleFoundationRealmChange(source, majorRealm, foundationRealm + 1);
            foundationPathData.setFoundationRealmProgress(majorRealm, 0.0D);
        }

        AscensionOriginSourceHelper.markPathDirty(source, foundationPathData.getPath());
    }

    public static double calculateCultivationRate(
            OriginSource source,
            Identifier primaryPath,
            List<Identifier> secondaryPaths,
            double baseRate
    ) {
        if (source == null || primaryPath == null || !Double.isFinite(baseRate) || baseRate <= 0.0D) {
            return 0.0D;
        }

        double primaryAffinity = finiteAffinity(AscensionOriginSourceHelper.getPathBonus(
                source,
                CULTIVATION_CATEGORY,
                primaryPath
        ));

        Set<Identifier> uniqueSecondaryPaths = new LinkedHashSet<>();
        if (secondaryPaths != null) {
            for (Identifier secondaryPath : secondaryPaths) {
                if (secondaryPath != null && !secondaryPath.equals(primaryPath)) {
                    uniqueSecondaryPaths.add(secondaryPath);
                }
            }
        }

        double secondaryAffinityTotal = 0.0D;
        for (Identifier secondaryPath : uniqueSecondaryPaths) {
            secondaryAffinityTotal += finiteAffinity(AscensionOriginSourceHelper.getPathBonus(
                    source,
                    CULTIVATION_CATEGORY,
                    secondaryPath
            ));
        }

        double secondaryAffinity = uniqueSecondaryPaths.isEmpty()
                ? 0.0D
                : secondaryAffinityTotal / uniqueSecondaryPaths.size();
        double affinityModifier = primaryAffinity + secondaryAffinity * SECONDARY_AFFINITY_WEIGHT;
        return Math.max(0.0D, baseRate * Math.max(0.0D, 1.0D + affinityModifier));
    }

    private static double finiteAffinity(double affinity) {
        return Double.isFinite(affinity) ? affinity : 0.0D;
    }
}
