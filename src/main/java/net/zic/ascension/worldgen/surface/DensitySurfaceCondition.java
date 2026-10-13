package net.zic.ascension.worldgen.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public record DensitySurfaceCondition(
        DensityFunction density,
        double minInclusive,
        double maxExclusive
) implements SurfaceRules.ConditionSource {
    public static final MapCodec<DensitySurfaceCondition> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("density").forGetter(DensitySurfaceCondition::density),
                    Codec.DOUBLE.fieldOf("min").forGetter(DensitySurfaceCondition::minInclusive),
                    Codec.DOUBLE.fieldOf("max").forGetter(DensitySurfaceCondition::maxExclusive)
            ).apply(instance, DensitySurfaceCondition::new)
    );

    public static final KeyDispatchDataCodec<DensitySurfaceCondition> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    private static final Map<RandomState, ConcurrentHashMap<DensityFunction, DensityFunction>> RUNTIME_CACHE = new WeakHashMap<>();
    private static final ThreadLocal<ColumnSampleCache> COLUMN_CACHE = ThreadLocal.withInitial(ColumnSampleCache::new);

    public DensitySurfaceCondition {
        if (minInclusive >= maxExclusive) {
            throw new IllegalArgumentException("Density surface condition minimum must be lower than maximum");
        }
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        DensityFunction runtimeDensity = runtimeDensity(context.randomState, density);
        return () -> {
            long xz = packXZ(context.blockX, context.blockZ);
            double value = COLUMN_CACHE.get().sample(
                    context.randomState,
                    xz,
                    density,
                    runtimeDensity,
                    context.blockX,
                    context.blockZ
            );
            return value >= minInclusive && value < maxExclusive;
        };
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    private static DensityFunction runtimeDensity(RandomState randomState, DensityFunction density) {
        ConcurrentHashMap<DensityFunction, DensityFunction> cache;
        synchronized (RUNTIME_CACHE) {
            cache = RUNTIME_CACHE.computeIfAbsent(randomState, ignored -> new ConcurrentHashMap<>());
        }
        return cache.computeIfAbsent(density, function -> function.mapAll(new RuntimeMapper(randomState)));
    }

    private static long packXZ(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }


    private static final class ColumnSampleCache {
        private final Map<DensityFunction, Double> samples = new HashMap<>();
        private WeakReference<RandomState> randomState;
        private long xz;
        private boolean initialized;

        private double sample(
                RandomState randomState,
                long xz,
                DensityFunction density,
                DensityFunction runtimeDensity,
                int x,
                int z
        ) {
            if (!initialized || this.randomState == null || this.randomState.get() != randomState || this.xz != xz) {
                this.randomState = new WeakReference<>(randomState);
                this.xz = xz;
                this.initialized = true;
                this.samples.clear();
            }
            return samples.computeIfAbsent(
                    density,
                    ignored -> runtimeDensity.compute(new DensityFunction.SinglePointContext(x, 64, z))
            );
        }
    }

    private static final class RuntimeMapper implements DensityFunction.Visitor {
        private final RandomState randomState;

        private RuntimeMapper(RandomState randomState) {
            this.randomState = randomState;
        }

        @Override
        public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
            Holder<NormalNoise.NoiseParameters> noiseData = noiseHolder.noiseData();
            var key = noiseData.unwrapKey().orElseThrow(() ->
                    new IllegalStateException("Cannot runtime-map an unkeyed surface density noise: " + noiseData)
            );
            return new DensityFunction.NoiseHolder(noiseData, randomState.getOrCreateNoise(key));
        }

        @Override
        public DensityFunction apply(DensityFunction function) {
            if (function instanceof DensityFunctions.HolderHolder holder) {
                return holder.function().value().mapAll(this);
            }
            return function;
        }
    }
}
