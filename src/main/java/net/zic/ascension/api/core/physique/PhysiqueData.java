package net.zic.ascension.api.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.datapack.physique.PhysiqueType;

public interface PhysiqueData extends RegistryObjectData {



    PhysiqueType getType();
}
