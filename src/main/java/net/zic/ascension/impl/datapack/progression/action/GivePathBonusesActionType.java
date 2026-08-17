package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.GivePathBonusesAction;

public class GivePathBonusesActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<GivePathBonusesAction>mapCodec(instance ->
                instance.group(
                        PathBonusBase.CODEC.fieldOf("bonuses").forGetter(GivePathBonusesAction::bonuses)
                ).apply(instance, GivePathBonusesAction::from)
        );
    }
}
