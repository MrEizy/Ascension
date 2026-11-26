package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.GiveScaledPurityStatsAction;

public final class GiveScaledPurityStatsActionType extends ProgressActionType {
    @Override public MapCodec<? extends ProgressAction> codec() { return GiveScaledPurityStatsAction.CODEC; }
}
