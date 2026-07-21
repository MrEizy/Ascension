package net.zic.ascension.api.ascension.datapack.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class SkillType {
    public abstract MapCodec<? extends Skill> codec();
    public abstract MapCodec<? extends SkillData> dataCodec();


    public static Codec<Skill> SKILL_CODEC = TypeRegistries.SKILL_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Skill::getType,
                    SkillType::codec
            );

    public static Codec<SkillData> SKILL_DATA_CODEC = TypeRegistries.SKILL_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    SkillData::getType,
                    SkillType::dataCodec
            );

}
