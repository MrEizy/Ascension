package net.zic.ascension.api.core.technique.realm_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;

import java.util.UUID;

/**
 * Runs on realm change, holds either the realm we are entering or the one we are leaving.
 *
 * special cases
 * 0,0 UP refers to when we first learn a technique, even if we learn it at 4,2 as long as it is the first time it is triggered
 * 0,0 DOWN refers to when we fully remove a technique and the PathData::cultivates() returns false
 */
public interface RealmChangeAction extends ProgressAction {


    @Override
    default void run(UUID holderId, OriginSource source, Identifier contextIdentifier, ProgressDirection direction){
        //first test to make sure context is a bloodline
        Path path  = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(path == null) return;
        PathData data = source.getPathData(contextIdentifier);
        if(data == null) return;
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,data.getCurrentTechnique(),source.getRegistryAccess());
        if(direction == ProgressDirection.UP){
            run(holderId,source,data,technique,data.getCurrentTechniqueData(),data.getMajorRealm(),data.getMinorRealm(),direction);
        }else{
            if(data.getMinorRealm() == data.getMaxMinorRealm(data.getMajorRealm(),source.getRegistryAccess())){
                //we fell down a major realm
                run(holderId,source,data,technique, data.getCurrentTechniqueData(), data.getMajorRealm()+1,0,direction);
            }else{
                run(holderId,source,data,technique,data.getCurrentTechniqueData(),data.getMajorRealm(),data.getMinorRealm()+1,direction);
            }
        }
    }

    /**
     *
     * @param holderId the id of the holder, used for unique identifiers
     * @param source the source the path data is attached to
     * @param pathData the path data
     * @param technique the current technique
     * @param techniqueData the current technique data
     * @param majorRealm the major realm we are entering/leaving
     * @param minorRealm the minor realm we are entering/leaving
     * @param direction the direction
     */
    void run(UUID holderId,OriginSource source,PathData pathData,Technique technique,TechniqueData techniqueData,int majorRealm,int minorRealm,ProgressDirection direction);

}
