package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Extra terrain height (in blocks) for hills and mountains on the outer end islands.
 *
 * <p>Uses the same island layout as ascension:outer_end_islands (offset_x, offset_z and xz_scale must match),
 * so every island centre is known. Each island rolls a tier from its position, and gets a smooth dome centred
 * on it that fades out before the island edge, so peaks never overhang the void.
 * Larger islands get proportionally taller domes to keep slopes gentle.
 *
 * <p>Output is 2D (ignores y); wrap it in minecraft:cache_2d.
 */
@NullMarked
public final class IslandPeaksDensityFunction implements DensityFunction {

    public enum Shape implements StringRepresentable {
        /** One round dome centred on the island. */
        DOME("dome"),
        /** An elongated ridge across the island with several summits along it. */
        RANGE("range");

        public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);
        private final String name;

        Shape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /**
     * @param maxIslandSize islands with a larger islandSize (9 = biggest, 22 = smallest) fall through to the next tier,
     *                      so tall shapes only appear on islands wide enough for them
     */
    public record Tier(double chance, double height, Shape shape, double maxIslandSize) {
        public static final Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0.0D, 1.0D).fieldOf("chance").forGetter(Tier::chance),
                Codec.doubleRange(0.0D, 1000.0D).fieldOf("height").forGetter(Tier::height),
                Shape.CODEC.optionalFieldOf("shape", Shape.DOME).forGetter(Tier::shape),
                Codec.doubleRange(9.0D, 22.0D).optionalFieldOf("max_island_size", 22.0D).forGetter(Tier::maxIslandSize)
        ).apply(instance, Tier::new));
    }

    /** Some islands get their summit capped into a plateau at a random fraction of their full height. */
    public record FlatTops(double chance, double minLevel, double maxLevel) {
        public static final FlatTops NONE = new FlatTops(0.0D, 1.0D, 1.0D);
        public static final Codec<FlatTops> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0.0D, 1.0D).fieldOf("chance").forGetter(FlatTops::chance),
                Codec.doubleRange(0.05D, 1.0D).fieldOf("min_level").forGetter(FlatTops::minLevel),
                Codec.doubleRange(0.05D, 1.0D).fieldOf("max_level").forGetter(FlatTops::maxLevel)
        ).apply(instance, FlatTops::new));
    }

    public static final MapCodec<IslandPeaksDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(IslandPeaksDensityFunction::input),
                    Codec.INT.optionalFieldOf("offset_x", 0).forGetter(IslandPeaksDensityFunction::offsetX),
                    Codec.INT.optionalFieldOf("offset_z", 0).forGetter(IslandPeaksDensityFunction::offsetZ),
                    Codec.doubleRange(1.0E-3D, 1000.0D).optionalFieldOf("xz_scale", 1.0D).forGetter(IslandPeaksDensityFunction::xzScale),
                    Tier.CODEC.listOf().fieldOf("tiers").forGetter(IslandPeaksDensityFunction::tiers),
                    Codec.doubleRange(0.0D, 0.99D).optionalFieldOf("edge_margin", 0.3D).forGetter(IslandPeaksDensityFunction::edgeMargin),
                    Codec.doubleRange(0.1D, 10.0D).optionalFieldOf("profile_exponent", 1.0D).forGetter(IslandPeaksDensityFunction::profileExponent),
                    FlatTops.CODEC.optionalFieldOf("flat_tops", FlatTops.NONE).forGetter(IslandPeaksDensityFunction::flatTops),
                    DensityFunction.HOLDER_HELPER_CODEC.optionalFieldOf("modifier", DensityFunctions.constant(1.0D)).forGetter(IslandPeaksDensityFunction::modifier)
            ).apply(instance, (input, offsetX, offsetZ, xzScale, tiers, edgeMargin, profileExponent, flatTops, modifier) ->
                    new IslandPeaksDensityFunction(input, offsetX, offsetZ, xzScale, tiers, edgeMargin, profileExponent, flatTops, modifier, null))
    );

    public static final KeyDispatchDataCodec<IslandPeaksDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    private static final float ISLAND_THRESHOLD = -0.9F;
    //islandSize ranges 9..22; the biggest islands (9) get full height
    private static final float LARGEST_ISLAND_SIZE = 9.0F;
    //how gradually a capped dome rounds into its plateau (in dome units, 0..1)
    private static final double PLATEAU_SOFTNESS = 0.12D;
    //separate random streams per island, so tier and plateau rolls are independent
    private static final long TIER_STREAM = 0L;
    private static final long FLAT_CHANCE_STREAM = 0x5DEECE66DL;
    private static final long FLAT_LEVEL_STREAM = 0x2545F4914F6CDD1DL;
    private static final long RANGE_ANGLE_STREAM = 0x632BE59BD9B4E019L;
    private static final long RANGE_PHASE_STREAM = 0x85EBCA77C2B2AE63L;
    //a range is this wide relative to its length
    private static final double RANGE_WIDTH = 0.6D;
    //number of summit half-waves along a range, and how deep the saddles between them dip
    private static final double RANGE_SUMMITS = 3.0D;
    private static final double RANGE_SADDLE_DEPTH = 0.3D;

    private final DensityFunction input;
    private final int offsetX;
    private final int offsetZ;
    private final double xzScale;
    /** Checked in order: an island takes the first tier whose cumulative chance covers its roll. */
    private final List<Tier> tiers;
    /** Fraction of the island radius (from the edge inwards) kept flat. */
    private final double edgeMargin;
    /** Raises the dome curve to this power; above 1 gives a wider, flatter base and a narrower summit. */
    private final double profileExponent;
    private final FlatTops flatTops;
    /** Multiplies the dome before plateaus are capped (e.g. ridge noise), so plateaus stay level. */
    private final DensityFunction modifier;
    private final double maxHeight;
    private final @Nullable SimplexNoise islandNoise;
    private final long salt;

    private IslandPeaksDensityFunction(DensityFunction input, int offsetX, int offsetZ, double xzScale, List<Tier> tiers,
                                       double edgeMargin, double profileExponent, FlatTops flatTops,
                                       DensityFunction modifier, @Nullable SimplexNoise islandNoise) {
        this.input = input;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.xzScale = xzScale;
        this.tiers = List.copyOf(tiers);
        this.edgeMargin = edgeMargin;
        this.profileExponent = profileExponent;
        this.flatTops = flatTops;
        this.modifier = modifier;
        this.maxHeight = this.tiers.stream().mapToDouble(Tier::height).max().orElse(0.0D) * Math.max(1.0D, modifier.maxValue());
        this.islandNoise = islandNoise;
        //ties tier rolls to the world seed through the seeded noise
        this.salt = islandNoise == null ? 0L : Double.doubleToLongBits(islandNoise.getValue(0.5D, 0.5D));
    }

    public DensityFunction input() {
        return input;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public double xzScale() {
        return xzScale;
    }

    public List<Tier> tiers() {
        return tiers;
    }

    public double edgeMargin() {
        return edgeMargin;
    }

    public double profileExponent() {
        return profileExponent;
    }

    public FlatTops flatTops() {
        return flatTops;
    }

    public DensityFunction modifier() {
        return modifier;
    }

    @Override
    public double compute(FunctionContext context) {
        if (islandNoise == null || maxHeight <= 0.0D) {
            return 0.0D;
        }
        //exact (not 8-block snapped) position in island sections, so domes rise smoothly instead of in steps
        double sectionX = (context.blockX() * xzScale + offsetX) / 8.0D;
        double sectionZ = (context.blockZ() * xzScale + offsetZ) / 8.0D;
        long chunkX = (long) Math.floor(sectionX / 2.0D);
        long chunkZ = (long) Math.floor(sectionZ / 2.0D);

        double shapeModifier = Math.max(0.0D, modifier.compute(context));
        double height = 0.0D;
        for (int xo = -12; xo <= 12; xo++) {
            for (int zo = -12; zo <= 12; zo++) {
                long totalChunkX = chunkX + xo;
                long totalChunkZ = chunkZ + zo;
                if (islandNoise.getValue(totalChunkX, totalChunkZ) >= ISLAND_THRESHOLD) {
                    continue;
                }
                //same island size and distance as the island shape itself
                float islandSize = (Mth.abs((float) totalChunkX) * 3439.0F + Mth.abs((float) totalChunkZ) * 147.0F) % 13.0F + 9.0F;
                Tier tier = tierFor(totalChunkX, totalChunkZ, islandSize);
                if (tier == null || tier.height() <= 0.0D) {
                    continue;
                }
                //island centres sit at section (2 * chunk), as in vanilla's island shape
                double xd = sectionX - totalChunkX * 2.0D;
                double zd = sectionZ - totalChunkZ * 2.0D;
                double distance;
                double summits = 1.0D;
                if (tier.shape() == Shape.RANGE) {
                    //distance in an ellipse stretched along a random direction; the long axis matches a dome, so it stays as far from the edge
                    double angle = hash01(totalChunkX, totalChunkZ, RANGE_ANGLE_STREAM) * Math.PI * 2.0D;
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double along = xd * cos + zd * sin;
                    double across = -xd * sin + zd * cos;
                    distance = Math.sqrt(along * along + (across / RANGE_WIDTH) * (across / RANGE_WIDTH));
                    //-1..1 along the usable length of the ridge
                    double position = along * islandSize / 100.0D / (1.0D - edgeMargin);
                    double phase = hash01(totalChunkX, totalChunkZ, RANGE_PHASE_STREAM) * Math.PI * 2.0D;
                    double wave = 0.5D + 0.5D * Math.cos(Math.PI * RANGE_SUMMITS * position + phase);
                    summits = 1.0D - RANGE_SADDLE_DEPTH * (1.0D - wave);
                } else {
                    distance = Math.sqrt(xd * xd + zd * zd);
                }
                //1 at the island centre, 0 where vanilla's island falloff reaches 100 (beyond the visible edge)
                double centrality = 1.0D - distance * islandSize / 100.0D;
                if (centrality <= edgeMargin) {
                    continue;
                }
                double t = (centrality - edgeMargin) / (1.0D - edgeMargin);
                double dome = Math.pow(t * t * (3.0D - 2.0D * t), profileExponent) * summits * shapeModifier;
                if (flatTops.chance() > 0.0D && hash01(totalChunkX, totalChunkZ, FLAT_CHANCE_STREAM) < flatTops.chance()) {
                    double level = Mth.lerp(hash01(totalChunkX, totalChunkZ, FLAT_LEVEL_STREAM), flatTops.minLevel(), flatTops.maxLevel());
                    dome = smoothMin(dome, level, PLATEAU_SOFTNESS);
                }
                height = Math.max(height, tier.height() * dome * (LARGEST_ISLAND_SIZE / islandSize));
            }
        }
        return height;
    }

    private @Nullable Tier tierFor(long islandX, long islandZ, float islandSize) {
        double roll = hash01(islandX, islandZ, TIER_STREAM);
        double cumulative = 0.0D;
        for (int i = 0; i < tiers.size(); i++) {
            cumulative += tiers.get(i).chance();
            if (roll < cumulative) {
                //islands too small for the rolled tier fall through to the following ones
                for (int j = i; j < tiers.size(); j++) {
                    if (islandSize <= tiers.get(j).maxIslandSize()) {
                        return tiers.get(j);
                    }
                }
                return null;
            }
        }
        return null;
    }

    /** Polynomial smooth minimum: like min(a, b) but rounded over a band of width k around where they meet. */
    @SuppressWarnings("SameParameterValue")
    private static double smoothMin(double a, double b, double k) {
        double h = Math.max(k - Math.abs(a - b), 0.0D) / k;
        return Math.min(a, b) - h * h * k * 0.25D;
    }

    private double hash01(long x, long z, long stream) {
        long h = x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ salt ^ stream;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(values, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        //the visitor swaps the input for a seeded end_islands; keep its noise
        DensityFunction mapped = input.mapAll(visitor);
        SimplexNoise noise = mapped instanceof DensityFunctions.EndIslandDensityFunction islands ? islands.islandNoise : null;
        return visitor.apply(new IslandPeaksDensityFunction(mapped, offsetX, offsetZ, xzScale, tiers, edgeMargin, profileExponent, flatTops,
                modifier.mapAll(visitor), noise));
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return maxHeight;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
