package net.zic.ascension.api.core.bloodline.purity;

import net.zic.ascension.api.core.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionConditionType;

/**
 * every purity change this is tested, if true we run the purity change action it is associated with
 */
public interface PurityChangeActionCondition {
    /**
     * purity 1 up/down is a special condition, equivalent to adding/removing said bloodline
     * @param source
     * @param bloodline
     * @param bloodlineData
     * @param direction
     * @return
     */
    boolean test(OriginSource source, Bloodline bloodline, BloodlineData bloodlineData,int purity, ProgressDirection direction);


    PurityChangeActionConditionType getType();
}
