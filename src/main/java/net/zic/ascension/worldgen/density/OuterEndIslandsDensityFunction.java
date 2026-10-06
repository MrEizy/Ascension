package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Vanilla's outer end islands without the main island, the empty ring around it, or the overflow that
 * turns everything into void roughly 370k blocks out.
 *
 * <p>Mirrors DensityFunctions.EndIslandDensityFunction step for step (same rounding and float maths), so
 * wherever vanilla still worked the terrain is identical. Coordinates are sampled at
 * (x * xz_scale + offset_x, z * xz_scale + offset_z); an xz_scale of 0.5 makes islands twice as wide.
 *
 * <p>The input must be minecraft:end_islands. It is only used for its island noise, which the world's
 * RandomState seeds when it maps the noise router, so islands follow the world seed.
 */
@NullMarked
public final class OuterEndIslandsDensityFunction implements DensityFunction {

    public static final MapCodec<OuterEndIslandsDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(OuterEndIslandsDensityFunction::input),
                    Codec.INT.optionalFieldOf("offset_x", 0).forGetter(OuterEndIslandsDensityFunction::offsetX),
                    Codec.INT.optionalFieldOf("offset_z", 0).forGetter(OuterEndIslandsDensityFunction::offsetZ),
                    Codec.doubleRange(1.0E-3D, 1000.0D).optionalFieldOf("xz_scale", 1.0D).forGetter(OuterEndIslandsDensityFunction::xzScale)
            ).apply(instance, (input, offsetX, offsetZ, xzScale) -> new OuterEndIslandsDensityFunction(input, offsetX, offsetZ, xzScale, null))
    );

    public static final KeyDispatchDataCodec<OuterEndIslandsDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    private static final float ISLAND_THRESHOLD = -0.9F;
    //vanilla's range: (-100 - 8) / 128 and (80 - 8) / 128
    private static final double MIN_VALUE = -0.84375D;
    private static final double MAX_VALUE = 0.5625D;

    private final DensityFunction input;
    private final int offsetX;
    private final int offsetZ;
    private final double xzScale;
    //null until the RandomState has seeded the input
    private final @Nullable SimplexNoise islandNoise;

    private OuterEndIslandsDensityFunction(DensityFunction input, int offsetX, int offsetZ, double xzScale, @Nullable SimplexNoise islandNoise) {
        this.input = input;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.xzScale = xzScale;
        this.islandNoise = islandNoise;
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

    @Override
    public double compute(FunctionContext context) {
        if (islandNoise == null) {
            return MIN_VALUE;
        }
        int x = (int) Math.floor(context.blockX() * xzScale) + offsetX;
        int z = (int) Math.floor(context.blockZ() * xzScale) + offsetZ;
        return (getHeightValue(islandNoise, x / 8, z / 8) - 8.0D) / 128.0D;
    }

    /**
     * Same as vanilla's getHeightValue, minus the main island term and the "> 4096" exclusion ring,
     * with chunk coordinates kept in longs so nothing overflows.
     */
    private static float getHeightValue(SimplexNoise islandNoise, int sectionX, int sectionZ) {
        int chunkX = sectionX / 2;
        int chunkZ = sectionZ / 2;
        int subSectionX = sectionX % 2;
        int subSectionZ = sectionZ % 2;
        float doffs = -100.0F;

        for (int xo = -12; xo <= 12; xo++) {
            for (int zo = -12; zo <= 12; zo++) {
                long totalChunkX = (long) chunkX + xo;
                long totalChunkZ = (long) chunkZ + zo;
                if (islandNoise.getValue(totalChunkX, totalChunkZ) < ISLAND_THRESHOLD) {
                    float islandSize = (Mth.abs((float) totalChunkX) * 3439.0F + Mth.abs((float) totalChunkZ) * 147.0F) % 13.0F + 9.0F;
                    float xd = subSectionX - xo * 2;
                    float zd = subSectionZ - zo * 2;
                    float newDoffs = 100.0F - Mth.sqrt(xd * xd + zd * zd) * islandSize;
                    newDoffs = Mth.clamp(newDoffs, -100.0F, 80.0F);
                    doffs = Math.max(doffs, newDoffs);
                }
            }
        }

        return doffs;
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
        return visitor.apply(new OuterEndIslandsDensityFunction(mapped, offsetX, offsetZ, xzScale, noise));
    }

    @Override
    public double minValue() {
        return MIN_VALUE;
    }

    @Override
    public double maxValue() {
        return MAX_VALUE;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
