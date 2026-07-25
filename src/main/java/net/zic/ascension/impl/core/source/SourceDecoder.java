package net.zic.ascension.impl.core.source;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;


public class SourceDecoder implements Decoder<AscensionOriginSource> {

    @Override
    public <T> DataResult<Pair<AscensionOriginSource, T>> decode(DynamicOps<T> ops, T input) {
        Tag tag = ops.convertTo(NbtOps.INSTANCE, input);

        if (!(tag instanceof CompoundTag compound)) {
            return DataResult.error(() -> "Expected CompoundTag");
        }

        AscensionOriginSource source = new AscensionOriginSource(compound);
        return DataResult.success(Pair.of(source, input));
    }
}
