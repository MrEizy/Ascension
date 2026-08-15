package net.zic.ascension.worldgen.debug;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * Seeds data-pack density functions with the same world seed/noise state used by world generation
 */
final class RuntimeDensityFunctionMapper implements DensityFunction.Visitor {

    private final RandomState randomState;

    RuntimeDensityFunctionMapper(RandomState randomState) {
        this.randomState = randomState;
    }

    @Override
    public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
        Holder<NormalNoise.NoiseParameters> noiseData = noiseHolder.noiseData();
        var key = noiseData.unwrapKey().orElseThrow(() ->
                new IllegalStateException("Cannot runtime-map an unkeyed worldgen noise: " + noiseData)
        );
        return new DensityFunction.NoiseHolder(noiseData, randomState.getOrCreateNoise(key));
    }

    @Override
    public DensityFunction apply(DensityFunction function) {
        return function;
    }
}
