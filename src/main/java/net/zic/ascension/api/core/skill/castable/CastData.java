package net.zic.ascension.api.core.skill.castable;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface CastData {

    void encode(RegistryFriendlyByteBuf buf);
}
