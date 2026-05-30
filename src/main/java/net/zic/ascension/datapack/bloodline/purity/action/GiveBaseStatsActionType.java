package net.zic.ascension.datapack.bloodline.purity.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionType;
import net.zic.ascension.core.bloodline.purity.action.GiveBaseStatsAction;
import net.zic.zenithlib.value_containers.ValueContainer;

public class GiveBaseStatsActionType extends PurityChangeActionType {

    @Override
    public MapCodec<? extends PurityChangeAction> codec() {
        return RecordCodecBuilder.<GiveBaseStatsAction>mapCodec(instance ->
                instance.group(
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().fieldOf("stats").forGetter(GiveBaseStatsAction::baseStats)
                ).apply(instance, GiveBaseStatsAction::new)
        );
    }
}
