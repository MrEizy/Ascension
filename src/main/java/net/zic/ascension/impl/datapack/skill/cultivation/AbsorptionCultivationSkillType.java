package net.zic.ascension.impl.datapack.skill.cultivation;

import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.cultivation.AbsorptionCultivationSkill;

public final class AbsorptionCultivationSkillType extends SkillType {
    @Override
    public MapCodec<? extends Skill<?>> codec() {
        return AbsorptionCultivationSkill.CODEC;
    }
}
