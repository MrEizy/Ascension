package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.GivePathBonusesAction;
import net.zic.ascension.impl.core.progression.modifier.ModifierConflictAction;

public class GivePathBonusesActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<GivePathBonusesAction>mapCodec(instance ->
                instance.group(
                        PathBonusHolder.MODIFIER_CODEC.fieldOf("bonuses").forGetter(GivePathBonusesAction::bonuses),
                        ModifierConflictAction.CODEC.optionalFieldOf("conflict_action", ModifierConflictAction.SALT).forGetter(GivePathBonusesAction::conflictAction)
                ).apply(instance, GivePathBonusesAction::from)
        );
    }
}
