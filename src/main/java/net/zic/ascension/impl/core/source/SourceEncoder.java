package net.zic.ascension.impl.core.source;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.zic.ascension.api.ascension.core.source.OriginSource;

public class SourceEncoder implements Encoder<OriginSource> {
    @Override
    public <T> DataResult<T> encode(OriginSource input, DynamicOps<T> ops, T prefix) {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        input.write(output);
        return DataResult.success(NbtOps.INSTANCE.convertTo(ops,output.buildResult()));
    }
}
