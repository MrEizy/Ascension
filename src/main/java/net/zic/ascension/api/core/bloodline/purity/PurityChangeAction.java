package net.zic.ascension.api.core.bloodline.purity;

import net.zic.ascension.api.core.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionType;

import java.util.UUID;

/**
 * Similar to realm change handler but for bloodline purity
 */
public interface PurityChangeAction {
    /**
     *
     * @param handlerId the unique Id of the handler which can be used for modifier Id
     * @param source
     * @param bloodline
     * @param bloodlineData
     * @param direction
     * @return
     */
    void run(UUID handlerId, OriginSource source, Bloodline bloodline, BloodlineData bloodlineData,int purity, ProgressDirection direction);


    PurityChangeActionType getType();
}
