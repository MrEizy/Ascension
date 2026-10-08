package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.impl.core.progression.ModifierMergeMode;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.GiveStatsAction;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

public class GiveStatsActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<GiveStatsAction>mapCodec(instance ->
                instance.group(
                        ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).fieldOf("stats").forGetter(GiveStatsAction::stats),
                        Codec.BOOL.optionalFieldOf("per_purity", false).forGetter(GiveStatsAction::perPurity),
                        ModifierMergeMode.CODEC.optionalFieldOf("merge_mode", ModifierMergeMode.AUTO).forGetter(GiveStatsAction::mergeMode)
                ).apply(instance, GiveStatsAction::from)
        );
    }
}
