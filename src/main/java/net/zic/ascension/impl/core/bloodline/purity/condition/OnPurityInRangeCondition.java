package net.zic.ascension.impl.core.bloodline.purity.condition;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.purity.PurityChangeActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

public record OnPurityInRangeCondition(int start, int end) implements PurityChangeActionCondition {

    @Override
    public boolean test(OriginSource source, Bloodline bloodline, BloodlineData bloodlineData, int purity, ProgressDirection direction) {
        //AscensionCraft.LOGGER.debug("Testing for purity : {}",purity);
        //AscensionCraft.LOGGER.debug("{} <= {} : {}",start,purity,(start<=purity));
        //AscensionCraft.LOGGER.debug("{} <= {} : {}",purity,end,(purity<=end));
        return start<=purity && purity<=end;
    }

    @Override
    public Component getDescription(RegistryAccess access) {
        if (start == 1 && end == 1) {
            return Component.translatable("ascension.tooltip.progression.condition.on_acquisition");
        }

        if (start == end) {
            return Component.translatable("ascension.tooltip.progression.condition.at_purity", start);
        }

        if (start == 1 && end == 100) {
            return Component.translatable("ascension.tooltip.progression.condition.each_purity", 1);
        }

        return Component.translatable("ascension.tooltip.progression.condition.purity_range", 1, start, end);
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.ON_PURITY_CONDITION.get();
    }
}
