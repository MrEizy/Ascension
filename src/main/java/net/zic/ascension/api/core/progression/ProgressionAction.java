package net.zic.ascension.api.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.source.OriginSource;

public interface ProgressionAction {
    /**
     * performs a generic action on a source assuming the Condition was met
     * @param source the origin source this condition is called on
     * @param contextIdentifier the Identifier of the registry object this action is being queried for(e.g a path, technique, bloodline, physique etc)
     * @param direction is the expected progression gained/up or lost/down
     */
    void run(OriginSource source, Identifier contextIdentifier, ProgressDirection direction);

}
