package net.zic.ascension.api.datapack.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.skill.SkillType;

public abstract class TechniqueType {
    public abstract MapCodec<? extends Technique> codec();


    public static Codec<Technique> TECHNIQUE_CODEC = TypeRegistries.TECHNIQUE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Technique::getType,
                    TechniqueType::codec
            );
}
