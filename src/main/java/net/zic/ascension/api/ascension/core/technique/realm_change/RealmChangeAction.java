package net.zic.ascension.api.ascension.core.technique.realm_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.UUID;

/**
 * Runs on realm change, holds either the realm we are entering or the one we are leaving.
 *
 * special cases
 * 0,0 UP refers to when we first learn a technique, even if we learn it at 4,2 as long as it is the first time it is triggered
 * 0,0 DOWN refers to when we fully remove a technique and the PathInstance::cultivates() returns false
 */
public interface RealmChangeAction extends ProgressAction {


    @Override
    default void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction){
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(technique == null) return;

        TechniqueData data = null;
        if(contextData instanceof TechniqueData) data = (TechniqueData) contextData;

        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source,technique.getPath());
        if(pathInstance == null) return;

        if((pathInstance.getCurrentMajorRealm() == -1 && direction == ProgressDirection.DOWN) || (pathInstance.getCurrentMajorRealm() == 0 && pathInstance.getCurrentMinorRealm() == 0 && direction == ProgressDirection.UP)){
            run(holderId,source,pathInstance,technique,data,0,0,direction);
        }else{

            if(direction == ProgressDirection.UP){
                run(holderId,source,pathInstance,technique,data,pathInstance.getCurrentMajorRealm(),pathInstance.getCurrentMinorRealm(),direction);
            }else{
                if(pathInstance.getCurrentMajorRealm() == pathInstance.getMaxMinorRealm(pathInstance.getCurrentMajorRealm())){
                    //we fell down a major realm
                    run(holderId,source,pathInstance,technique, data, pathInstance.getCurrentMajorRealm()+1,0,direction);
                }else{
                    run(holderId,source,pathInstance,technique,data,pathInstance.getCurrentMajorRealm(),pathInstance.getCurrentMinorRealm()+1,direction);
                }
            }
        }

    }

    /**
     *
     * @param holderId the id of the holder, used for unique identifiers
     * @param source the source the path data is attached to
     * @param PathInstance the path data
     * @param technique the current technique
     * @param techniqueData the current technique data
     * @param majorRealm the major realm we are entering/leaving
     * @param minorRealm the minor realm we are entering/leaving
     * @param direction the direction
     */
    void run(UUID holderId, OriginSource source, PathInstance PathInstance, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, ProgressDirection direction);

}
