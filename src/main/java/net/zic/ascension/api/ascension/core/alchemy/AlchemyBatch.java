package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AlchemyBatch(AlchemySubstance substance, int ingredientCount) {
    public static final Codec<AlchemyBatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AlchemySubstance.CODEC.fieldOf("substance").forGetter(AlchemyBatch::substance),
            Codec.INT.optionalFieldOf("ingredient_count", 0).forGetter(AlchemyBatch::ingredientCount)
    ).apply(instance, AlchemyBatch::new));

    public static final AlchemyBatch EMPTY = new AlchemyBatch(AlchemySubstance.EMPTY, 0);

    public AlchemyBatch {
        substance = substance == null ? AlchemySubstance.EMPTY : substance;
        ingredientCount = Math.max(0, ingredientCount);
    }

    public boolean isEmpty() {
        return ingredientCount == 0 || substance.isEmpty();
    }
}
