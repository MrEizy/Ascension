package net.zic.ascension.worldgen.noise;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.zic.ascension.AscensionCraft;

public final class AscNoises {
    public static final ResourceKey<NormalNoise.NoiseParameters> CLIFF_DETAIL = key("cliff_detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENT_WARP_X = key("continent_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENT_WARP_Z = key("continent_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTS = key("continents");
    public static final ResourceKey<NormalNoise.NoiseParameters> EXTREME_PEAKS = key("extreme_peaks");
    public static final ResourceKey<NormalNoise.NoiseParameters> GREAT_VALLEYS = key("great_valleys");
    public static final ResourceKey<NormalNoise.NoiseParameters> HIGHLANDS = key("highlands");
    public static final ResourceKey<NormalNoise.NoiseParameters> LOCAL_DETAIL = key("local_detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> MACRO_REGIONS = key("macro_regions");
    public static final ResourceKey<NormalNoise.NoiseParameters> MASSIFS = key("massifs");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_BELTS = key("mountain_belts");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_RIDGES = key("mountain_ridges");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_WARP_X = key("mountain_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_WARP_Z = key("mountain_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> OROGENY = key("orogeny");
    public static final ResourceKey<NormalNoise.NoiseParameters> OROGENY_WARP_X = key("orogeny_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> OROGENY_WARP_Z = key("orogeny_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> PEAK_MODULATION = key("peak_modulation");
    public static final ResourceKey<NormalNoise.NoiseParameters> PLATEAU_DETAIL = key("plateau_detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> PLATEAUS = key("plateaus");
    public static final ResourceKey<NormalNoise.NoiseParameters> REGION_WARP_X = key("region_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> REGION_WARP_Z = key("region_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> SECONDARY_RIDGES = key("secondary_ridges");

    private AscNoises() {
    }

    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(CLIFF_DETAIL, new NormalNoise.NoiseParameters(-5, 1.0D, 0.5D, 0.18D));
        context.register(CONTINENT_WARP_X, new NormalNoise.NoiseParameters(-10, 1.0D, 0.38D));
        context.register(CONTINENT_WARP_Z, new NormalNoise.NoiseParameters(-10, 1.0D, 0.38D));
        context.register(CONTINENTS, new NormalNoise.NoiseParameters(-13, 1.0D, 0.52D, 0.18D));
        context.register(EXTREME_PEAKS, new NormalNoise.NoiseParameters(-8, 1.0D, 0.24D));
        context.register(GREAT_VALLEYS, new NormalNoise.NoiseParameters(-10, 1.0D, 0.32D));
        context.register(HIGHLANDS, new NormalNoise.NoiseParameters(-10, 1.0D, 0.3D, 0.1D));
        context.register(LOCAL_DETAIL, new NormalNoise.NoiseParameters(-5, 1.0D, 0.3D, 0.1D));
        context.register(MACRO_REGIONS, new NormalNoise.NoiseParameters(-13, 1.0D, 0.32D));
        context.register(MASSIFS, new NormalNoise.NoiseParameters(-9, 1.0D, 0.42D, 0.14D));
        context.register(MOUNTAIN_BELTS, new NormalNoise.NoiseParameters(-11, 1.0D, 0.24D));
        context.register(MOUNTAIN_RIDGES, new NormalNoise.NoiseParameters(-9, 1.0D, 0.52D, 0.18D));
        context.register(MOUNTAIN_WARP_X, new NormalNoise.NoiseParameters(-10, 1.0D, 0.42D));
        context.register(MOUNTAIN_WARP_Z, new NormalNoise.NoiseParameters(-10, 1.0D, 0.42D));
        context.register(OROGENY, new NormalNoise.NoiseParameters(-13, 1.0D, 0.46D, 0.18D));
        context.register(OROGENY_WARP_X, new NormalNoise.NoiseParameters(-11, 1.0D, 0.36D));
        context.register(OROGENY_WARP_Z, new NormalNoise.NoiseParameters(-11, 1.0D, 0.36D));
        context.register(PEAK_MODULATION, new NormalNoise.NoiseParameters(-9, 1.0D, 0.36D, 0.12D));
        context.register(PLATEAU_DETAIL, new NormalNoise.NoiseParameters(-9, 1.0D, 0.28D));
        context.register(PLATEAUS, new NormalNoise.NoiseParameters(-12, 1.0D, 0.28D));
        context.register(REGION_WARP_X, new NormalNoise.NoiseParameters(-10, 1.0D, 0.38D));
        context.register(REGION_WARP_Z, new NormalNoise.NoiseParameters(-10, 1.0D, 0.38D));
        context.register(SECONDARY_RIDGES, new NormalNoise.NoiseParameters(-8, 1.0D, 0.42D, 0.14D));
    }

    private static ResourceKey<NormalNoise.NoiseParameters> key(String name) {
        return ResourceKey.create(Registries.NOISE, AscensionCraft.prefix("terrain/" + name));
    }
}
