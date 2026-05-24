package net.zic.ascension.api.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.datapack.TypeRegistries;

import java.util.function.Supplier;

public abstract class PhysiqueType {


    public abstract MapCodec<? extends Physique> codec();


    public static Codec<Physique> PHYSIQUE_CODEC = TypeRegistries.PHYSIQUE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Physique::getType,
                    PhysiqueType::codec
            );
}
