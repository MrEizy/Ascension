package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.foundation.FoundationPathInstance;

import java.util.List;

public class CultivationUtil {
    public static final Identifier CULTIVATION_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"cultivation");
    public static final Identifier FOUNDATION_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"foundation");

    /**

     * TODO update canBreakthrough to tryBreakthrough, that way you can safely assume it is safe to trigger things like tribulations from it
     * TODO update to use effect Value
     * @param caster
     * @param source
     * @param PathInstance
     * @param secondaryPaths
     * @param baseRate
     */
    public static void cultivate(LivingEntity caster, OriginSource source, PathInstance PathInstance, List<Identifier> secondaryPaths, double baseRate){


        if(PathInstance.getCurrentTechnique() == null) return;
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,PathInstance.getCurrentTechnique(),source.getRegistryAccess());
        if(technique == null) return;
        TechniqueData data = PathInstance.getCurrentTechniqueData();

        double cultivationAmount = baseRate*(1+PathEffectValueUtil.getEffectiveAffinity(caster,PathInstance.getPath()));

        for(Identifier secondaryPath : secondaryPaths){
            cultivationAmount += baseRate*(1+PathEffectValueUtil.getEffectiveAffinity(caster,secondaryPath));
        }
        double maxProgress = PathInstance.getMaxProgress(PathInstance.getMajorRealm(),PathInstance.getMinorRealm(),source.getRegistryAccess());

        PathInstance.setProgress(Math.min(maxProgress,cultivationAmount+PathInstance.getProgress()));

        if(technique.tryBreakthrough(caster,source,PathInstance.getMajorRealm(),PathInstance.getMinorRealm(),PathInstance.getProgress(),data)){

            if(PathInstance.getMinorRealm() == PathInstance.getMaxMinorRealm(PathInstance.getMajorRealm(),source.getRegistryAccess())){
                PathInstance.handleRealmChange(source, PathInstance.getMajorRealm()+1,0);
            }else{
                PathInstance.handleRealmChange(source,PathInstance.getMajorRealm(), PathInstance.getMinorRealm()+1);
            }
            PathInstance.setProgress(0);
        }

        AscensionOriginSourceHelper.markPathDirty(source,PathInstance.getPath());
    }

    public static void cultivateFoundation(LivingEntity entity, OriginSource source, FoundationPathInstance foundationPathInstance, double baseRate){
        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,foundationPathInstance.getPath(),source.getRegistryAccess());

        if(!(path instanceof FoundationPath foundationPath)) return;

        double rate  =baseRate*(1+ PathEffectValueUtil.getEffectiveAffinity(entity,foundationPathInstance.getPath()));

        int majorRealm = foundationPathInstance.getMajorRealm();
        int foundationRealm = foundationPathInstance.getFoundationRealm(majorRealm);

        double maxProgress = foundationPath.getMaxFoundationProgress(majorRealm, foundationRealm);
        foundationPathInstance.setFoundationRealmProgress(majorRealm, foundationPathInstance.getFoundationRealmProgress(majorRealm) + rate);

        if(foundationPath.tryBreakthroughFoundation(
                entity,
                source,
                majorRealm,
                foundationRealm,
                foundationPathInstance.getFoundationRealmProgress(majorRealm)+rate)){
            foundationPathInstance.handleFoundationRealmChange(source, majorRealm,foundationRealm+1);
            foundationPathInstance.setFoundationRealmProgress(majorRealm,0);
        }

        AscensionOriginSourceHelper.markPathDirty(source,foundationPathInstance.getPath());
    }
}
