package net.zic.ascension.api.ascension.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueOutput;

public interface TechniqueData {


    public void write(ValueOutput output);


    public void encode(ByteBuf buf);
}
