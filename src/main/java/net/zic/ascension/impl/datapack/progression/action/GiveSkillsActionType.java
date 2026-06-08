package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.GiveSkillsAction;

public class GiveSkillsActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<GiveSkillsAction>mapCodec(instance ->
                instance.group(
                        Identifier.CODEC.listOf().fieldOf("skills").forGetter(GiveSkillsAction::skills)
                ).apply(instance, GiveSkillsAction::from)
        );
    }
}
