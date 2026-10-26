package net.zic.ascension.api.ascension.datapack.golden_core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCore;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class GoldenCoreType {


    public abstract MapCodec<? extends GoldenCore<?>> codec();


    public static final Codec<GoldenCore<?>> GOLDEN_CORE_CODEC = TypeRegistries.GOLDEN_CORE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    GoldenCore::getType,
                    GoldenCoreType::codec
            );
}
