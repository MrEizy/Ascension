package net.zic.ascension.core.bloodline.purity.action;

import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionType;
import net.zic.ascension.datapack.bloodline.purity.action.AscensionPurityChangeActionTypes;
import net.zic.ascension.datapack.bloodline.purity.action.GiveBaseStatsActionType;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;

import java.util.List;
import java.util.UUID;

public record GiveBaseStatsAction(List<ValueContainer.BaseModifier> baseStats) implements PurityChangeAction {

    @Override
    public void run(UUID handlerId, OriginSource source, Bloodline bloodline, BloodlineData bloodlineData, int purity, ProgressDirection direction) {
        for(ValueContainer.BaseModifier modifier : baseStats){
           if(direction == ProgressDirection.UP) source.addStat(ZenithRegistries.STAT_REGISTRY.getValue(modifier.container()), modifier.val());
           else source.removeStat(ZenithRegistries.STAT_REGISTRY.getValue(modifier.container()),modifier.val());
        }
    }

    @Override
    public PurityChangeActionType getType() {
        return AscensionPurityChangeActionTypes.GIVE_BASE_STATS_TYPE.get();
    }
}
