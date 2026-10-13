package net.zic.ascension.worldgen.noise;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.zic.ascension.AscensionCraft;

public final class AscNoises {
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENT_WARP_X = terrainKey("continent_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENT_WARP_Z = terrainKey("continent_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTS = terrainKey("continents");
    public static final ResourceKey<NormalNoise.NoiseParameters> GREAT_VALLEYS = terrainKey("great_valleys");
    public static final ResourceKey<NormalNoise.NoiseParameters> LOCAL_DETAIL = terrainKey("local_detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> MASSIFS = terrainKey("massifs");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_BELTS = terrainKey("mountain_belts");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_FOLDS = terrainKey("mountain_folds");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_RIDGES = terrainKey("mountain_ridges");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_WARP_X = terrainKey("mountain_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> MOUNTAIN_WARP_Z = terrainKey("mountain_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> OROGENY = terrainKey("orogeny");
    public static final ResourceKey<NormalNoise.NoiseParameters> ROLLING_HILLS = terrainKey("rolling_hills");
    public static final ResourceKey<NormalNoise.NoiseParameters> SECONDARY_RIDGES = terrainKey("secondary_ridges");
    public static final ResourceKey<NormalNoise.NoiseParameters> WEATHERING = terrainKey("weathering");

    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_PATCHES = surfaceKey("patches");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_ROCK = surfaceKey("rock");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_DETAIL = surfaceKey("detail");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_SNOW = surfaceKey("snow");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_STRATA = surfaceKey("strata");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_BROAD_STRATA = surfaceKey("broad_strata");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_PROVINCES = surfaceKey("provinces");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_WEATHERING = surfaceKey("weathering");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_SCREE = surfaceKey("scree");

    private AscNoises() {
    }

    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(CONTINENT_WARP_X, new NormalNoise.NoiseParameters(-11, 1.0D, 0.36D));
        context.register(CONTINENT_WARP_Z, new NormalNoise.NoiseParameters(-11, 1.0D, 0.36D));
        context.register(CONTINENTS, new NormalNoise.NoiseParameters(-13, 1.0D, 0.5D, 0.16D));
        context.register(GREAT_VALLEYS, new NormalNoise.NoiseParameters(-9, 1.0D, 0.34D, 0.1D));
        context.register(LOCAL_DETAIL, new NormalNoise.NoiseParameters(-5, 1.0D, 0.3D, 0.08D));
        context.register(MASSIFS, new NormalNoise.NoiseParameters(-9, 1.0D, 0.44D, 0.14D));
        context.register(MOUNTAIN_BELTS, new NormalNoise.NoiseParameters(-11, 1.0D, 0.28D));
        context.register(MOUNTAIN_FOLDS, new NormalNoise.NoiseParameters(-8, 1.0D, 0.40D, 0.12D));
        context.register(MOUNTAIN_RIDGES, new NormalNoise.NoiseParameters(-8, 1.0D, 0.48D, 0.16D));
        context.register(MOUNTAIN_WARP_X, new NormalNoise.NoiseParameters(-10, 1.0D, 0.4D));
        context.register(MOUNTAIN_WARP_Z, new NormalNoise.NoiseParameters(-10, 1.0D, 0.4D));
        context.register(OROGENY, new NormalNoise.NoiseParameters(-12, 1.0D, 0.42D, 0.14D));
        context.register(ROLLING_HILLS, new NormalNoise.NoiseParameters(-8, 1.0D, 0.38D, 0.12D));
        context.register(SECONDARY_RIDGES, new NormalNoise.NoiseParameters(-7, 1.0D, 0.42D, 0.12D));
        context.register(WEATHERING, new NormalNoise.NoiseParameters(-7, 1.0D, 0.36D, 0.12D));

        context.register(SURFACE_PATCHES, new NormalNoise.NoiseParameters(-4, 1.0D, 0.45D));
        context.register(SURFACE_ROCK, new NormalNoise.NoiseParameters(-5, 1.0D, 0.50D, 0.15D));
        context.register(SURFACE_DETAIL, new NormalNoise.NoiseParameters(-2, 1.0D, 0.35D));
        context.register(SURFACE_SNOW, new NormalNoise.NoiseParameters(-5, 1.0D, 0.42D, 0.16D));
        context.register(SURFACE_STRATA, new NormalNoise.NoiseParameters(-6, 1.0D, 0.42D, 0.16D));
        context.register(SURFACE_BROAD_STRATA, new NormalNoise.NoiseParameters(-9, 1.0D, 0.48D, 0.18D));
        context.register(SURFACE_PROVINCES, new NormalNoise.NoiseParameters(-10, 1.0D, 0.44D, 0.14D));
        context.register(SURFACE_WEATHERING, new NormalNoise.NoiseParameters(-9, 1.0D, 0.34D, 0.12D));
        context.register(SURFACE_SCREE, new NormalNoise.NoiseParameters(-7, 1.0D, 0.40D, 0.12D));
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
