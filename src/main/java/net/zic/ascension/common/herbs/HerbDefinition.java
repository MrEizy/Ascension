package net.zic.ascension.common.herbs;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiContainer;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.configuration.biome.BiomeConfiguration;
import net.zic.ascension.configuration.biome.BiomeConfigurations;
import net.zic.ascension.configuration.dimension.DimensionConfiguration;
import net.zic.ascension.configuration.dimension.DimensionConfigurations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Code-defined description for herb species.
 */
public final class HerbDefinition {
    public static final int MAX_GROWTH_STAGES = 16;
    public static final int MAX_AGE_TIERS = 16;

    private final Identifier id;
    private final int growthStages;
    private final float baseGrowthChance;
    private final PlantingType plantingType;
    private final List<AgeThreshold> ageThresholds;
    private final int[] wildAgeWeights;
    private final Predicate<BlockState> naturalSupport;
    private final QiProfile qiProfile;
    private final GrowthModifier growthModifier;
    private final SpawnRule spawnRule;
    private final QualityResolver qualityResolver;
    private final List<HerbEffect> effects;

    private HerbDefinition(Builder builder) {
        this.id = builder.id;
        this.growthStages = builder.growthStages;
        this.baseGrowthChance = builder.baseGrowthChance;
        this.plantingType = builder.plantingType;
        this.ageThresholds = List.copyOf(builder.ageThresholds);
        this.wildAgeWeights = Arrays.copyOf(builder.wildAgeWeights, builder.wildAgeWeights.length);
        this.naturalSupport = builder.naturalSupport;
        this.qiProfile = new QiProfile(
                builder.qiCapacity,
                builder.availableQiFraction,
                Map.copyOf(builder.qiAffinities)
        );
        this.growthModifier = builder.growthModifier;
        this.spawnRule = builder.spawnRule;
        this.qualityResolver = builder.qualityResolver;
        this.effects = List.copyOf(builder.effects);

        if (growthStages < 1 || growthStages > MAX_GROWTH_STAGES) {
            throw new IllegalArgumentException("Herb " + id + " must have between 1 and " + MAX_GROWTH_STAGES + " growth stages");
        }
        if (ageThresholds.isEmpty() || ageThresholds.size() > MAX_AGE_TIERS) {
            throw new IllegalArgumentException("Herb " + id + " must have between 1 and " + MAX_AGE_TIERS + " age tiers");
        }
        if (wildAgeWeights.length != ageThresholds.size()) {
            throw new IllegalArgumentException("Herb " + id + " must provide one wild age weight per age tier");
        }
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public Identifier id() {
        return id;
    }

    public int growthStages() {
        return growthStages;
    }

    public int maxGrowthStage() {
        return growthStages - 1;
    }

    public float baseGrowthChance() {
        return baseGrowthChance;
    }

    public PlantingType plantingType() {
        return plantingType;
    }

    public int maxAgeTier() {
        return ageThresholds.size() - 1;
    }

    public AgeThreshold ageThreshold(int tier) {
        return ageThresholds.get(Mth.clamp(tier, 0, maxAgeTier()));
    }

    public List<AgeThreshold> ageThresholds() {
        return ageThresholds;
    }

    public QiProfile qiProfile() {
        return qiProfile;
    }

    public boolean canWildSurviveOn(BlockState support) {
        return naturalSupport.test(support);
    }

    public double growthMultiplier(ServerLevel level, BlockPos pos, BlockState state) {
        double qiMultiplier = qiProfile.growthMultiplier(level, pos);
        if (qiMultiplier <= 0.0D) {
            return 0.0D;
        }
        return Math.max(0.0D, qiMultiplier * growthModifier.multiplier(level, pos, state));
    }

    public double qiSuitability(ServerLevel level, BlockPos pos) {
        return qiProfile.growthMultiplier(level, pos);
    }

    public boolean canSpawn(WorldGenLevel level, BlockPos pos, RandomSource random) {
        return qiProfile.canWildSpawn(level, pos) && spawnRule.canSpawn(level, pos, random);
    }

    public Quality resolveQuality(ServerLevel level, BlockPos pos, BlockState state, boolean wild) {
        Quality quality = qualityResolver.resolve(level, pos, state, wild);
        return quality == null ? Quality.COMMON : quality;
    }

    public int chooseWildAgeTier(RandomSource random) {
        long total = 0L;
        for (int weight : wildAgeWeights) {
            total += Math.max(0, weight);
        }
        if (total <= 0L) {
            return 0;
        }

        long roll = Math.floorMod(random.nextLong(), total);
        long cursor = 0L;
        for (int i = 0; i < wildAgeWeights.length; i++) {
            cursor += Math.max(0, wildAgeWeights[i]);
            if (roll < cursor) {
                return i;
            }
        }
        return 0;
    }

    public void applyEffects(LivingEntity entity, ItemStack stack, AscensionComponents.HerbData data) {
        for (HerbEffect effect : effects) {
            effect.apply(entity, stack, data);
        }
    }

    public enum PlantingType {
        NONE,
        DIRECT,
        SEED
    }

    public enum Quality {
        POOR,
        COMMON,
        GOOD,
        SUPERIOR,
        PERFECT;

        public static Quality byTier(int tier) {
            Quality[] values = values();
            return values[Mth.clamp(tier, 0, values.length - 1)];
        }
    }

    public record AgeThreshold(int years, int averageRandomTicksToNext) {
        public AgeThreshold {
            if (years < 0) {
                throw new IllegalArgumentException("Herb age cannot be negative");
            }
            if (averageRandomTicksToNext < 0) {
                throw new IllegalArgumentException("averageRandomTicksToNext cannot be negative");
            }
        }

        public boolean canAdvance() {
            return averageRandomTicksToNext > 0;
        }
    }

    public record QiRequirement(double minimum, double ideal) {
        public static final QiRequirement NONE = new QiRequirement(0.0D, 0.0D);

        public QiRequirement {
            if (!Double.isFinite(minimum) || !Double.isFinite(ideal)) {
                throw new IllegalArgumentException("Herb Qi requirements must be finite");
            }
            minimum = Math.max(0.0D, minimum);
            ideal = Math.max(minimum, ideal);
        }

        public boolean isRelevant() {
            return minimum > 0.0D || ideal > 0.0D;
        }

        public boolean meetsMinimum(double value) {
            return !isRelevant() || value >= minimum;
        }

        public double suitability(double value) {
            if (!isRelevant()) {
                return 1.0D;
            }
            if (value < minimum) {
                return 0.0D;
            }
            if (ideal <= minimum) {
                return 1.0D;
            }
            return Mth.clamp(value / ideal, 0.0D, 1.0D);
        }
    }

    public record QiProfile(
            QiRequirement capacity,
            QiRequirement availableFraction,
            Map<Identifier, QiRequirement> affinities
    ) {
        public static final QiProfile NONE = new QiProfile(
                QiRequirement.NONE,
                QiRequirement.NONE,
                Map.of()
        );

        public QiProfile {
            capacity = Objects.requireNonNull(capacity, "capacity");
            availableFraction = Objects.requireNonNull(availableFraction, "availableFraction");
            affinities = Map.copyOf(affinities);
        }

        public double growthMultiplier(ServerLevel level, BlockPos pos) {
            ChunkQiContainer qi = ChunkQiContainer.getContainer(level.getChunk(pos));

            double multiplier = capacity.suitability(qi.getEnergyCap());
            if (multiplier <= 0.0D) {
                return 0.0D;
            }

            multiplier = Math.min(multiplier, availableFraction.suitability(qi.getEnergyFraction()));
            if (multiplier <= 0.0D) {
                return 0.0D;
            }

            for (Map.Entry<Identifier, QiRequirement> entry : affinities.entrySet()) {
                multiplier = Math.min(multiplier, entry.getValue().suitability(qi.getAffinity(entry.getKey())));
                if (multiplier <= 0.0D) {
                    return 0.0D;
                }
            }

            return multiplier;
        }

        public boolean canWildSpawn(WorldGenLevel level, BlockPos pos) {
            WorldgenQi qi = resolveWorldgenQi(level, pos);
            if (!capacity.meetsMinimum(qi.capacity())) {
                return false;
            }

            for (Map.Entry<Identifier, QiRequirement> entry : affinities.entrySet()) {
                if (!entry.getValue().meetsMinimum(qi.affinity(entry.getKey()))) {
                    return false;
                }
            }

            return true;
        }

        private static WorldgenQi resolveWorldgenQi(WorldGenLevel level, BlockPos pos) {
            ChunkQiContainer container = ChunkQiContainer.getContainer(level.getChunk(pos));
            if (container.hasAtmosphericConfiguration()) {
                Map<Identifier, Double> affinities = new LinkedHashMap<>();
                for (Identifier path : container.getAllAffinities()) {
                    affinities.put(path, container.getAffinity(path));
                }
                return new WorldgenQi(container.getEnergyCap(), affinities);
            }

            double capacity = 0.0D;
            Map<Identifier, Double> affinities = new LinkedHashMap<>();

            BiomeConfigurations biomeConfigurations = BiomeConfigurations.getInstance();
            if (biomeConfigurations != null) {
                BiomeConfiguration biome = biomeConfigurations.getConfiguration(level.getBiome(pos));
                if (biome != null) {
                    capacity += biome.energyCap();
                    biome.affinities().forEach((path, value) -> affinities.merge(path, value, Double::sum));
                }
            }

            DimensionConfigurations dimensionConfigurations = DimensionConfigurations.getInstance();
            if (dimensionConfigurations != null) {
                DimensionConfiguration dimension = dimensionConfigurations.getConfiguration(
                        level.getLevel().dimension().identifier()
                );
                if (dimension != null) {
                    capacity += dimension.energyCap();
                    dimension.affinities().forEach((path, value) -> affinities.merge(path, value, Double::sum));
                }
            }

            return new WorldgenQi(capacity, affinities);
        }

        private record WorldgenQi(double capacity, Map<Identifier, Double> affinities) {
            private double affinity(Identifier path) {
                return affinities.getOrDefault(path, 0.0D);
            }
        }
    }

    @FunctionalInterface
    public interface GrowthModifier {
        GrowthModifier NORMAL = (level, pos, state) -> 1.0D;

        double multiplier(ServerLevel level, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface SpawnRule {
        SpawnRule ALWAYS = (level, pos, random) -> true;

        boolean canSpawn(WorldGenLevel level, BlockPos pos, RandomSource random);
    }

    @FunctionalInterface
    public interface QualityResolver {
        QualityResolver COMMON = (level, pos, state, wild) -> Quality.COMMON;

        Quality resolve(ServerLevel level, BlockPos pos, BlockState state, boolean wild);
    }

    @FunctionalInterface
    public interface HerbEffect {
        void apply(LivingEntity entity, ItemStack stack, AscensionComponents.HerbData data);
    }

    public static final class Builder {
        private final Identifier id;
        private int growthStages = 4;
        private float baseGrowthChance = 0.25F;
        private PlantingType plantingType = PlantingType.DIRECT;
        private List<AgeThreshold> ageThresholds = new ArrayList<>(List.of(new AgeThreshold(1, 0)));
        private int[] wildAgeWeights = {1};
        private Predicate<BlockState> naturalSupport = state -> false;
        private QiRequirement qiCapacity = QiRequirement.NONE;
        private QiRequirement availableQiFraction = QiRequirement.NONE;
        private final Map<Identifier, QiRequirement> qiAffinities = new LinkedHashMap<>();
        private GrowthModifier growthModifier = GrowthModifier.NORMAL;
        private SpawnRule spawnRule = SpawnRule.ALWAYS;
        private QualityResolver qualityResolver = QualityResolver.COMMON;
        private final List<HerbEffect> effects = new ArrayList<>();

        private Builder(Identifier id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder growthStages(int growthStages) {
            this.growthStages = growthStages;
            return this;
        }

        public Builder baseGrowthChance(float baseGrowthChance) {
            this.baseGrowthChance = Mth.clamp(baseGrowthChance, 0.0F, 1.0F);
            return this;
        }

        public Builder planting(PlantingType plantingType) {
            this.plantingType = Objects.requireNonNull(plantingType, "plantingType");
            return this;
        }

        public Builder ageThresholds(AgeThreshold... thresholds) {
            this.ageThresholds = new ArrayList<>(List.of(thresholds));
            if (wildAgeWeights.length != this.ageThresholds.size()) {
                this.wildAgeWeights = new int[this.ageThresholds.size()];
                Arrays.fill(this.wildAgeWeights, 1);
            }
            return this;
        }

        public Builder wildAgeWeights(int... weights) {
            this.wildAgeWeights = Arrays.copyOf(weights, weights.length);
            return this;
        }

        public Builder naturalSupport(Predicate<BlockState> naturalSupport) {
            this.naturalSupport = Objects.requireNonNull(naturalSupport, "naturalSupport");
            return this;
        }

        public Builder qiCapacity(double minimum, double ideal) {
            this.qiCapacity = new QiRequirement(minimum, ideal);
            return this;
        }

        public Builder availableQiFraction(double minimum, double ideal) {
            this.availableQiFraction = new QiRequirement(
                    Mth.clamp(minimum, 0.0D, 1.0D),
                    Mth.clamp(ideal, 0.0D, 1.0D)
            );
            return this;
        }

        public Builder qiAffinity(Identifier path, double minimum, double ideal) {
            this.qiAffinities.put(
                    Objects.requireNonNull(path, "path"),
                    new QiRequirement(minimum, ideal)
            );
            return this;
        }

        public Builder growthModifier(GrowthModifier growthModifier) {
            this.growthModifier = Objects.requireNonNull(growthModifier, "growthModifier");
            return this;
        }

        public Builder spawnRule(SpawnRule spawnRule) {
            this.spawnRule = Objects.requireNonNull(spawnRule, "spawnRule");
            return this;
        }

        public Builder quality(QualityResolver qualityResolver) {
            this.qualityResolver = Objects.requireNonNull(qualityResolver, "qualityResolver");
            return this;
        }

        public Builder effect(HerbEffect effect) {
            this.effects.add(Objects.requireNonNull(effect, "effect"));
            return this;
        }

        public HerbDefinition build() {
            return new HerbDefinition(this);
        }
    }
}
