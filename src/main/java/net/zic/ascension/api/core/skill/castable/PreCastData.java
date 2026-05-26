package net.zic.ascension.api.core.skill.castable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface PreCastData {

    void write(ValueOutput output);
    void encode(RegistryFriendlyByteBuf buf);
}
