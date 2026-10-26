package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyBatch;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMergeResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies ordered, reagent-limited medicinal transformations on top of Ascension's path-aware merge.
 * Every insertion is deterministic, consumes its reagents, and records generated medicinal products.
 */
public final class MedicinalReactionResolver {
    private static final double EPSILON = 1.0E-8D;

    private MedicinalReactionResolver() {
    }

    public static Step add(MedicinalComposition current, int ingredientCount, AlchemySubstance incoming,
                           double safeDelta, double explosionDelta, @Nullable RegistryAccess access) {
        MedicinalComposition before = current == null ? MedicinalComposition.EMPTY : current;
        double safe = Double.isFinite(safeDelta) ? Math.max(0.0D, safeDelta) : 3.0D;
        double explosion = Double.isFinite(explosionDelta) ? Math.max(safe, explosionDelta) : Math.max(safe, 8.0D);
        if (incoming == null || incoming.isEmpty()) {
            return new Step(before, ingredientCount, AlchemyMergeResolver.Outcome.STABLE, 0.0D, List.of());
        }
        if (before.isEmpty()) {
            return new Step(MedicinalComposition.of(incoming), 1, AlchemyMergeResolver.Outcome.STABLE, 0.0D, List.of());
        }

        AlchemySubstance previous = before.substance();
        AlchemyMergeResolver.MergeResult merged = AlchemyMergeResolver.merge(
                new AlchemyBatch(previous, Math.max(1, ingredientCount)), incoming, safe, explosion, access
        );

        Map<Identifier, Double> availablePreviousProperties = new LinkedHashMap<>(previous.properties());
        Map<Identifier, Double> availablePreviousAffinities = new LinkedHashMap<>(previous.affinities());
        Map<Identifier, Double> availablePreviousProducts = new LinkedHashMap<>(before.products());
        Map<Identifier, Double> availableIncomingProperties = new LinkedHashMap<>(incoming.properties());
        Map<Identifier, Double> availableIncomingAffinities = new LinkedHashMap<>(incoming.affinities());
        Map<Identifier, Double> availableIncomingProducts = new LinkedHashMap<>();

        Map<Identifier, Double> properties = new LinkedHashMap<>(merged.batch().substance().properties());
        Map<Identifier, Double> affinities = new LinkedHashMap<>(merged.batch().substance().affinities());
        Map<Identifier, Double> products = new LinkedHashMap<>(before.products());
        double purity = merged.batch().substance().purity();
        double instability = merged.batch().substance().instability();
        double toxicity = before.toxicity();
        double energy = merged.energyDelta();
        List<Identifier> matched = new ArrayList<>();

        if (access != null) {
            Registry<MedicinalReaction> rules = MedicinalRegistries.REACTIONS.get(access);
            List<Map.Entry<ResourceKey<MedicinalReaction>, MedicinalReaction>> ordered = new ArrayList<>(rules.entrySet());
            ordered.sort(Comparator.<Map.Entry<ResourceKey<MedicinalReaction>, MedicinalReaction>>comparingInt(
                    entry -> entry.getValue().priority()).reversed()
                    .thenComparing(entry -> entry.getKey().identifier().toString()));

            for (Map.Entry<ResourceKey<MedicinalReaction>, MedicinalReaction> entry : ordered) {
                MedicinalReaction rule = entry.getValue();
                if (!relationshipMatches(access, rule, previous, incoming)) {
                    continue;
                }
                if (!contains(availablePreviousProperties, rule.existing().properties())
                        || !contains(availablePreviousAffinities, rule.existing().affinities())
                        || !contains(availablePreviousProducts, rule.existing().products())
                        || !contains(availableIncomingProperties, rule.incoming().properties())
                        || !contains(availableIncomingAffinities, rule.incoming().affinities())
                        || !contains(availableIncomingProducts, rule.incoming().products())) {
                    continue;
                }

                subtract(availablePreviousProperties, rule.existing().properties());
                subtract(availablePreviousAffinities, rule.existing().affinities());
                subtract(availablePreviousProducts, rule.existing().products());
                subtract(availableIncomingProperties, rule.incoming().properties());
                subtract(availableIncomingAffinities, rule.incoming().affinities());
                subtract(availableIncomingProducts, rule.incoming().products());
                subtract(properties, rule.existing().properties());
                subtract(properties, rule.incoming().properties());
                subtract(affinities, rule.existing().affinities());
                subtract(affinities, rule.incoming().affinities());
                subtract(products, rule.existing().products());
                subtract(products, rule.incoming().products());
                add(properties, rule.output().properties());
                add(affinities, rule.output().affinities());
                add(products, rule.output().products());
                add(products, rule.products());
                purity = Mth.clamp(purity + rule.purityDelta(), 0.0D, 1.0D);
                instability = Mth.clamp(instability + rule.instabilityDelta(), 0.0D, MedicinalComposition.MAX_CONCENTRATION);
                toxicity = Mth.clamp(toxicity + rule.toxicityDelta(), 0.0D, MedicinalComposition.MAX_CONCENTRATION);
                energy = Mth.clamp(energy + rule.energyDelta(), 0.0D, MedicinalComposition.MAX_CONCENTRATION);
                matched.add(entry.getKey().identifier());
                if (matched.size() >= 32) break;
            }
        }

        AlchemySubstance base = merged.batch().substance();
        AlchemySubstance result = new AlchemySubstance(MedicinalComposition.bounded(properties),
                MedicinalComposition.bounded(affinities), base.rankTier(), base.amplifier(), purity, instability);
        MedicinalComposition composition = new MedicinalComposition(result, products, toxicity);
        AlchemyMergeResolver.Outcome outcome = energy > explosion
                ? AlchemyMergeResolver.Outcome.CATASTROPHIC
                : energy > safe ? AlchemyMergeResolver.Outcome.UNSTABLE : merged.outcome();
        return new Step(composition, Math.max(1, ingredientCount) + 1, outcome, energy, List.copyOf(matched));
    }

