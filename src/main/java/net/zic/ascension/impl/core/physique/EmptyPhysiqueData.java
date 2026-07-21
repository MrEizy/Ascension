package net.zic.ascension.impl.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.impl.datapack.physique.AscensionPhysiqueTypes;

public class EmptyPhysiqueData implements PhysiqueData {
    @Override
    public PhysiqueType getType() {
        return AscensionPhysiqueTypes.SIMPLE_PHYSIQUE_TYPE.get();
    }

    @Override
    public void write(ValueOutput output) {

    }

    @Override
    public void encode(ByteBuf buf) {

    }
}
