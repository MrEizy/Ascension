package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.MapCodec;
import net.zic.ascension.impl.core.technique.SimpleTechnique;

/**
 * The codec of ascension:simple_technique as a MapCodec of SimpleTechnique, so the lost and connected techniques can extend it with
 * their own fields without repeating (and having to keep up with) the simple technique's fields.
 */
final class SimpleTechniqueCodecs {
    private SimpleTechniqueCodecs() {
    }

    @SuppressWarnings("unchecked")
    static MapCodec<SimpleTechnique> simple() {
        return (MapCodec<SimpleTechnique>) (MapCodec<?>) new SimpleTechniqueType().codec();
    }
}