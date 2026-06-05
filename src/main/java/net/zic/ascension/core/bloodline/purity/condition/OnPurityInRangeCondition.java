package net.zic.ascension.core.bloodline.purity.condition;

import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeActionCondition;
import net.zic.ascension.api.core.source.OriginSource;

import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.datapack.progression.AscensionProgressActionConditionTypes;

public record OnPurityInRangeCondition(int start,int end) implements PurityChangeActionCondition {

    @Override
    public boolean test(OriginSource source, Bloodline bloodline, BloodlineData bloodlineData, int purity, ProgressDirection direction) {
        AscensionCraft.LOGGER.debug("Testing for purity : {}",purity);
        AscensionCraft.LOGGER.debug("{} <= {} : {}",start,purity,(start<=purity));
        AscensionCraft.LOGGER.debug("{} <= {} : {}",purity,end,(purity<=end));
        return start<=purity && purity<=end;
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.ON_PURITY_CONDITION.get();
    }


}
