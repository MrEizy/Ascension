package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.configuration.interactions.PathInteractions;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AlchemyMergeResolver {
    private static final double EPSILON = 1.0E-12D;

    private AlchemyMergeResolver() {
    }

    public static MergeResult merge(AlchemyBatch batch, AlchemySubstance incoming, AlchemyContext context) {
        AlchemyProcessHooks.ProcessSettings settings = AlchemyProcessHooks.resolve(context == null ? AlchemyContext.EMPTY : context);
        return merge(batch, incoming, settings.safeEnergyDelta(), settings.explosionEnergyDelta());
    }

    public static MergeResult merge(AlchemyBatch batch, AlchemySubstance incoming, double safeDelta, double explosionDelta) {
        AlchemyBatch existing = batch == null ? AlchemyBatch.EMPTY : batch;
        double safe = finiteNonNegative(safeDelta);
        double explosion = Math.max(safe, finiteNonNegative(explosionDelta));

        if (incoming == null || incoming.isEmpty()) {
            return new MergeResult(existing, Outcome.STABLE, 0.0D);
        }
        if (existing.isEmpty()) {
            return new MergeResult(new AlchemyBatch(incoming, 1), Outcome.STABLE, 0.0D);
        }

        AlchemySubstance current = existing.substance();
        LinkedHashMap<Identifier, Double> existingAffinities = new LinkedHashMap<>(current.affinities());
        LinkedHashMap<Identifier, Double> incomingAffinities = new LinkedHashMap<>(incoming.affinities());
        Map<Identifier, Double> originalExisting = Map.copyOf(existingAffinities);
        Map<Identifier, Double> originalIncoming = Map.copyOf(incomingAffinities);

        double energyDelta = applyInteractions(originalIncoming, originalExisting, existingAffinities) + applyInteractions(originalExisting, originalIncoming, incomingAffinities);
        energyDelta = Math.max(0.0D, energyDelta);

        LinkedHashMap<Identifier, Double> affinities = new LinkedHashMap<>(existingAffinities);
        incomingAffinities.forEach((id, amount) -> affinities.merge(id, amount, Double::sum));
        affinities.entrySet().removeIf(entry -> entry.getValue() <= EPSILON);

        LinkedHashMap<Identifier, Double> properties = new LinkedHashMap<>(current.properties());
        incoming.properties().forEach((id, amount) -> properties.merge(id, amount, Double::sum));

        int nextCount = existing.ingredientCount() + 1;
        double amplifier = weightedAverage(current.amplifier(), existing.ingredientCount(), incoming.amplifier(), 1.0D);
        double purity = weightedAverage(current.purity(), existing.ingredientCount(), incoming.purity(), 1.0D);
        double instability = weightedAverage(current.instability(), existing.ingredientCount(), incoming.instability(), 1.0D) + energyDelta / Math.max(1.0D, nextCount);

        Outcome outcome = energyDelta <= safe ? Outcome.STABLE : energyDelta <= explosion ? Outcome.UNSTABLE : Outcome.CATASTROPHIC;
        double severity = severity(energyDelta, safe, explosion, outcome);
        if (outcome == Outcome.UNSTABLE) {
            purity *= 1.0D - 0.15D * severity;
            instability += 0.25D * severity;
        } else if (outcome == Outcome.CATASTROPHIC) {
            purity *= 0.5D;
            instability += 1.0D + severity;
        }

        AlchemySubstance merged = new AlchemySubstance(
                properties,
                affinities,
                Math.min(current.rankTier(), incoming.rankTier()),
                amplifier,
                purity,
                instability
        );

        return new MergeResult(new AlchemyBatch(merged, nextCount), outcome, energyDelta);
    }

    private static double applyInteractions(
            Map<Identifier, Double> sources,
            Map<Identifier, Double> targets,
            Map<Identifier, Double> mutableTargets
    ) {
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

    private static double severity(double energyDelta, double safe, double explosion, Outcome outcome) {
        if (outcome == Outcome.STABLE) {
            return 0.0D;
        }
        if (outcome == Outcome.CATASTROPHIC) {
            return Math.max(1.0D, (energyDelta - explosion) / Math.max(1.0D, explosion));
        }
        double range = Math.max(EPSILON, explosion - safe);
        return Mth.clamp((energyDelta - safe) / range, 0.0D, 1.0D);
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
            batch = batch == null ? AlchemyBatch.EMPTY : batch;
            outcome = outcome == null ? Outcome.STABLE : outcome;
            energyDelta = Double.isFinite(energyDelta) ? Math.max(0.0D, energyDelta) : 0.0D;
        }

        public double severity(double explosionDelta) {
            double threshold = Math.max(EPSILON, finiteNonNegative(explosionDelta));
            return outcome == Outcome.CATASTROPHIC ? Math.max(1.0D, energyDelta / threshold) : 0.0D;
        }
    }
}
