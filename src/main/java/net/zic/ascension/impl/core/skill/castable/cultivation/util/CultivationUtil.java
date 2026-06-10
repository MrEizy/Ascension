package net.zic.ascension.impl.core.skill.castable.cultivation.util;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;

import java.util.List;

public class CultivationUtil {
    /**
     * TODO update canBreakthrough to tryBreakthrough, that way you can safely assume it is safe to trigger things like tribulations from it
     *
     * @param source
     * @param pathData
     * @param secondaryPaths
     * @param baseRate
     */
    public static void cultivate(OriginSource source, PathData pathData, List<Identifier> secondaryPaths,double baseRate){


        if(pathData.getCurrentTechnique() == null) return;
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,pathData.getCurrentTechnique(),source.getRegistryAccess());
        if(technique == null) return;
        TechniqueData data = pathData.getCurrentTechniqueData();

        double cultivationAmount = baseRate*(1+source.getAffinity(pathData.getPath()));

        for(Identifier secondaryPath : secondaryPaths){
            cultivationAmount += baseRate*(1+source.getAffinity(secondaryPath));
        }
        double maxProgress = pathData.getMaxProgress(pathData.getMajorRealm(),pathData.getMinorRealm(),source.getRegistryAccess());

        pathData.setProgress(Math.min(maxProgress,cultivationAmount+pathData.getProgress()));

        if(technique.canBreakthrough(source,pathData.getMajorRealm(),pathData.getMinorRealm(),pathData.getProgress(),data)){

            if(pathData.getMinorRealm() == pathData.getMaxMinorRealm(pathData.getMajorRealm(),source.getRegistryAccess())){
                pathData.handlerRealmChange(source, pathData.getMajorRealm()+1,0);
            }else{
                pathData.handlerRealmChange(source,pathData.getMajorRealm(), pathData.getMinorRealm()+1);
            }
            pathData.setProgress(0);
        }

    }
}
