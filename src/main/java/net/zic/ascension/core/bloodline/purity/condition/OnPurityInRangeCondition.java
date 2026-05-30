package net.zic.ascension.core.bloodline.purity.condition;

import net.zic.ascension.api.core.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeActionCondition;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionConditionType;
import net.zic.ascension.datapack.bloodline.purity.condition.AscensionPurityChangeActionConditionsTypes;

public record OnPurityInRangeCondition(int start,int end) implements PurityChangeActionCondition {

    @Override
    public boolean test(OriginSource source, Bloodline bloodline, BloodlineData bloodlineData, int purity, ProgressDirection direction) {
        return purity>=start && purity<=end;
    }

    @Override
    public PurityChangeActionConditionType getType() {
        return AscensionPurityChangeActionConditionsTypes.ON_PURITY_CONDITION.get();
    }


}
