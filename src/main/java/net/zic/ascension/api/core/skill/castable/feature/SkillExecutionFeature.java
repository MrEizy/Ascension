package net.zic.ascension.api.core.skill.castable.feature;

import net.zic.ascension.api.datapack.CodecType;
import com.mojang.serialization.Codec;
import net.zic.ascension.api.datapack.TypeRegistries;

public interface SkillExecutionFeature {
    Codec<SkillExecutionFeature> CODEC = TypeRegistries.SKILL_EXECUTION_FEATURE_TYPE_REGISTRY.byNameCodec().dispatch(
            SkillExecutionFeature::getType,
            CodecType<SkillExecutionFeature>::codec
    );

    CodecType<SkillExecutionFeature> getType();

    void apply(SkillExecutionContext context);
}
