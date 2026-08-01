package net.zic.ascension.impl.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

public class EmptySkillData implements SkillData {
    @Override
    public void write(ValueOutput output) {

    }

    @Override
    public void encode(ByteBuf buf) {

    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.SIMPLE_PASSIVE_SKILL_TYPE.get();
    }
}
