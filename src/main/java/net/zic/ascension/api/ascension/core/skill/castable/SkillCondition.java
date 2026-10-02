package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public interface SkillCondition {
    Codec<SkillCondition> CODEC = TypeRegistries.SKILL_CONDITION_TYPE_REGISTRY.byNameCodec().dispatch(
            SkillCondition::getType,
            CodecType<SkillCondition>::codec
    );

    CodecType<SkillCondition> getType();

    boolean test(SkillActionContext context);
}
