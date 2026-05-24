package net.zic.ascension.api.datapack.bloodline;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;

public abstract class BloodlineType {
    public abstract MapCodec<? extends Bloodline> codec();


    public static Codec<Bloodline> BLOODLINE_CODEC = TypeRegistries.BLOODLINE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Bloodline::getType,
                    BloodlineType::codec
            );

}
