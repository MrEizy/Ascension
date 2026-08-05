package net.zic.ascension.api.ascension.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;

public interface SkillData {


    SkillType getType();
    void write(ValueOutput output, RegistryAccess access);
    void encode(ByteBuf buf,RegistryAccess access);
}
