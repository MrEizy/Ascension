package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.impl.core.progression.ModifierMergeMode;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.GivePathBonusesAction;

public class GivePathBonusesActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<GivePathBonusesAction>mapCodec(instance ->
                instance.group(
                        PathBonusHolder.MODIFIER_CODEC.fieldOf("bonuses").forGetter(GivePathBonusesAction::bonuses),
                        Codec.BOOL.optionalFieldOf("per_purity", false).forGetter(GivePathBonusesAction::perPurity),
                        ModifierMergeMode.CODEC.optionalFieldOf("merge_mode", ModifierMergeMode.AUTO).forGetter(GivePathBonusesAction::mergeMode)
                ).apply(instance, GivePathBonusesAction::from)
        );
    }
}
