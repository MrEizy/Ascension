package net.zic.ascension.api.ascension.datapack.bloodline;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class BloodlineType {
    public abstract MapCodec<? extends Bloodline> codec();
    public abstract MapCodec<? extends BloodlineData> dataCodec();

    public static Codec<Bloodline> BLOODLINE_CODEC = TypeRegistries.BLOODLINE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Bloodline::getType,
                    BloodlineType::codec
            );
    public static Codec<BloodlineData> BLOODLINE_DATA_CODEC = TypeRegistries.BLOODLINE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    BloodlineData::getType,
                    BloodlineType::dataCodec
            );

}
