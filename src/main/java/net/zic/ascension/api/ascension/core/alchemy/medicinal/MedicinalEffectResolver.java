package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Resolves data-driven effect expressions from the final medicinal mixture without modifying it.
 * The result is a persistent effect profile, not an immediate skill or resource transaction.
 */
public final class MedicinalEffectResolver {
    private MedicinalEffectResolver() {
    }

    public static MedicinalProfile resolve(MedicinalComposition composition, RegistryAccess access) {
        MedicinalComposition source = composition == null ? MedicinalComposition.EMPTY : composition;
        if (source.isEmpty() || access == null) return new MedicinalProfile(source, List.of());
        Registry<MedicinalEffectExpression> registry = MedicinalRegistries.EXPRESSIONS.get(access);
        List<Map.Entry<ResourceKey<MedicinalEffectExpression>, MedicinalEffectExpression>> rules =
                new ArrayList<>(registry.entrySet());
        rules.sort(Comparator.<Map.Entry<ResourceKey<MedicinalEffectExpression>, MedicinalEffectExpression>>comparingInt(
                entry -> entry.getValue().priority()).reversed()
                .thenComparing(entry -> entry.getKey().identifier().toString()));

        List<MedicinalProfile.Effect> effects = new ArrayList<>();
        for (Map.Entry<ResourceKey<MedicinalEffectExpression>, MedicinalEffectExpression> entry : rules) {
            MedicinalEffectExpression rule = entry.getValue();
            if (source.substance().purity() < rule.minimumPurity()
                    || source.substance().instability() > rule.maximumInstability()
                    || source.toxicity() < rule.minimumToxicity()) continue;

            double amount = minimumRatio(rule.requirements().properties(), source.substance().properties());
            amount = Math.min(amount, minimumRatio(rule.requirements().affinities(), source.substance().affinities()));
            amount = Math.min(amount, minimumRatio(rule.requirements().products(), source.products()));
            if (rule.requirements().isEmpty() && rule.minimumToxicity() > 0.0D) {
                amount = source.toxicity() / rule.minimumToxicity();
            }
            if (!Double.isFinite(amount) || amount <= 0.0D) continue;

            double purityFactor = rule.adverse()
                    ? (1.0D + source.toxicity() + source.substance().instability())
                    : source.substance().purity() / (1.0D + source.substance().instability());
            double potency = Mth.clamp(Math.min(amount, 64.0D) * rule.potencyPerUnit()
                    * source.substance().amplifier() * purityFactor, 0.0D, 4096.0D);
            if (potency <= 1.0E-8D) continue;
            effects.add(new MedicinalProfile.Effect(rule.effect(), potency, rule.durationTicks(), rule.adverse()));
            if (effects.size() >= 32) break;
        }
        return new MedicinalProfile(source, effects);
    }

    private static double minimumRatio(Map<Identifier, Double> required, Map<Identifier, Double> available) {
        double ratio = Double.POSITIVE_INFINITY;
        for (Map.Entry<Identifier, Double> entry : required.entrySet()) {
            ratio = Math.min(ratio, available.getOrDefault(entry.getKey(), 0.0D) / entry.getValue());
        }
        return ratio;
    }
}
