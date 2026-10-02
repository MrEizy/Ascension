package net.zic.ascension.api.ascension.core.skill.castable.action;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public interface SkillAction {
    Codec<SkillAction> CODEC = TypeRegistries.SKILL_ACTION_TYPE_REGISTRY.byNameCodec().dispatch(
            SkillAction::getType,
            CodecType<SkillAction>::codec
    );

    CodecType<SkillAction> getType();

    ActionSubject subject();

    void apply(SkillActionContext context);
}
