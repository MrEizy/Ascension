package net.zic.ascension.api.ascension.core.technique.realm_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

/**
 * this is ran to determine if the Listener should be run
 * instances of this are part of a registry that can be referenced in datapacks
 */
public interface RealmChangeActionCondition extends ProgressActionCondition {
    @Override
    default boolean test(OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction){
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(technique == null) return false;

        TechniqueData data = null;
        if(contextData instanceof TechniqueData) data = (TechniqueData) contextData;

        PathInstance PathInstance = AscensionOriginSourceHelper.getPathInstance(source,technique.getPath());
        if(PathInstance == null) return false;
        if((PathInstance.getCultivatedRealms(contextIdentifier).isEmpty()) ||
                (PathInstance.getCultivatedRealms(contextIdentifier).size() == 1 && direction == ProgressDirection.UP )){
             return test(source,PathInstance,technique,data,0,0,direction);
        }
        if(direction == ProgressDirection.UP){
            return test(source,PathInstance,technique,data,PathInstance.getMajorRealm(),PathInstance.getMinorRealm(),direction);
        }

        if(PathInstance.getMinorRealm() == PathInstance.getMaxMinorRealm(PathInstance.getMajorRealm(),source.getRegistryAccess())){
            //we fell down a major realm
            return test(source,PathInstance,technique, data, PathInstance.getMajorRealm()+1,0,direction);
        }

        return test(source,PathInstance,technique,data,PathInstance.getMajorRealm(),PathInstance.getMinorRealm()+1,direction);
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
    boolean test(OriginSource source, PathInstance PathInstance, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, ProgressDirection direction);


}
