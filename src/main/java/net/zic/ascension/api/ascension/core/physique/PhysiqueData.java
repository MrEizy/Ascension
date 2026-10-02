package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;

public interface PhysiqueData {



    PhysiqueType getType();
    void write(ValueOutput output, RegistryAccess access);
    void encode(ByteBuf buf,RegistryAccess access);
}
