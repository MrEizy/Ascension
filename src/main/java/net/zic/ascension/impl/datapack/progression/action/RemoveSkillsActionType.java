package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.RemoveSkillsAction;

public class RemoveSkillsActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<RemoveSkillsAction>mapCodec(instance -> instance.group(
                Identifier.CODEC.listOf().fieldOf("skills").forGetter(RemoveSkillsAction::skills)
        ).apply(instance, RemoveSkillsAction::from));
    }
}
