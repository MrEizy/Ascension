package net.zic.ascension.impl.datapack.skill;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.SkillExecutionDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.held.HeldCastSpec;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.castable.held.HeldCastSkill;
import net.zic.ascension.impl.core.skill.castable.held.HeldCastSkillData;

public final class HeldCastSkillType extends SkillType {
    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<HeldCastSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(HeldCastSkill::name),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(HeldCastSkill::description),
                HeldCastSpec.CODEC.fieldOf("cast").forGetter(HeldCastSkill::cast),
                SkillExecutionDefinition.CODEC.fieldOf("execution").forGetter(HeldCastSkill::execution)
        ).apply(instance, HeldCastSkill::new));
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return MapCodec.unit(HeldCastSkillData::new);
    }
}
