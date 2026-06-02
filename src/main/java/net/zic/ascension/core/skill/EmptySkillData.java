package net.zic.ascension.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.skill.SkillData;

public class EmptySkillData implements SkillData {
    @Override
    public void write(ValueOutput output) {

    }

    @Override
    public void encode(ByteBuf buf) {

    }
}
