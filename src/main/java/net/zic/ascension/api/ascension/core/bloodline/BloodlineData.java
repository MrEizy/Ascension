package net.zic.ascension.api.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;

public interface BloodlineData {
    int getPurity();
    void setPurity(int newPurity);


    BloodlineType getType();

    void write(ValueOutput output, RegistryAccess access);
    void encode(ByteBuf buf,RegistryAccess access);
}
