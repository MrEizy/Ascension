package net.zic.ascension.api.datapack;

import com.mojang.serialization.MapCodec;

public record CodecType<T>(MapCodec<? extends T> codec) {
}
