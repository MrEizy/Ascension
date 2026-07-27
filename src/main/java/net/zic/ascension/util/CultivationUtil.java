package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;

import java.util.List;

public class CultivationUtil {
    public static final Identifier CULTIVATION_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"cultivation");
    public static final Identifier FOUNDATION_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"foundation");

    /**

     * TODO update canBreakthrough to tryBreakthrough, that way you can safely assume it is safe to trigger things like tribulations from it
     * TODO update to use effect Value
     * @param caster
     * @param source
     * @param pathData
     * @param secondaryPaths
     * @param baseRate
     */
    public static void cultivate(LivingEntity caster, OriginSource source, PathData pathData, List<Identifier> secondaryPaths, double baseRate){


        if(pathData.getCurrentTechnique() == null) return;
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,pathData.getCurrentTechnique(),source.getRegistryAccess());
        if(technique == null) return;
        TechniqueData data = pathData.getCurrentTechniqueData();

        double cultivationAmount = baseRate*(1+PathEffectValueUtil.getEffectiveAffinity(caster,pathData.getPath()));

        for(Identifier secondaryPath : secondaryPaths){
            cultivationAmount += baseRate*(1+PathEffectValueUtil.getEffectiveAffinity(caster,secondaryPath));
        }
        double maxProgress = pathData.getMaxProgress(pathData.getMajorRealm(),pathData.getMinorRealm(),source.getRegistryAccess());

        pathData.setProgress(Math.min(maxProgress,cultivationAmount+pathData.getProgress()));

        if(technique.tryBreakthrough(caster,source,pathData.getMajorRealm(),pathData.getMinorRealm(),pathData.getProgress(),data)){

            if(pathData.getMinorRealm() == pathData.getMaxMinorRealm(pathData.getMajorRealm(),source.getRegistryAccess())){
                pathData.handleRealmChange(source, pathData.getMajorRealm()+1,0);
            }else{
                pathData.handleRealmChange(source,pathData.getMajorRealm(), pathData.getMinorRealm()+1);
            }
            pathData.setProgress(0);
        }

        AscensionOriginSourceHelper.markPathDirty(source,pathData.getPath());
    }

    public static void cultivateFoundation(LivingEntity entity, OriginSource source, FoundationPathData foundationPathData, double baseRate){
        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,foundationPathData.getPath(),source.getRegistryAccess());

        if(!(path instanceof FoundationPath foundationPath)) return;

        double rate  =baseRate*(1+ PathEffectValueUtil.getEffectiveAffinity(entity,foundationPathData.getPath()));

        int majorRealm = foundationPathData.getMajorRealm();
        int foundationRealm = foundationPathData.getFoundationRealm(majorRealm);

        double maxProgress = foundationPath.getMaxFoundationProgress(majorRealm, foundationRealm);
        foundationPathData.setFoundationRealmProgress(majorRealm, foundationPathData.getFoundationRealmProgress(majorRealm) + rate);

        if(foundationPath.tryBreakthroughFoundation(
                entity,
                source,
                majorRealm,
                foundationRealm,
                foundationPathData.getFoundationRealmProgress(majorRealm)+rate)){
            foundationPathData.handleFoundationRealmChange(source, majorRealm,foundationRealm+1);
            foundationPathData.setFoundationRealmProgress(majorRealm,0);
        }

        AscensionOriginSourceHelper.markPathDirty(source,foundationPathData.getPath());
    }
}
