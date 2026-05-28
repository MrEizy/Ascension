package net.zic.ascension.api.core.technique.realm_change;

import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.ascension.api.datapack.technique.realm_change.ListenerConditionType;

/**
 * this is ran to determine if the Listener should be run
 * instances of this are part of a registry that can be referenced in datapacks
 */
public interface ListenerCondition {

    /**
     * 0,0 up/down is a special realm condition. this is only run when FIRST getting a technique
     * (so if we do technique 1 0->2 technique 2 2->3 technique 1 3->4, it is called once on 1 and 2 even though 1 was added twice
     * same with down 0,0 this is only called when a technique is fully removed
     *
     * if you want to access the path data you can do so through the source
     *
     * TO CONSIDER replace source with whichever entity wrapper triggered the breakthrough?
     *
     * TODO remove once implemented
     * the default condtions are
     * ALL_MAJOR_REALMS -> triggers on every major realm breakthrough ( not including 0,0)
     * ALL_MINOT_REALMS -> triggers on every minor realm breakthrough ( not inclduing 0,0)
     *
     * EACH_MINOR_REALM_IN_MAJOR_REALMS -> takes in a set of major realms, runs for every minor realm in those set of major realms
     *
     * EACH_MAJOR_REALM_IN -> takes in a set of major realms, and runs for each major realm breakthrough
     *
     * EACH_MINOR_REALM_IN -> takes in a set of minor realm and major realms runs for each combination
     *
     * TECHNIQUE_STATUS_CHANGED -> runs when unique gained and unique lost
     *
     * @param source the data source that was changed
     * @param technique the technique used for the realm change
     * @param techniqueData the technique data of the technique
     * @param majorRealm direction == up the new realm, direction == down the old realm
     * @param minorRealm direction == up the new realm, direction == down the old realm
     * @param direction if we are going up into this realm or falling into i
     * @return true-> run listener false -> dont run listener
     */
    boolean test(OriginSource source, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, RealmChangeDirection direction);

    ListenerConditionType getType();
}
