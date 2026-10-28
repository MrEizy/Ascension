package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.Locale;

public record AlchemyRefinementTechnique(Stage stage, double heat, double control, double yield,
                                         double purityDelta, double instabilityDelta, double toxicityDelta,
                                         double safeEnergyBonus, double explosionEnergyBonus,
                                         double qualityBonus, double focusPrecision) {
    public enum Stage {
        EXTRACTION, HEATING, STABILIZATION, CONDENSATION;

        public static final Codec<Stage> CODEC = Codec.STRING.comapFlatMap(name -> {
            try {
                return DataResult.success(Stage.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return DataResult.error(() -> "Unknown alchemy technique stage: " + name);
            }
        }, stage -> stage.name().toLowerCase(Locale.ROOT));
    }

    public static final Codec<AlchemyRefinementTechnique> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Stage.CODEC.fieldOf("stage").forGetter(AlchemyRefinementTechnique::stage),
            Codec.DOUBLE.optionalFieldOf("heat", 1.0D).forGetter(AlchemyRefinementTechnique::heat),
            Codec.DOUBLE.optionalFieldOf("control", 2.5D).forGetter(AlchemyRefinementTechnique::control),
            Codec.DOUBLE.optionalFieldOf("yield", 1.0D).forGetter(AlchemyRefinementTechnique::yield),
            Codec.DOUBLE.optionalFieldOf("purity_delta", 0.0D).forGetter(AlchemyRefinementTechnique::purityDelta),
            Codec.DOUBLE.optionalFieldOf("instability_delta", 0.0D).forGetter(AlchemyRefinementTechnique::instabilityDelta),
            Codec.DOUBLE.optionalFieldOf("toxicity_delta", 0.0D).forGetter(AlchemyRefinementTechnique::toxicityDelta),
            Codec.DOUBLE.optionalFieldOf("safe_energy_bonus", 0.0D).forGetter(AlchemyRefinementTechnique::safeEnergyBonus),
            Codec.DOUBLE.optionalFieldOf("explosion_energy_bonus", 0.0D).forGetter(AlchemyRefinementTechnique::explosionEnergyBonus),
            Codec.DOUBLE.optionalFieldOf("quality_bonus", 0.0D).forGetter(AlchemyRefinementTechnique::qualityBonus),
            Codec.DOUBLE.optionalFieldOf("focus_precision", 0.0D).forGetter(AlchemyRefinementTechnique::focusPrecision)
    ).apply(instance, AlchemyRefinementTechnique::new));

    public AlchemyRefinementTechnique {
        if (stage == null) throw new IllegalArgumentException("Missing refinement stage");
        heat = bounded(heat, 0.1D, 4.0D, 1.0D);
        control = bounded(control, 0.1D, 8.0D, 2.5D);
        yield = bounded(yield, 0.25D, 2.0D, 1.0D);
        purityDelta = bounded(purityDelta, -0.25D, 0.25D, 0.0D);
        instabilityDelta = bounded(instabilityDelta, -1.0D, 1.0D, 0.0D);
        toxicityDelta = bounded(toxicityDelta, -1.0D, 1.0D, 0.0D);
        safeEnergyBonus = bounded(safeEnergyBonus, -2.5D, 4.0D, 0.0D);
        explosionEnergyBonus = bounded(explosionEnergyBonus, -3.0D, 5.0D, 0.0D);
        qualityBonus = bounded(qualityBonus, -0.15D, 0.15D, 0.0D);
        focusPrecision = bounded(focusPrecision, 0.0D, 0.4D, 0.0D);
    }

    private static double bounded(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Mth.clamp(value, min, max) : fallback;
    }
}
