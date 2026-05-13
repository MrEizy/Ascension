package net.thejadeproject.ascension.refactor_packages.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.NineHeavenlyTribulations;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.events.CultivateEvent;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;

import java.util.List;

public class CultivationUtil {


    public static boolean tryCultivate(Entity entity, ResourceLocation path,List<ResourceLocation> attributedPaths,double amount){
        IEntityData entityData = entity.getData(ModAttachments.ENTITY_DATA);
        //System.out.println("Player is trying to cultivate");
        PathData pathData = entity.getData(ModAttachments.ENTITY_DATA).getPathData(path);

        if(pathData == null) return false;
        if(pathData.isBreakingThrough()) return false;

        ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());

        double base = amount*(entityData.getPathBonusHandler().getPathBonus(path));
        for(ResourceLocation attributedPath : attributedPaths){
            base *= entityData.getPathBonusHandler().getPathBonus(attributedPath);
        }
        CultivateEvent event = new CultivateEvent(entity,base,path, attributedPaths);
        NeoForge.EVENT_BUS.post(event);


        if(pathData.getCurrentRealmProgress()+event.getRate() >= technique.getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm())){
            //TODO minor/major realm breakthrough shenanigans here
            pathData.setCurrentRealmProgress(technique.getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm()));

            //TODO for now just force breakthrough
            if(pathData.getMinorRealm() < technique.getMaxMinorRealm(pathData.getMajorRealm()) && technique.canBreakthroughMinorRealm(
                    entity.getData(ModAttachments.ENTITY_DATA),
                    pathData.getMajorRealm(),
                    pathData.getMinorRealm(),
                    pathData.getCurrentRealmProgress()
            )){
                pathData.handleRealmChange(pathData.getMajorRealm(),pathData.getMinorRealm()+1,entity.getData(ModAttachments.ENTITY_DATA));
            } else if(pathData.getMajorRealm()<technique.getMaxMajorRealm() && technique.getStabilityHandler() != null && pathData.getCurrentRealmStability() < technique.getStabilityHandler().getMaxCultivationTicks()) {
                //TODO temporary
                IBreakthroughInstance instance = new NineHeavenlyTribulations(1);
                pathData.setBreakthroughInstance(instance);
                pathData.setBreakingThrough(true);
                //pathData.handleRealmChange(pathData.getMajorRealm()+1,0,entityData);
                //pathData.setCurrentRealmStability(pathData.getCurrentRealmStability()+1);
            }
        }else {
            pathData.setCurrentRealmProgress(pathData.getCurrentRealmProgress()+event.getRate());
        }
        return true;
    }
}
