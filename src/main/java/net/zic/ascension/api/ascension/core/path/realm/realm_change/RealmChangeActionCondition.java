package net.zic.ascension.api.ascension.core.path.realm.realm_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.UUID;

/**
 * this is ran to determine if the Listener should be run
 * instances of this are part of a registry that can be referenced in datapacks
 */
public interface RealmChangeActionCondition extends ProgressActionCondition {
    @Override
    default boolean test(OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction,String salt){
        if(!(contextData instanceof PathInstance pathInstance)) return false;


        if((pathInstance.getCurrentMajorRealm() == -1 && direction == ProgressDirection.DOWN) || (pathInstance.getCurrentMajorRealm() == 0 && pathInstance.getCurrentMinorRealm() == 0 && direction == ProgressDirection.UP)){
            return test(source,contextIdentifier,pathInstance,0,0,direction);
        }
        if(direction == ProgressDirection.UP){
            return test(source,contextIdentifier,pathInstance,pathInstance.getCurrentMajorRealm(),pathInstance.getCurrentMinorRealm(),direction);
        }
        Realm realm = pathInstance.getNextRealm();
        return test(source,contextIdentifier,pathInstance,realm.majorRealm(),realm.minorRealm(),direction);
    }

    /**
     *
     * @param source the source the path data is attached to
     * @param path the path
     * @param PathInstance the path data
     * @param majorRealm the major realm we are entering/leaving
     * @param minorRealm the minor realm we are entering/leaving
     * @param direction the direction
     * @return true-> run action false-> do not run action
     */
    boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction);

}
