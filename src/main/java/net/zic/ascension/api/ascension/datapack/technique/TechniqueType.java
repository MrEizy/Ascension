package net.zic.ascension.api.ascension.datapack.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class TechniqueType {
    public abstract MapCodec<? extends Technique> codec();


    public static Codec<Technique> TECHNIQUE_CODEC = TypeRegistries.TECHNIQUE_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Technique::getType,
                    TechniqueType::codec
            );
}
