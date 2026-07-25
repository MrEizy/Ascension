package net.zic.ascension.impl.core.skill.castable.held;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

public final class HeldCastSkillData implements SkillData {
    @Override
    public void write(ValueOutput output) {
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.HELD_CAST_SKILL_TYPE.get();
    }
}
