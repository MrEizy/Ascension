package net.zic.ascension.worldgen.noise;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.zic.ascension.AscensionCraft;

public final class AscNoises {
    // Terrain
    public static final ResourceKey<NormalNoise.NoiseParameters> CLIFF_DETAIL = terrainKey("cliff_detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENT_WARP_X = terrainKey("continent_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENT_WARP_Z = terrainKey("continent_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTS = terrainKey("continents");
    public static final ResourceKey<NormalNoise.NoiseParameters> EXTREME_PEAKS = terrainKey("extreme_peaks");
    public static final ResourceKey<NormalNoise.NoiseParameters> GREAT_VALLEYS = terrainKey("great_valleys");
    public static final ResourceKey<NormalNoise.NoiseParameters> HIGHLANDS = terrainKey("highlands");
    public static final ResourceKey<NormalNoise.NoiseParameters> LOCAL_DETAIL = terrainKey("local_detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> MACRO_REGIONS = terrainKey("macro_regions");
    public static final ResourceKey<NormalNoise.NoiseParameters> MASSIFS = terrainKey("massifs");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_BELTS = terrainKey("mountain_belts");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_RIDGES = terrainKey("mountain_ridges");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_WARP_X = terrainKey("mountain_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_WARP_Z = terrainKey("mountain_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> OROGENY = terrainKey("orogeny");
    public static final ResourceKey<NormalNoise.NoiseParameters> OROGENY_WARP_X = terrainKey("orogeny_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> OROGENY_WARP_Z = terrainKey("orogeny_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> PEAK_MODULATION = terrainKey("peak_modulation");
    public static final ResourceKey<NormalNoise.NoiseParameters> PLATEAU_DETAIL = terrainKey("plateau_detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> PLATEAUS = terrainKey("plateaus");
    public static final ResourceKey<NormalNoise.NoiseParameters> REGION_WARP_X = terrainKey("region_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> REGION_WARP_Z = terrainKey("region_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> SECONDARY_RIDGES = terrainKey("secondary_ridges");

    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_PATCHES = surfaceKey("patches");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_ROCK = surfaceKey("rock");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_DETAIL = surfaceKey("detail");

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

        context.register(SURFACE_PATCHES, new NormalNoise.NoiseParameters(-4, 1.0D, 0.45D));
        context.register(SURFACE_ROCK, new NormalNoise.NoiseParameters(-5, 1.0D, 0.50D, 0.15D));
        context.register(SURFACE_DETAIL, new NormalNoise.NoiseParameters(-2, 1.0D, 0.35D));
    }

    private static ResourceKey<NormalNoise.NoiseParameters> terrainKey(String name) {
        return key("terrain/" + name);
    }

    private static ResourceKey<NormalNoise.NoiseParameters> surfaceKey(String name) {
        return key("surface/" + name);
    }

    private static ResourceKey<NormalNoise.NoiseParameters> key(String path) {
        return ResourceKey.create(Registries.NOISE, AscensionCraft.prefix(path));
    }
}
