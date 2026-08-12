package net.zic.ascension.impl.core.skill.castable.cultivation;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

public final class SimpleCultivationSkillData implements SkillData {
    @Override
     public void write(ValueOutput output, RegistryAccess access) {

    }

    @Override
    public void encode(ByteBuf buf, RegistryAccess access) {

    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.SIMPLE_CULTIVATION_SKILL_TYPE.get();
    }
}
