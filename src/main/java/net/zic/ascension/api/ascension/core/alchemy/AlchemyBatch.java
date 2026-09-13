package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.configuration.interactions.PathInteractions;

import java.util.LinkedHashMap;
import java.util.Map;

public record AlchemyBatch(AlchemySubstance substance, int ingredientCount) {
    private static final double EPSILON = 1.0E-12D;

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

    public MergeResult merge(AlchemySubstance incoming, double safeDelta, double explosionDelta) {
        double safe = finiteNonNegative(safeDelta);
        double explosion = Math.max(safe, finiteNonNegative(explosionDelta));

        if (incoming == null || incoming.isEmpty()) {
            return new MergeResult(this, Outcome.STABLE, 0.0D);
        }
        if (isEmpty()) {
            return new MergeResult(new AlchemyBatch(incoming, 1), Outcome.STABLE, 0.0D);
        }

        LinkedHashMap<Identifier, Double> existingAffinities = new LinkedHashMap<>(substance.affinities());
        LinkedHashMap<Identifier, Double> incomingAffinities = new LinkedHashMap<>(incoming.affinities());
        Map<Identifier, Double> originalExisting = Map.copyOf(existingAffinities);
        Map<Identifier, Double> originalIncoming = Map.copyOf(incomingAffinities);

        double energyDelta = applyInteractions(originalIncoming, originalExisting, existingAffinities) + applyInteractions(originalExisting, originalIncoming, incomingAffinities);
        energyDelta = Math.max(0.0D, energyDelta);

        LinkedHashMap<Identifier, Double> affinities = new LinkedHashMap<>(existingAffinities);
        incomingAffinities.forEach((id, amount) -> affinities.merge(id, amount, Double::sum));
        affinities.entrySet().removeIf(entry -> entry.getValue() <= EPSILON);

        LinkedHashMap<Identifier, Double> properties = new LinkedHashMap<>(substance.properties());
        incoming.properties().forEach((id, amount) -> properties.merge(id, amount, Double::sum));

        int nextCount = ingredientCount + 1;
        double purity = weightedAverage(substance.purity(), ingredientCount, incoming.purity(), 1.0D);
        double instability = weightedAverage(substance.instability(), ingredientCount, incoming.instability(), 1.0D)
                + energyDelta / Math.max(1.0D, nextCount);

        AlchemySubstance merged = new AlchemySubstance(
                properties,
                affinities,
                Math.min(substance.rankTier(), incoming.rankTier()),
                purity,
                instability
        );

        Outcome outcome = energyDelta <= safe ? Outcome.STABLE : energyDelta <= explosion ? Outcome.UNSTABLE : Outcome.CATASTROPHIC;
        return new MergeResult(new AlchemyBatch(merged, nextCount), outcome, energyDelta);
    }

    private static double applyInteractions(Map<Identifier, Double> sources, Map<Identifier, Double> targets, Map<Identifier, Double> mutableTargets) {
        PathInteractions.FrozenPathInteractions interactions = PathInteractions.getInstance();
        if (interactions == null) {
            return 0.0D;
        }

        double energyDelta = 0.0D;
        for (Map.Entry<Identifier, Double> source : sources.entrySet()) {
            for (Map.Entry<Identifier, Double> target : targets.entrySet()) {
                if (source.getKey().equals(target.getKey())) {
                    continue;
                }

                PathInteraction interaction = interactions.getInteraction(source.getKey(), target.getKey());
                if (interaction == null || !Double.isFinite(interaction.value()) || interaction.value() <= 0.0D) {
                    continue;
                }

                double value = interaction.value();
                switch (interaction.type()) {
                    case GENERATIVE -> {
                        double delta = source.getValue() * value;
                        mutableTargets.merge(target.getKey(), delta, Double::sum);
                        energyDelta += delta * 0.25D;
                    }
                    case DESTRUCTIVE -> {
                        double current = mutableTargets.getOrDefault(target.getKey(), 0.0D);
                        double delta = Math.min(current, source.getValue() * value);
                        mutableTargets.put(target.getKey(), Math.max(0.0D, current - delta));
                        energyDelta += delta;
                    }
                    case RELATED -> energyDelta -= Math.min(source.getValue(), target.getValue()) * value * 0.5D;
                }
            }
        }
        return energyDelta;
    }

    private static double weightedAverage(double first, double firstWeight, double second, double secondWeight) {
        double total = firstWeight + secondWeight;
        return total <= EPSILON ? (first + second) * 0.5D : (first * firstWeight + second * secondWeight) / total;
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : Double.MAX_VALUE;
    }

    public enum Outcome {
        STABLE,
        UNSTABLE,
        CATASTROPHIC
    }

    public record MergeResult(AlchemyBatch batch, Outcome outcome, double energyDelta) {
        public MergeResult {
            batch = batch == null ? EMPTY : batch;
            outcome = outcome == null ? Outcome.STABLE : outcome;
            energyDelta = Double.isFinite(energyDelta) ? Math.max(0.0D, energyDelta) : 0.0D;
        }
    }
}
