package net.zic.ascension.api.core.technique.realm_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;

/**
 * this is ran to determine if the Listener should be run
 * instances of this are part of a registry that can be referenced in datapacks
 */
public interface RealmChangeActionCondition extends ProgressActionCondition {
    @Override
    default boolean test( OriginSource source, Identifier contextIdentifier, ProgressDirection direction){
        //first test to make sure context is a bloodline
        Path path  = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(path == null) return false;
        PathData data = source.getPathData(contextIdentifier);
        if(data == null) return false;
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,data.getCurrentTechnique(),source.getRegistryAccess());
        if(direction == ProgressDirection.UP){
            return test(source,data,technique,data.getCurrentTechniqueData(),data.getMajorRealm(),data.getMinorRealm(),direction);
        }
        if(data.getMinorRealm() == data.getMaxMinorRealm(data.getMajorRealm(),source.getRegistryAccess())){
            //we fell down a major realm
            return test(source,data,technique, data.getCurrentTechniqueData(), data.getMajorRealm()+1,0,direction);
        }

        return test(source,data,technique,data.getCurrentTechniqueData(),data.getMajorRealm(),data.getMinorRealm()+1,direction);
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
    boolean test(OriginSource source,PathData pathData,Technique technique,TechniqueData techniqueData,int majorRealm,int minorRealm,ProgressDirection direction);


}
