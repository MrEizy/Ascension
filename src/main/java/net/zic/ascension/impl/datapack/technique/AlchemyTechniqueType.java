package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.impl.core.technique.AlchemyTechnique;

public final class AlchemyTechniqueType extends TechniqueType {
    @Override public MapCodec<? extends Technique<?>> codec() {
        return AlchemyTechnique.CODEC;
    }
}
