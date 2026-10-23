package net.zic.ascension.api.ascension.core.path.realm.realm_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
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
    default void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction,String salt){

        if(!(contextData instanceof PathInstance<?> pathInstance)) return;



        if((pathInstance.getCurrentMajorRealm() == -1 && direction == ProgressDirection.DOWN) || (pathInstance.getCurrentMajorRealm() == 0 && pathInstance.getCurrentMinorRealm() == 0 && direction == ProgressDirection.UP)){
            run(holderId,source,contextIdentifier,pathInstance,0,0,direction);
        }else{

            if(direction == ProgressDirection.UP){
                run(holderId,source,contextIdentifier,pathInstance,pathInstance.getCurrentMajorRealm(),pathInstance.getCurrentMinorRealm(),direction);
            }else{
                Realm realm = pathInstance.getNextRealm();
                run(holderId,source,contextIdentifier,pathInstance,realm.majorRealm(),realm.minorRealm(),direction);

            }
        }

    }

    /**
     *
     * @param holderId the id of the holder, used for unique identifiers
     * @param source the source the path data is attached to
     * @param path the path
     * @param pathInstance the path instance
     * @param majorRealm the major realm we are entering/leaving
     * @param minorRealm the minor realm we are entering/leaving
     * @param direction the direction
     */
    void run(UUID holderId, OriginSource source,Identifier path, PathInstance<?> pathInstance, int majorRealm, int minorRealm, ProgressDirection direction);

}
