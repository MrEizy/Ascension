package net.zic.ascension.api.ascension.datapack;

import com.mojang.serialization.MapCodec;

public record CodecType<T>(MapCodec<? extends T> codec) {
}
