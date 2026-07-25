package net.zic.ascension.impl.core.bloodline.purity.condition;

import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.purity.PurityChangeActionCondition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;

import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

public record OnPurityInRangeCondition(int start,int end) implements PurityChangeActionCondition {

    @Override
    public boolean test(AscensionOriginSource source, Bloodline bloodline, BloodlineData bloodlineData, int purity, ProgressDirection direction) {
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