    private static boolean relationshipMatches(RegistryAccess access, MedicinalReaction rule,
                                               AlchemySubstance previous, AlchemySubstance incoming) {
        if (rule.relationship().isEmpty()) return true;
        MedicinalReaction.Relationship gate = rule.relationship().get();
        if (previous.affinities().getOrDefault(gate.source(), 0.0D) <= EPSILON
                || incoming.affinities().getOrDefault(gate.target(), 0.0D) <= EPSILON) return false;
        PathInteraction interaction = PathInteractionHelper.getInteraction(access, gate.source(), gate.target());
        return interaction != null && interaction.type() == gate.type() && interaction.value() > 0.0D;
    }

    private static boolean contains(Map<Identifier, Double> available, Map<Identifier, Double> needed) {
        for (Map.Entry<Identifier, Double> entry : needed.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0.0D) + EPSILON < entry.getValue()) return false;
        }
        return true;
    }

    private static void subtract(Map<Identifier, Double> target, Map<Identifier, Double> values) {
        values.forEach((id, amount) -> {
            double remaining = target.getOrDefault(id, 0.0D) - amount;
            if (remaining <= EPSILON) target.remove(id);
            else target.put(id, remaining);
        });
    }

    private static void add(Map<Identifier, Double> target, Map<Identifier, Double> values) {
        values.forEach((id, amount) -> target.merge(id, amount, (a, b) -> Math.min(
                MedicinalComposition.MAX_CONCENTRATION, a + b)));
    }

    public record Step(MedicinalComposition composition, int ingredientCount,
                       AlchemyMergeResolver.Outcome outcome, double energyDelta, List<Identifier> reactions) {
        public Step {
            composition = composition == null ? MedicinalComposition.EMPTY : composition;
            ingredientCount = Math.max(0, ingredientCount);
            reactions = reactions == null ? List.of() : List.copyOf(reactions);
        }
    }
}
