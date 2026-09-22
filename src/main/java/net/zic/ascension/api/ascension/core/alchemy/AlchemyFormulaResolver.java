package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class AlchemyFormulaResolver {
    private static final int MAX_PASSES = 64;

    private AlchemyFormulaResolver() {
    }

    public static CondensationResult condense(AlchemyBatch batch, AlchemyContext context) {
        AlchemyBatch remaining = batch == null ? AlchemyBatch.EMPTY : batch;
        if (remaining.isEmpty() || context == null || context.level() == null) {
            return new CondensationResult(List.of(), remaining, List.of());
        }

        Registry<AlchemyFormula> registry = AlchemyFormulas.REGISTRY.get(context.level().registryAccess());
        List<ItemStack> outputs = new ArrayList<>();
        List<Identifier> matchedFormulas = new ArrayList<>();

        for (int pass = 0; pass < MAX_PASSES && !remaining.isEmpty(); pass++) {
            Candidate candidate = bestCandidate(registry, remaining.substance());
            if (candidate == null) {
                break;
            }

            AlchemyFormula formula = candidate.formula();
            Item item = BuiltInRegistries.ITEM.getValue(formula.output());
            if (item == null || item == Items.AIR) {
                break;
            }

            int yield = formula.yield(remaining.substance());
            AscensionComponents.PillData pillData = createPillData(remaining.substance(), candidate.quality());
            addOutputs(outputs, item, yield, pillData);
            matchedFormulas.add(candidate.id());

            AlchemySubstance consumed = formula.consume(remaining.substance());
            if (consumed.equals(remaining.substance())) {
                break;
            }
            remaining = consumed.isEmpty()
                    ? AlchemyBatch.EMPTY
                    : new AlchemyBatch(consumed, remaining.ingredientCount());
        }

        return new CondensationResult(List.copyOf(outputs), remaining, List.copyOf(matchedFormulas));
    }

    private static Candidate bestCandidate(Registry<AlchemyFormula> registry, AlchemySubstance substance) {
        return registry.entrySet().stream()
                .map(entry -> candidate(entry, substance))
                .filter(candidate -> candidate != null)
                .max(Comparator.comparingDouble(Candidate::quality)
                        .thenComparingInt(candidate -> candidate.formula().priority())
                        .thenComparing(candidate -> candidate.id().toString(), Comparator.reverseOrder()))
                .orElse(null);
    }

    private static Candidate candidate(Map.Entry<ResourceKey<AlchemyFormula>, AlchemyFormula> entry, AlchemySubstance substance) {
        AlchemyFormula formula = entry.getValue();
        if (formula == null || !formula.matches(substance)) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.getValue(formula.output());
        if (item == null || item == Items.AIR) {
            return null;
        }
        return new Candidate(entry.getKey().identifier(), formula, formula.matchQuality(substance));
    }

    private static void addOutputs(
            List<ItemStack> outputs,
            Item item,
            int count,
            AscensionComponents.PillData pillData
    ) {
        int remaining = Math.max(1, count);
        while (remaining > 0) {
            ItemStack stack = new ItemStack(item);
            int stackCount = Math.min(remaining, stack.getMaxStackSize());
            stack.setCount(stackCount);
            stack.set(AscensionComponents.PILL_DATA.get(), pillData);
            outputs.add(stack);
            remaining -= stackCount;
        }
    }

    private static AscensionComponents.PillData createPillData(AlchemySubstance substance, double matchQuality) {
        int purity = Math.max(0, Math.min(100, (int) Math.round(matchQuality * 100.0D)));
        return new AscensionComponents.PillData(
                AscensionComponents.PillData.rankForTier(substance.rankTier()),
                purity,
                substance.amplifier()
        );
    }

    private record Candidate(Identifier id, AlchemyFormula formula, double quality) {
    }

    public record CondensationResult(
            List<ItemStack> outputs,
            AlchemyBatch remainder,
            List<Identifier> matchedFormulas
    ) {
        public CondensationResult {
            outputs = outputs == null ? List.of() : List.copyOf(outputs);
            remainder = remainder == null ? AlchemyBatch.EMPTY : remainder;
            matchedFormulas = matchedFormulas == null ? List.of() : List.copyOf(matchedFormulas);
        }

        public boolean success() {
            return !outputs.isEmpty();
        }

        public boolean hasRemainder() {
            return !remainder.isEmpty();
        }

        public int outputCount() {
            return outputs.stream().mapToInt(ItemStack::getCount).sum();
        }
    }
}
