package net.zic.ascension.api.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;

public interface BloodlineData extends RegistryObjectData {
    int getPurity();
    void setPurity(int newPurity);


    BloodlineType getType();

}
