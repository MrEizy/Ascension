package net.zic.ascension.datapack.progression.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.core.progression.GiveBaseStatsAction;
import net.zic.zenithlib.value_containers.ValueContainer;

public class GiveBaseStatsActionType extends ProgressActionType {

    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<GiveBaseStatsAction>mapCodec(instance ->
                instance.group(
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().fieldOf("stats").forGetter(GiveBaseStatsAction::baseStats)
                ).apply(instance, GiveBaseStatsAction::from)
        );
    }
}
