package net.zic.ascension.impl.datapack.progression.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.core.progression.SetSkillLevelAction;

public final class SetSkillLevelActionType extends ProgressActionType {
    @Override
    public MapCodec<? extends ProgressAction> codec() {
        return RecordCodecBuilder.<SetSkillLevelAction>mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("skill").forGetter(SetSkillLevelAction::skill),
                Identifier.CODEC.fieldOf("contribution").forGetter(SetSkillLevelAction::contribution),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("level").forGetter(SetSkillLevelAction::level),
                Codec.BOOL.optionalFieldOf("set_floor", true).forGetter(SetSkillLevelAction::setFloor),
                Codec.BOOL.optionalFieldOf("set_cap", true).forGetter(SetSkillLevelAction::setCap)
        ).apply(instance, SetSkillLevelAction::new));
    }
}
