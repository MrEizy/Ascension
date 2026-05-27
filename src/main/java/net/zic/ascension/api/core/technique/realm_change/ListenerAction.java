package net.zic.ascension.api.core.technique.realm_change;

import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.ascension.api.datapack.technique.realm_change.ListenerActionType;

/**
 * The action taken when a realm change happens that matches the Listener condition
 *
 * Same as conditions there will be a ActionType Registry that holds how to construct these
 * //TODO remove when implemented
 * Some Default types will include
 *
 * give base stats -> takes in a list of stats and a value to add on to the base
 * give stat modifiers -> takes in a list of modifiers (make a value modifier codex) to apply
 *
 * give skills -> takes in a list of skills to give
 *
 * give paths -> takes in a list of paths to unlock
 *
 * give path bonus -> takes in a list of path bonuses to unlock
 *
 * give path bonus modifiers -> takes in a list of path bonus modifiers to unlock
 *
 *
 */
public interface ListenerAction {
    void run(OriginSource source, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, RealmChangeDirection direction);

    ListenerActionType getType();
}
