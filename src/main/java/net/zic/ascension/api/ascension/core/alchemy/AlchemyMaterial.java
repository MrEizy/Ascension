package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

public record AlchemyMaterial(
        AlchemyEssence essence,
        double purity,
        double instability,
        double refinementDifficulty
) {
    public static final Codec<AlchemyMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AlchemyEssence.CODEC.fieldOf("essence").forGetter(AlchemyMaterial::essence),
            Codec.DOUBLE.optionalFieldOf("purity", 1.0D).forGetter(AlchemyMaterial::purity),
            Codec.DOUBLE.optionalFieldOf("instability", 0.0D).forGetter(AlchemyMaterial::instability),
            Codec.DOUBLE.optionalFieldOf("refinement_difficulty", 1.0D).forGetter(AlchemyMaterial::refinementDifficulty)
    ).apply(instance, AlchemyMaterial::new));

    public static final AlchemyMaterial EMPTY = new AlchemyMaterial(AlchemyEssence.EMPTY, 1.0D, 0.0D, 0.0D);

    public AlchemyMaterial {
        essence = essence == null ? AlchemyEssence.EMPTY : essence;
        purity = Mth.clamp(Double.isFinite(purity) ? purity : 1.0D, 0.0D, 1.0D);
        instability = finiteNonNegative(instability);
        refinementDifficulty = finiteNonNegative(refinementDifficulty);
    }

    public boolean isEmpty() {
        return essence.isEmpty();
    }

    public AlchemySubstance substance() {
        return isEmpty() ? AlchemySubstance.EMPTY : new AlchemySubstance(
                essence.properties(),
                essence.affinities(),
                essence.rankTier(),
                essence.amplifier(),
                purity,
                instability
        );
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }
}
