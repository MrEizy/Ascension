package net.zic.ascension.api.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface SkillData {

    void write(ValueOutput output);
    void encode(ByteBuf buf);
}
