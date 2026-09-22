package net.zic.ascension.impl.datapack.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyEssence;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterial;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterialProvider;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.item.herbs.HerbItem;

import java.util.List;
import java.util.Map;

public final class AlchemyMaterialProviders {
    private static final ExtensionTypeRegistry<AlchemyMaterialProvider> TYPES = new ExtensionTypeRegistry<>(
            AlchemyMaterials.PROVIDER_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<AlchemyMaterialProvider>, CodecType<AlchemyMaterialProvider>> FIXED =
            TYPES.add("fixed", Fixed.CODEC);
    public static final DeferredHolder<CodecType<AlchemyMaterialProvider>, CodecType<AlchemyMaterialProvider>> HERB =
            TYPES.add("herb", Herb.CODEC);

    private AlchemyMaterialProviders() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }

    public record Fixed(AlchemyMaterial material) implements AlchemyMaterialProvider {
        public static final MapCodec<Fixed> CODEC = AlchemyMaterial.CODEC.fieldOf("material").xmap(Fixed::new, Fixed::material);

        public Fixed {
            material = material == null ? AlchemyMaterial.EMPTY : material;
        }

        @Override
        public CodecType<AlchemyMaterialProvider> getType() {
            return FIXED.get();
        }

        @Override
        public AlchemyMaterial resolve(ItemStack stack, AlchemyContext context) {
            return material;
        }
    }

    public record Herb(
            Map<Identifier, Double> properties,
            Map<Identifier, Double> affinities,
            List<Integer> rankTiers,
            double amplifier,
            double purity,
            double instability,
            double refinementDifficulty
    ) implements AlchemyMaterialProvider {
        public static final MapCodec<Herb> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("properties", Map.of()).forGetter(Herb::properties),
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).forGetter(Herb::affinities),
                Codec.intRange(0, 5).listOf().optionalFieldOf("rank_tiers", List.of()).forGetter(Herb::rankTiers),
                Codec.DOUBLE.optionalFieldOf("amplifier", 1.0D).forGetter(Herb::amplifier),
                Codec.DOUBLE.optionalFieldOf("purity", 1.0D).forGetter(Herb::purity),
                Codec.DOUBLE.optionalFieldOf("instability", 0.0D).forGetter(Herb::instability),
                Codec.DOUBLE.optionalFieldOf("refinement_difficulty", 1.0D).forGetter(Herb::refinementDifficulty)
        ).apply(instance, Herb::new));

        public Herb {
            properties = properties == null ? Map.of() : Map.copyOf(properties);
            affinities = affinities == null ? Map.of() : Map.copyOf(affinities);
            rankTiers = rankTiers == null ? List.of() : List.copyOf(rankTiers);
            amplifier = finiteNonNegative(amplifier, 1.0D);
            purity = Mth.clamp(Double.isFinite(purity) ? purity : 1.0D, 0.0D, 1.0D);
            instability = finiteNonNegative(instability, 0.0D);
            refinementDifficulty = finiteNonNegative(refinementDifficulty, 1.0D);
        }

        @Override
        public CodecType<AlchemyMaterialProvider> getType() {
            return HERB.get();
        }

        @Override
        public AlchemyMaterial resolve(ItemStack stack, AlchemyContext context) {
            if (!(stack.getItem() instanceof HerbItem herbItem)) {
                return AlchemyMaterial.EMPTY;
            }

            AscensionComponents.HerbData data = herbItem.data(stack);
            HerbDefinition.AgeThreshold age = herbItem.definition().ageThreshold(data.ageTier());
            double qualityAmplifier = qualityAmplifier(data.qualityTier());
            double resolvedAmplifier = amplifier * qualityAmplifier;
            double resolvedPurity = Mth.clamp(purity * qualityAmplifier, 0.0D, 1.0D);
            double resolvedInstability = instability * qualityInstability(data.qualityTier());

            return new AlchemyMaterial(
                    new AlchemyEssence(properties, affinities, rankTier(data.ageTier(), age.years()), resolvedAmplifier),
                    resolvedPurity,
                    resolvedInstability,
                    refinementDifficulty
            );
        }

        private int rankTier(int ageTier, int years) {
            int tier = Math.max(0, ageTier);
            if (tier < rankTiers.size()) {
                return rankTiers.get(tier);
            }
            if (years >= 1_000_000) return 5;
            if (years >= 100_000) return 4;
            if (years >= 10_000) return 3;
            if (years >= 1_000) return 2;
            if (years >= 100) return 1;
            return 0;
        }

        private static double qualityAmplifier(int tier) {
            return switch (Mth.clamp(tier, 0, HerbDefinition.Quality.values().length - 1)) {
                case 0 -> 0.75D;
                case 2 -> 1.04D;
                case 3 -> 1.08D;
                case 4 -> 1.12D;
                default -> 1.0D;
            };
        }

        private static double qualityInstability(int tier) {
            return switch (Mth.clamp(tier, 0, HerbDefinition.Quality.values().length - 1)) {
                case 0 -> 1.25D;
                case 2 -> 0.90D;
                case 3 -> 0.80D;
                case 4 -> 0.65D;
                default -> 1.0D;
            };
        }

        private static double finiteNonNegative(double value, double fallback) {
            return Double.isFinite(value) ? Math.max(0.0D, value) : fallback;
        }
    }
}
