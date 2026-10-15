package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.GivePathBonusesUsingPurityAction;
import net.zic.ascension.impl.core.progression.modifier.ModifierConflictAction;

public class GivePathBonusesUsingPurityActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<GivePathBonusesUsingPurityAction>mapCodec(instance ->
                instance.group(
                        PathBonusHolder.MODIFIER_CODEC.fieldOf("bonuses").forGetter(GivePathBonusesUsingPurityAction::bonuses),
                        ModifierConflictAction.CODEC.optionalFieldOf("conflict_action", ModifierConflictAction.SALT).forGetter(GivePathBonusesUsingPurityAction::conflictAction)
                ).apply(instance, GivePathBonusesUsingPurityAction::from)
        );
    }
}
