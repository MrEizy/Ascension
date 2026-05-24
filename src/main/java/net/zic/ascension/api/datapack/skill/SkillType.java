package net.zic.ascension.api.datapack.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;

public abstract class SkillType {
    public abstract MapCodec<? extends Skill> codec();


    public static Codec<Skill> SKILL_CODEC = TypeRegistries.SKILL_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Skill::getType,
                    SkillType::codec
            );
}
