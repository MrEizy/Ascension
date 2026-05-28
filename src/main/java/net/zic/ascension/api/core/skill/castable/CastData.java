package net.zic.ascension.api.core.skill.castable;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface CastData {

    void encode(ByteBuf buf);
}
