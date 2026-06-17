package net.zic.ascension.api.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.datapack.skill.SkillType;

public interface SkillData extends RegistryObjectData {


    SkillType getType();
}
