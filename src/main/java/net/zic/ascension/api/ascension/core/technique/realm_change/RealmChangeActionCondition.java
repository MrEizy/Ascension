package net.zic.ascension.api.ascension.core.technique.realm_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;

/**
 * this is ran to determine if the Listener should be run
 * instances of this are part of a registry that can be referenced in datapacks
 */
public interface RealmChangeActionCondition extends ProgressActionCondition {
    @Override
    default boolean test(AscensionOriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction){
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(technique == null) return false;

        TechniqueData data = null;
        if(contextData instanceof TechniqueData) data = (TechniqueData) contextData;

        PathData pathData = source.getPathData(technique.getPath());
        if(pathData == null) return false;
        if((pathData.getCultivatedRealms(contextIdentifier).isEmpty()) ||
                (pathData.getCultivatedRealms(contextIdentifier).size() == 1 && direction == ProgressDirection.UP )){
             return test(source,pathData,technique,data,0,0,direction);
        }
        if(direction == ProgressDirection.UP){
            return test(source,pathData,technique,data,pathData.getMajorRealm(),pathData.getMinorRealm(),direction);
        }

        if(pathData.getMinorRealm() == pathData.getMaxMinorRealm(pathData.getMajorRealm(),source.getRegistryAccess())){
            //we fell down a major realm
            return test(source,pathData,technique, data, pathData.getMajorRealm()+1,0,direction);
        }

        return test(source,pathData,technique,data,pathData.getMajorRealm(),pathData.getMinorRealm()+1,direction);
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
    boolean test(AscensionOriginSource source, PathData pathData, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, ProgressDirection direction);


}
