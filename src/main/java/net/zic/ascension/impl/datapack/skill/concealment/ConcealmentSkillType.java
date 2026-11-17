package net.zic.ascension.impl.datapack.skill.concealment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.concealment.ConcealmentSkill;
import net.zic.ascension.impl.core.skill.passive.PassiveSkill;
import net.zic.ascension.impl.datapack.skill.passive.PassiveSkillType;

public final class ConcealmentSkillType extends SkillType {
    private static final MapCodec<PassiveSkill> BASE_CODEC = PassiveSkillType.toggleableDefaultCodec().flatXmap(
            passive -> passive.isToggleable() ? DataResult.success(passive) : DataResult.error(() -> "Concealment skills must be toggleable"),
            DataResult::success
    );
    private static final MapCodec<ConcealmentSkill> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BASE_CODEC.forGetter(skill -> skill),
            ConcealmentSkill.Identity.CODEC.optionalFieldOf("identity", ConcealmentSkill.Identity.EMPTY).forGetter(ConcealmentSkill::identity),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ConcealmentSkill::priority)
    ).apply(instance, ConcealmentSkill::new));

    @Override
    public MapCodec<? extends Skill<?>> codec() {
        return CODEC;
    }
}
