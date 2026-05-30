package net.zic.ascension.core.source;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.zic.ascension.api.core.source.OriginSource;


public class SourceDecoder implements Decoder<OriginSource> {

    @Override
    public <T> DataResult<Pair<OriginSource, T>> decode(DynamicOps<T> ops, T input) {
        Tag tag = ops.convertTo(NbtOps.INSTANCE, input);

        if (!(tag instanceof CompoundTag compound)) {
            return DataResult.error(() -> "Expected CompoundTag");
        }

        OriginSource source = new OriginSource(compound);
        return DataResult.success(Pair.of(source, input));
    }
}
