package net.zic.ascension.api.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.tribulation.Tribulation;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.technique.TechniqueType;

public abstract class TribulationType {
    public abstract MapCodec<? extends Tribulation> codec();


    public static Codec<Tribulation> TRIBULATION_CODEC = TypeRegistries.TRIBULATION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Tribulation::getType,
                    TribulationType::codec
            );
}
