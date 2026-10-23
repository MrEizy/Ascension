package net.zic.ascension.api.ascension.datapack.bloodline;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class BloodlineType {
    public abstract MapCodec<? extends Bloodline<?>> codec();

    public static Codec<Bloodline<?>> BLOODLINE_CODEC = TypeRegistries.BLOODLINE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Bloodline::getType,
                    BloodlineType::codec
            );

}
