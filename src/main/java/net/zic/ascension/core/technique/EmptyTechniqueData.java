package net.zic.ascension.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;

public class EmptyTechniqueData implements TechniqueData {
    @Override
    public void write(ValueOutput output) {

    }

    @Override
    public void encode(ByteBuf buf) {

    }
}
