package net.zic.ascension.worldgen.density;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.noise.AscNoises;

import java.util.List;

import net.zic.ascension.worldgen.density.HermiteSplineDensityFunction.Point;

public final class AscDensityFunctions {
    public static final ResourceKey<DensityFunction> BASE_HEIGHT = key("base_height");
    public static final ResourceKey<DensityFunction> BIOME_CONTINENTALNESS = key("biome_continentalness");
    public static final ResourceKey<DensityFunction> BIOME_DEPTH = key("biome_depth");
    public static final ResourceKey<DensityFunction> BIOME_EROSION = key("biome_erosion");
    public static final ResourceKey<DensityFunction> BIOME_EROSION_BASE = key("biome_erosion_base");
    public static final ResourceKey<DensityFunction> BIOME_EROSION_SURFACE = key("biome_erosion_surface");
    public static final ResourceKey<DensityFunction> BIOME_HUMIDITY = key("biome_humidity");
    public static final ResourceKey<DensityFunction> BIOME_SURFACE_DEPTH = key("biome_surface_depth");
    public static final ResourceKey<DensityFunction> BIOME_SURFACE_PROXIMITY = key("biome_surface_proximity");
    public static final ResourceKey<DensityFunction> BIOME_TEMPERATURE = key("biome_temperature");
    public static final ResourceKey<DensityFunction> BIOME_WEIRDNESS = key("biome_weirdness");
    public static final ResourceKey<DensityFunction> BIOME_WEIRDNESS_SURFACE = key("biome_weirdness_surface");
    public static final ResourceKey<DensityFunction> CLIFF_DETAIL = key("cliff_detail");
    public static final ResourceKey<DensityFunction> CLIFF_MASK = key("cliff_mask");
    public static final ResourceKey<DensityFunction> CONTINENT_WARP_X = key("continent_warp_x");
    public static final ResourceKey<DensityFunction> CONTINENT_WARP_Z = key("continent_warp_z");
    public static final ResourceKey<DensityFunction> CONTINENTAL_TARGET_Y = key("continental_target_y");
    public static final ResourceKey<DensityFunction> CONTINENTS = key("continents");
    public static final ResourceKey<DensityFunction> CORE_MASK = key("core_mask");
    public static final ResourceKey<DensityFunction> CORE_PROFILE = key("core_profile");
    public static final ResourceKey<DensityFunction> CORE_TARGET_Y = key("core_target_y");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY = key("final_density");
    public static final ResourceKey<DensityFunction> FOOTHILL_MASK = key("foothill_mask");
    public static final ResourceKey<DensityFunction> FOOTHILL_PROFILE = key("foothill_profile");
    public static final ResourceKey<DensityFunction> FOOTHILL_TARGET_Y = key("foothill_target_y");
    public static final ResourceKey<DensityFunction> HERO_GATE = key("hero_gate");
    public static final ResourceKey<DensityFunction> HERO_SCORE = key("hero_score");
    public static final ResourceKey<DensityFunction> HERO_TARGET_Y = key("hero_target_y");
    public static final ResourceKey<DensityFunction> INITIAL_DENSITY_WITHOUT_JAGGEDNESS = key("initial_density_without_jaggedness");
    public static final ResourceKey<DensityFunction> LAND_MASK = key("land_mask");
    public static final ResourceKey<DensityFunction> LOCAL_DETAIL_NOISE = key("local_detail_noise");
    public static final ResourceKey<DensityFunction> LOWLAND_TARGET_Y = key("lowland_target_y");
    public static final ResourceKey<DensityFunction> MACRO_REGIONS = key("macro_regions");
    public static final ResourceKey<DensityFunction> MASSIF_BOOST_RAW_Y = key("massif_boost_raw_y");
    public static final ResourceKey<DensityFunction> MASSIF_BOOST_Y = key("massif_boost_y");
    public static final ResourceKey<DensityFunction> MASSIF_NOISE = key("massif_noise");
    public static final ResourceKey<DensityFunction> MOUNTAIN_AXIS = key("mountain_axis");
    public static final ResourceKey<DensityFunction> MOUNTAIN_AXIS_ABS = key("mountain_axis_abs");
    public static final ResourceKey<DensityFunction> MOUNTAIN_ERODED_Y = key("mountain_eroded_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_LAND_GATE = key("mountain_land_gate");
    public static final ResourceKey<DensityFunction> MOUNTAIN_PROVINCE = key("mountain_province");
    public static final ResourceKey<DensityFunction> MOUNTAIN_TARGET_BASE_Y = key("mountain_target_base_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_TARGET_RAW_Y = key("mountain_target_raw_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_TARGET_UNCARVED_Y = key("mountain_target_uncarved_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_TARGET_Y = key("mountain_target_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_WARP_X = key("mountain_warp_x");
    public static final ResourceKey<DensityFunction> MOUNTAIN_WARP_Z = key("mountain_warp_z");
    public static final ResourceKey<DensityFunction> MOUNTAIN_WEATHERING_NOISE = key("mountain_weathering_noise");
    public static final ResourceKey<DensityFunction> NON_MOUNTAIN = key("non_mountain");
    public static final ResourceKey<DensityFunction> OROGENY = key("orogeny");
    public static final ResourceKey<DensityFunction> OROGENY_WARP_X = key("orogeny_warp_x");
    public static final ResourceKey<DensityFunction> OROGENY_WARP_Z = key("orogeny_warp_z");
    public static final ResourceKey<DensityFunction> PLATEAU_BASE_Y = key("plateau_base_y");
    public static final ResourceKey<DensityFunction> PLATEAU_COORD = key("plateau_coord");
    public static final ResourceKey<DensityFunction> PLATEAU_MASK = key("plateau_mask");
    public static final ResourceKey<DensityFunction> PLATEAU_REGION = key("plateau_region");
    public static final ResourceKey<DensityFunction> PLATEAU_TARGET_RAW_Y = key("plateau_target_raw_y");
    public static final ResourceKey<DensityFunction> PLATEAU_TARGET_Y = key("plateau_target_y");
    public static final ResourceKey<DensityFunction> REGION_WARP_X = key("region_warp_x");
    public static final ResourceKey<DensityFunction> REGION_WARP_Z = key("region_warp_z");
    public static final ResourceKey<DensityFunction> RIDGE_BOOST_Y = key("ridge_boost_y");
    public static final ResourceKey<DensityFunction> RIDGE_COMBINED = key("ridge_combined");
    public static final ResourceKey<DensityFunction> RIDGE_PRIMARY = key("ridge_primary");
    public static final ResourceKey<DensityFunction> RIDGE_PRIMARY_COORD = key("ridge_primary_coord");
    public static final ResourceKey<DensityFunction> RIDGE_SCORE = key("ridge_score");
    public static final ResourceKey<DensityFunction> RIDGE_SECONDARY = key("ridge_secondary");
    public static final ResourceKey<DensityFunction> RIDGE_SECONDARY_COORD = key("ridge_secondary_coord");
    public static final ResourceKey<DensityFunction> ROLLING_BOOST_Y = key("rolling_boost_y");
    public static final ResourceKey<DensityFunction> ROLLING_MASK = key("rolling_mask");
    public static final ResourceKey<DensityFunction> ROLLING_REGION = key("rolling_region");
    public static final ResourceKey<DensityFunction> SURFACE_DETAIL = key("surface_detail");
    public static final ResourceKey<DensityFunction> SURFACE_TARGET_Y = key("surface_target_y");
    public static final ResourceKey<DensityFunction> TERRAIN_SLOPE = key("terrain_slope");
    public static final ResourceKey<DensityFunction> VALLEY_AXIS = key("valley_axis");
    public static final ResourceKey<DensityFunction> VALLEY_MASK = key("valley_mask");
    public static final ResourceKey<DensityFunction> VALLEY_PENALTY_Y = key("valley_penalty_y");
    public static final ResourceKey<DensityFunction> VALLEY_SCORE = key("valley_score");

    private AscDensityFunctions() {
    }

    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        AscDensityDsl d = new AscDensityDsl(context);
        context.register(BIOME_EROSION_BASE, biome_erosion_base(d));
        context.register(BIOME_HUMIDITY, biome_humidity(d));
        context.register(CONTINENT_WARP_X, continent_warp_x(d));
        context.register(CONTINENT_WARP_Z, continent_warp_z(d));
        context.register(HERO_GATE, hero_gate(d));
        context.register(LOCAL_DETAIL_NOISE, local_detail_noise(d));
        context.register(MOUNTAIN_WARP_X, mountain_warp_x(d));
        context.register(MOUNTAIN_WARP_Z, mountain_warp_z(d));
        context.register(MOUNTAIN_WEATHERING_NOISE, mountain_weathering_noise(d));
        context.register(OROGENY_WARP_X, orogeny_warp_x(d));
        context.register(OROGENY_WARP_Z, orogeny_warp_z(d));
        context.register(REGION_WARP_X, region_warp_x(d));
        context.register(REGION_WARP_Z, region_warp_z(d));
        context.register(CONTINENTS, continents(d));
        context.register(MASSIF_NOISE, massif_noise(d));
        context.register(MOUNTAIN_AXIS, mountain_axis(d));
        context.register(RIDGE_PRIMARY_COORD, ridge_primary_coord(d));
        context.register(RIDGE_SECONDARY_COORD, ridge_secondary_coord(d));
        context.register(OROGENY, orogeny(d));
        context.register(MACRO_REGIONS, macro_regions(d));
        context.register(PLATEAU_COORD, plateau_coord(d));
        context.register(VALLEY_AXIS, valley_axis(d));
        context.register(CONTINENTAL_TARGET_Y, continental_target_y(d));
        context.register(LAND_MASK, land_mask(d));
        context.register(MASSIF_BOOST_RAW_Y, massif_boost_raw_y(d));
        context.register(MOUNTAIN_AXIS_ABS, mountain_axis_abs(d));
        context.register(RIDGE_PRIMARY, ridge_primary(d));
        context.register(RIDGE_SECONDARY, ridge_secondary(d));
        context.register(MOUNTAIN_PROVINCE, mountain_province(d));
        context.register(ROLLING_REGION, rolling_region(d));
        context.register(PLATEAU_REGION, plateau_region(d));
        context.register(VALLEY_MASK, valley_mask(d));
        context.register(MOUNTAIN_LAND_GATE, mountain_land_gate(d));
        context.register(CORE_PROFILE, core_profile(d));
        context.register(FOOTHILL_PROFILE, foothill_profile(d));
        context.register(RIDGE_COMBINED, ridge_combined(d));
        context.register(CORE_MASK, core_mask(d));
        context.register(FOOTHILL_MASK, foothill_mask(d));
        context.register(CORE_TARGET_Y, core_target_y(d));
        context.register(HERO_SCORE, hero_score(d));
        context.register(MASSIF_BOOST_Y, massif_boost_y(d));
        context.register(RIDGE_SCORE, ridge_score(d));
        context.register(FOOTHILL_TARGET_Y, foothill_target_y(d));
        context.register(NON_MOUNTAIN, non_mountain(d));
        context.register(SURFACE_DETAIL, surface_detail(d));
        context.register(VALLEY_SCORE, valley_score(d));
        context.register(HERO_TARGET_Y, hero_target_y(d));
        context.register(RIDGE_BOOST_Y, ridge_boost_y(d));
        context.register(MOUNTAIN_TARGET_BASE_Y, mountain_target_base_y(d));
        context.register(PLATEAU_MASK, plateau_mask(d));
        context.register(BIOME_WEIRDNESS_SURFACE, biome_weirdness_surface(d));
        context.register(TERRAIN_SLOPE, terrain_slope(d));
        context.register(VALLEY_PENALTY_Y, valley_penalty_y(d));
        context.register(MOUNTAIN_TARGET_RAW_Y, mountain_target_raw_y(d));
        context.register(MOUNTAIN_TARGET_UNCARVED_Y, mountain_target_uncarved_y(d));
        context.register(BIOME_CONTINENTALNESS, biome_continentalness(d));
        context.register(BIOME_EROSION_SURFACE, biome_erosion_surface(d));
        context.register(PLATEAU_BASE_Y, plateau_base_y(d));
        context.register(ROLLING_MASK, rolling_mask(d));
        context.register(CLIFF_MASK, cliff_mask(d));
        context.register(MOUNTAIN_TARGET_Y, mountain_target_y(d));
        context.register(PLATEAU_TARGET_RAW_Y, plateau_target_raw_y(d));
        context.register(ROLLING_BOOST_Y, rolling_boost_y(d));
        context.register(CLIFF_DETAIL, cliff_detail(d));
        context.register(MOUNTAIN_ERODED_Y, mountain_eroded_y(d));
        context.register(PLATEAU_TARGET_Y, plateau_target_y(d));
        context.register(LOWLAND_TARGET_Y, lowland_target_y(d));
        context.register(SURFACE_TARGET_Y, surface_target_y(d));
        context.register(BASE_HEIGHT, base_height(d));
        context.register(BIOME_DEPTH, biome_depth(d));
        context.register(BIOME_SURFACE_DEPTH, biome_surface_depth(d));
        context.register(FINAL_DENSITY, final_density(d));
        context.register(INITIAL_DENSITY_WITHOUT_JAGGEDNESS, initial_density_without_jaggedness(d));
        context.register(BIOME_SURFACE_PROXIMITY, biome_surface_proximity(d));
        context.register(BIOME_EROSION, biome_erosion(d));
        context.register(BIOME_TEMPERATURE, biome_temperature(d));
        context.register(BIOME_WEIRDNESS, biome_weirdness(d));
    }

    private static DensityFunction biome_erosion_base(AscDensityDsl d) {
        return d.flatCache(d.shiftedNoise2d(d.vanillaDensity("shift_x"), d.vanillaDensity("shift_z"), 0.25D, d.vanillaNoiseKey("erosion")));
    }

    private static DensityFunction biome_humidity(AscDensityDsl d) {
        return d.flatCache(d.shiftedNoise2d(d.vanillaDensity("shift_x"), d.vanillaDensity("shift_z"), 0.25D, d.vanillaNoiseKey("vegetation")));
    }

    private static DensityFunction continent_warp_x(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.CONTINENT_WARP_X, 1.0D, 0.0D));
    }

    private static DensityFunction continent_warp_z(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.CONTINENT_WARP_Z, 1.0D, 0.0D));
    }

    private static DensityFunction hero_gate(AscDensityDsl d) {
        return d.spline(d.flatCache(d.clamp(d.noise(AscNoises.EXTREME_PEAKS, 1.0D, 0.0D), -1.0D, 1.0D)), List.of(p(-1.0F, 0.0F, 0.0F), p(0.28F, 0.0F, 0.0F), p(0.38F, 0.08F, 0.9F), p(0.48F, 0.28F, 2.2F), p(0.58F, 0.56F, 2.8F), p(0.68F, 0.82F, 2.0F), p(0.78F, 0.96F, 0.9F), p(0.86F, 1.0F, 0.0F), p(1.0F, 1.0F, 0.0F)));
    }

    private static DensityFunction local_detail_noise(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.LOCAL_DETAIL, 1.0D, 0.0D));
    }

    private static DensityFunction mountain_warp_x(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.MOUNTAIN_WARP_X, 1.0D, 0.0D));
    }

    private static DensityFunction mountain_warp_z(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.MOUNTAIN_WARP_Z, 1.0D, 0.0D));
    }

    private static DensityFunction mountain_weathering_noise(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.noise(AscNoises.PEAK_MODULATION, 1.0D, 0.0D), -1.0D, 1.0D));
    }

    private static DensityFunction orogeny_warp_x(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.OROGENY_WARP_X, 1.0D, 0.0D));
    }

    private static DensityFunction orogeny_warp_z(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.OROGENY_WARP_Z, 1.0D, 0.0D));
    }

    private static DensityFunction region_warp_x(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.REGION_WARP_X, 1.0D, 0.0D));
    }

    private static DensityFunction region_warp_z(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.REGION_WARP_Z, 1.0D, 0.0D));
    }

    private static DensityFunction continents(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.CONTINENTS, 1.0D, 0.0D), d.ref(CONTINENT_WARP_X), d.ref(CONTINENT_WARP_Z), 720.0D), -1.0D, 1.0D));
    }

    private static DensityFunction massif_noise(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.MASSIFS, 1.0D, 0.0D), d.mul(d.constant(0.35D), d.ref(MOUNTAIN_WARP_X)), d.mul(d.constant(0.35D), d.ref(MOUNTAIN_WARP_Z)), 650.0D), -1.0D, 1.0D));
    }

    private static DensityFunction mountain_axis(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.MOUNTAIN_BELTS, 1.0D, 0.0D), d.ref(MOUNTAIN_WARP_X), d.ref(MOUNTAIN_WARP_Z), 1180.0D), -1.0D, 1.0D));
    }

    private static DensityFunction ridge_primary_coord(AscDensityDsl d) {
        return d.flatCache(d.abs(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.MOUNTAIN_RIDGES, 1.0D, 0.0D), d.mul(d.constant(0.45D), d.ref(MOUNTAIN_WARP_X)), d.mul(d.constant(0.45D), d.ref(MOUNTAIN_WARP_Z)), 520.0D), -1.0D, 1.0D)));
    }

    private static DensityFunction ridge_secondary_coord(AscDensityDsl d) {
        return d.flatCache(d.abs(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.SECONDARY_RIDGES, 1.0D, 0.0D), d.mul(d.constant(0.28D), d.ref(MOUNTAIN_WARP_Z)), d.mul(d.constant(-0.28D), d.ref(MOUNTAIN_WARP_X)), 360.0D), -1.0D, 1.0D)));
    }

    private static DensityFunction orogeny(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.OROGENY, 1.0D, 0.0D), d.ref(OROGENY_WARP_X), d.ref(OROGENY_WARP_Z), 1450.0D), -1.0D, 1.0D));
    }

    private static DensityFunction macro_regions(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.MACRO_REGIONS, 1.0D, 0.0D), d.ref(REGION_WARP_X), d.ref(REGION_WARP_Z), 860.0D), -1.0D, 1.0D));
    }

    private static DensityFunction plateau_coord(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.PLATEAUS, 1.0D, 0.0D), d.ref(REGION_WARP_X), d.ref(REGION_WARP_Z), 880.0D), -1.0D, 1.0D));
    }

    private static DensityFunction valley_axis(AscDensityDsl d) {
        return d.flatCache(d.abs(d.clamp(new DomainWarpDensityFunction(d.noise(AscNoises.GREAT_VALLEYS, 1.0D, 0.0D), d.mul(d.constant(0.62D), d.ref(REGION_WARP_X)), d.mul(d.constant(0.62D), d.ref(REGION_WARP_Z)), 820.0D), -1.0D, 1.0D)));
    }

    private static DensityFunction continental_target_y(AscDensityDsl d) {
        return d.spline(d.ref(CONTINENTS), List.of(p(-1.0F, -18.0F, 0.0F), p(-0.72F, 10.0F, 45.0F), p(-0.5F, 32.0F, 90.0F), p(-0.3F, 51.0F, 70.0F), p(-0.16F, 59.5F, 40.0F), p(-0.06F, 64.0F, 22.0F), p(0.1F, 66.0F, 7.0F), p(0.4F, 68.0F, 3.0F), p(1.0F, 69.0F, 0.0F)));
    }

    private static DensityFunction land_mask(AscDensityDsl d) {
        return d.spline(d.ref(CONTINENTS), List.of(p(-1.0F, 0.0F, 0.0F), p(-0.28F, 0.0F, 0.0F), p(-0.12F, 0.1F, 1.0F), p(0.02F, 0.72F, 2.2F), p(0.14F, 1.0F, 0.0F), p(1.0F, 1.0F, 0.0F)));
    }

    private static DensityFunction massif_boost_raw_y(AscDensityDsl d) {
        return d.spline(d.ref(MASSIF_NOISE), List.of(p(-1.0F, 0.0F, 0.0F), p(0.1F, 0.0F, 0.0F), p(0.25F, 4.0F, 22.0F), p(0.45F, 12.0F, 40.0F), p(0.65F, 24.0F, 55.0F), p(0.82F, 38.0F, 70.0F), p(1.0F, 50.0F, 0.0F)));
    }

    private static DensityFunction mountain_axis_abs(AscDensityDsl d) {
        return d.flatCache(d.abs(d.ref(MOUNTAIN_AXIS)));
    }

    private static DensityFunction ridge_primary(AscDensityDsl d) {
        return d.spline(d.ref(RIDGE_PRIMARY_COORD), List.of(p(0.0F, 1.0F, 0.0F), p(0.04F, 0.99F, -0.2F), p(0.1F, 0.82F, -2.2F), p(0.18F, 0.48F, -2.8F), p(0.28F, 0.16F, -1.7F), p(0.4F, 0.0F, 0.0F), p(1.0F, 0.0F, 0.0F)));
    }

    private static DensityFunction ridge_secondary(AscDensityDsl d) {
        return d.spline(d.ref(RIDGE_SECONDARY_COORD), List.of(p(0.0F, 1.0F, 0.0F), p(0.05F, 0.96F, -0.5F), p(0.12F, 0.7F, -2.8F), p(0.2F, 0.34F, -2.3F), p(0.3F, 0.08F, -1.1F), p(0.36F, 0.0F, 0.0F), p(1.0F, 0.0F, 0.0F)));
    }

    private static DensityFunction mountain_province(AscDensityDsl d) {
        return d.spline(d.ref(OROGENY), List.of(p(-1.0F, 0.0F, 0.0F), p(-0.1F, 0.0F, 0.0F), p(0.0F, 0.03F, 0.5F), p(0.1F, 0.14F, 1.4F), p(0.2F, 0.38F, 2.5F), p(0.3F, 0.66F, 2.7F), p(0.4F, 0.86F, 1.6F), p(0.5F, 0.97F, 0.7F), p(0.58F, 1.0F, 0.0F), p(1.0F, 1.0F, 0.0F)));
    }

    private static DensityFunction rolling_region(AscDensityDsl d) {
        return d.spline(d.ref(MACRO_REGIONS), List.of(p(-1.0F, 0.0F, 0.0F), p(-0.65F, 0.0F, 0.0F), p(-0.44F, 0.18F, 1.0F), p(-0.18F, 0.88F, 1.0F), p(0.1F, 0.72F, -0.6F), p(0.3F, 0.1F, -1.0F), p(0.42F, 0.0F, 0.0F), p(1.0F, 0.0F, 0.0F)));
    }

    private static DensityFunction plateau_region(AscDensityDsl d) {
        return d.spline(d.ref(PLATEAU_COORD), List.of(p(-1.0F, 0.0F, 0.0F), p(0.4F, 0.0F, 0.0F), p(0.52F, 0.06F, 0.6F), p(0.6F, 0.4F, 3.0F), p(0.68F, 0.88F, 3.0F), p(0.75F, 1.0F, 0.0F), p(1.0F, 1.0F, 0.0F)));
    }

    private static DensityFunction valley_mask(AscDensityDsl d) {
        return d.spline(d.ref(VALLEY_AXIS), List.of(p(0.0F, 1.0F, 0.0F), p(0.055F, 1.0F, 0.0F), p(0.11F, 0.9F, -1.3F), p(0.19F, 0.48F, -2.2F), p(0.28F, 0.1F, -1.4F), p(0.34F, 0.0F, 0.0F), p(1.0F, 0.0F, 0.0F)));
    }

    private static DensityFunction mountain_land_gate(AscDensityDsl d) {
        return d.spline(d.ref(LAND_MASK), List.of(p(0.0F, 0.0F, 0.0F), p(0.18F, 0.0F, 0.0F), p(0.28F, 0.34F, 3.0F), p(0.38F, 0.78F, 3.2F), p(0.48F, 1.0F, 0.0F), p(1.0F, 1.0F, 0.0F)));
    }

    private static DensityFunction core_profile(AscDensityDsl d) {
        return d.spline(d.ref(MOUNTAIN_AXIS_ABS), List.of(p(0.0F, 1.0F, 0.0F), p(0.08F, 1.0F, 0.0F), p(0.16F, 0.94F, -0.7F), p(0.24F, 0.78F, -1.8F), p(0.32F, 0.52F, -2.7F), p(0.4F, 0.2F, -2.6F), p(0.46F, 0.0F, 0.0F), p(1.0F, 0.0F, 0.0F)));
    }

    private static DensityFunction foothill_profile(AscDensityDsl d) {
        return d.spline(d.ref(MOUNTAIN_AXIS_ABS), List.of(p(0.0F, 1.0F, 0.0F), p(0.18F, 1.0F, 0.0F), p(0.32F, 0.94F, -0.6F), p(0.46F, 0.72F, -1.8F), p(0.6F, 0.38F, -2.2F), p(0.72F, 0.12F, -1.5F), p(0.8F, 0.0F, 0.0F), p(1.0F, 0.0F, 0.0F)));
    }

    private static DensityFunction ridge_combined(AscDensityDsl d) {
        return d.max(d.ref(RIDGE_PRIMARY), d.mul(d.constant(0.52D), d.ref(RIDGE_SECONDARY)));
    }

    private static DensityFunction core_mask(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.min(d.ref(MOUNTAIN_LAND_GATE), d.min(d.ref(MOUNTAIN_PROVINCE), d.ref(CORE_PROFILE))), 0.0D, 1.0D));
    }

    private static DensityFunction foothill_mask(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.min(d.ref(MOUNTAIN_LAND_GATE), d.min(d.ref(MOUNTAIN_PROVINCE), d.ref(FOOTHILL_PROFILE))), 0.0D, 1.0D));
    }

    private static DensityFunction core_target_y(AscDensityDsl d) {
        return d.spline(d.ref(CORE_MASK), List.of(p(0.0F, -64.0F, 0.0F), p(0.06F, 68.0F, 0.0F), p(0.14F, 86.0F, 180.0F), p(0.24F, 116.0F, 285.0F), p(0.34F, 148.0F, 300.0F), p(0.44F, 178.0F, 285.0F), p(0.54F, 205.0F, 255.0F), p(0.64F, 228.0F, 210.0F), p(0.74F, 246.0F, 165.0F), p(0.84F, 260.0F, 120.0F), p(0.94F, 272.0F, 80.0F), p(1.0F, 278.0F, 0.0F)));
    }

    private static DensityFunction hero_score(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.min(d.ref(CORE_MASK), d.ref(HERO_GATE)), 0.0D, 1.0D));
    }

    private static DensityFunction massif_boost_y(AscDensityDsl d) {
        return d.mul(d.ref(CORE_MASK), d.ref(MASSIF_BOOST_RAW_Y));
    }

    private static DensityFunction ridge_score(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.min(d.ref(CORE_MASK), d.ref(RIDGE_COMBINED)), 0.0D, 1.0D));
    }

    private static DensityFunction foothill_target_y(AscDensityDsl d) {
        return d.spline(d.ref(FOOTHILL_MASK), List.of(p(0.0F, -64.0F, 0.0F), p(0.05F, 62.0F, 0.0F), p(0.15F, 74.0F, 85.0F), p(0.3F, 90.0F, 105.0F), p(0.5F, 110.0F, 100.0F), p(0.7F, 132.0F, 100.0F), p(0.85F, 148.0F, 80.0F), p(1.0F, 160.0F, 0.0F)));
    }

    private static DensityFunction non_mountain(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(FOOTHILL_MASK))), 0.0D, 1.0D));
    }

    private static DensityFunction surface_detail(AscDensityDsl d) {
        return d.mul(d.ref(LOCAL_DETAIL_NOISE), d.add(d.constant(0.018D), d.mul(d.constant(0.03D), d.ref(FOOTHILL_MASK))));
    }

    private static DensityFunction valley_score(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.min(d.ref(VALLEY_MASK), d.ref(FOOTHILL_MASK)), 0.0D, 1.0D));
    }

    private static DensityFunction hero_target_y(AscDensityDsl d) {
        return d.spline(d.ref(HERO_SCORE), List.of(p(0.0F, -64.0F, 0.0F), p(0.38F, -64.0F, 0.0F), p(0.46F, 250.0F, 0.0F), p(0.56F, 274.0F, 220.0F), p(0.66F, 294.0F, 170.0F), p(0.78F, 307.0F, 85.0F), p(0.9F, 313.0F, 30.0F), p(1.0F, 314.0F, 0.0F)));
    }

    private static DensityFunction ridge_boost_y(AscDensityDsl d) {
        return d.mul(d.constant(64.0D), d.ref(RIDGE_SCORE));
    }

    private static DensityFunction mountain_target_base_y(AscDensityDsl d) {
        return d.flatCache(d.max(d.ref(FOOTHILL_TARGET_Y), d.ref(CORE_TARGET_Y)));
    }

    private static DensityFunction plateau_mask(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.mul(d.mul(d.ref(LAND_MASK), d.ref(NON_MOUNTAIN)), d.ref(PLATEAU_REGION)), 0.0D, 1.0D));
    }

    private static DensityFunction biome_weirdness_surface(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.mul(d.vanillaDensity("overworld/ridges"), d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(FOOTHILL_MASK)))), d.add(d.add(d.mul(d.constant(0.5D), d.ref(FOOTHILL_MASK)), d.mul(d.constant(0.1D), d.ref(CORE_MASK))), d.add(d.mul(d.constant(0.06666666666666667D), d.ref(RIDGE_SCORE)), d.mul(d.constant(-0.28D), d.ref(VALLEY_SCORE))))), -1.0D, 1.0D));
    }

    private static DensityFunction terrain_slope(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.max(d.ref(RIDGE_SCORE), d.max(d.mul(d.constant(0.82D), d.ref(VALLEY_SCORE)), d.mul(d.constant(1.45D), d.max(d.constant(0.0D), d.add(d.ref(FOOTHILL_MASK), d.mul(d.constant(-1.0D), d.ref(CORE_MASK))))))), 0.0D, 1.0D));
    }

    private static DensityFunction valley_penalty_y(AscDensityDsl d) {
        return d.mul(d.constant(110.0D), d.ref(VALLEY_SCORE));
    }

    private static DensityFunction mountain_target_raw_y(AscDensityDsl d) {
        return d.max(d.add(d.add(d.ref(MOUNTAIN_TARGET_BASE_Y), d.ref(RIDGE_BOOST_Y)), d.ref(MASSIF_BOOST_Y)), d.ref(HERO_TARGET_Y));
    }

    private static DensityFunction mountain_target_uncarved_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.max(d.add(d.add(d.ref(MOUNTAIN_TARGET_BASE_Y), d.ref(RIDGE_BOOST_Y)), d.ref(MASSIF_BOOST_Y)), d.ref(HERO_TARGET_Y)), -64.0D, 400.0D));
    }

    private static DensityFunction biome_continentalness(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.spline(d.ref(CONTINENTS), List.of(p(-1.0F, -0.95F, 0.0F), p(-0.72F, -0.78F, 0.7F), p(-0.5F, -0.55F, 1.0F), p(-0.3F, -0.34F, 1.0F), p(-0.18F, -0.23F, 0.9F), p(-0.1F, -0.15F, 0.85F), p(-0.06F, -0.105F, 0.75F), p(0.0F, -0.02F, 0.85F), p(0.1F, 0.12F, 0.9F), p(0.4F, 0.42F, 0.7F), p(1.0F, 0.75F, 0.0F))), d.add(d.mul(d.constant(0.28D), d.ref(FOOTHILL_MASK)), d.mul(d.constant(0.08D), d.ref(PLATEAU_MASK)))), -1.0D, 1.0D));
    }

    private static DensityFunction biome_erosion_surface(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.add(d.add(d.add(d.add(d.constant(0.58D), d.mul(d.constant(-0.95D), d.ref(FOOTHILL_MASK))), d.mul(d.constant(-0.38D), d.ref(CORE_MASK))), d.mul(d.constant(-0.18D), d.ref(RIDGE_SCORE))), d.mul(d.constant(0.38D), d.ref(VALLEY_SCORE))), d.mul(d.constant(-0.25D), d.ref(PLATEAU_MASK))), -1.0D, 1.0D));
    }

    private static DensityFunction plateau_base_y(AscDensityDsl d) {
        return d.spline(d.ref(PLATEAU_MASK), List.of(p(0.0F, -64.0F, 0.0F), p(0.12F, 68.0F, 0.0F), p(0.28F, 100.0F, 180.0F), p(0.46F, 132.0F, 170.0F), p(0.64F, 155.0F, 115.0F), p(0.82F, 174.0F, 80.0F), p(1.0F, 188.0F, 0.0F)));
    }

    private static DensityFunction rolling_mask(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.mul(d.mul(d.mul(d.ref(LAND_MASK), d.ref(NON_MOUNTAIN)), d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(PLATEAU_MASK)))), d.ref(ROLLING_REGION)), 0.0D, 1.0D));
    }

    private static DensityFunction cliff_mask(AscDensityDsl d) {
        return d.spline(d.ref(TERRAIN_SLOPE), List.of(p(0.0F, 0.0F, 0.0F), p(0.12F, 0.0F, 0.0F), p(0.22F, 0.06F, 0.8F), p(0.34F, 0.44F, 2.6F), p(0.48F, 0.9F, 1.6F), p(0.62F, 1.0F, 0.0F), p(1.0F, 1.0F, 0.0F)));
    }

    private static DensityFunction mountain_target_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.ref(MOUNTAIN_TARGET_UNCARVED_Y), d.mul(d.constant(-1.0D), d.ref(VALLEY_PENALTY_Y))), -64.0D, 400.0D));
    }

    private static DensityFunction plateau_target_raw_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.ref(PLATEAU_BASE_Y), d.mul(d.constant(10.0D), d.mul(d.ref(PLATEAU_MASK), d.noise(AscNoises.PLATEAU_DETAIL, 1.0D, 0.0D)))), -64.0D, 198.0D));
    }

    private static DensityFunction rolling_boost_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.mul(d.ref(ROLLING_MASK), d.spline(d.flatCache(d.clamp(d.noise(AscNoises.HIGHLANDS, 1.0D, 0.0D), -1.0D, 1.0D)), List.of(p(-1.0F, -3.0F, 0.0F), p(-0.5F, -1.0F, 3.0F), p(0.0F, 3.0F, 8.0F), p(0.5F, 9.0F, 8.0F), p(1.0F, 15.0F, 0.0F)))), -4.0D, 16.0D));
    }

    private static DensityFunction cliff_detail(AscDensityDsl d) {
        return d.mul(d.constant(0.13D), d.mul(d.ref(CLIFF_MASK), d.noise(AscNoises.CLIFF_DETAIL, 1.0D, 0.72D)));
    }

    private static DensityFunction mountain_eroded_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.ref(MOUNTAIN_TARGET_Y), d.mul(d.constant(12.0D), d.mul(d.ref(FOOTHILL_MASK), d.ref(MOUNTAIN_WEATHERING_NOISE)))), -64.0D, 400.0D));
    }

    private static DensityFunction plateau_target_y(AscDensityDsl d) {
        return d.flatCache(new TerraceDensityFunction(d.ref(PLATEAU_TARGET_RAW_Y), 14.0D, 0.82D, 0.075D));
    }

    private static DensityFunction lowland_target_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.ref(CONTINENTAL_TARGET_Y), d.ref(ROLLING_BOOST_Y)), -24.0D, 88.0D));
    }

    private static DensityFunction surface_target_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.max(d.ref(LOWLAND_TARGET_Y), d.max(d.ref(PLATEAU_TARGET_Y), d.ref(MOUNTAIN_ERODED_Y))), -24.0D, 400.0D));
    }

    private static DensityFunction base_height(AscDensityDsl d) {
        return d.flatCache(new HeightTargetDensityFunction(d.ref(SURFACE_TARGET_Y), 64.0D, 64.0D));
    }

    private static DensityFunction biome_depth(AscDensityDsl d) {
        return d.clamp(d.add(new HeightTargetDensityFunction(d.ref(SURFACE_TARGET_Y), 0.0D, 32.0D), d.yGradient(-64, 416, 2.0D, -13.0D)), -1.0D, 1.0D);
    }

    private static DensityFunction biome_surface_depth(AscDensityDsl d) {
        return d.add(new HeightTargetDensityFunction(d.ref(SURFACE_TARGET_Y), 0.0D, 64.0D), d.yGradient(-64, 416, 1.0D, -6.5D));
    }

    private static DensityFunction final_density(AscDensityDsl d) {
        return d.squeeze(d.mul(d.constant(0.64D), d.interpolated(d.blendDensity(d.add(d.add(d.yGradient(-64, 416, 2.0D, -5.5D), d.ref(BASE_HEIGHT)), d.add(d.ref(SURFACE_DETAIL), d.ref(CLIFF_DETAIL)))))));
    }

    private static DensityFunction initial_density_without_jaggedness(AscDensityDsl d) {
        return d.clamp(d.add(d.yGradient(-64, 416, 2.0D, -5.5D), d.ref(BASE_HEIGHT)), -64.0D, 64.0D);
    }

    private static DensityFunction biome_surface_proximity(AscDensityDsl d) {
        return d.clamp(d.spline(d.ref(BIOME_SURFACE_DEPTH), List.of(p(-8.0F, 1.0F, 0.0F), p(0.75F, 1.0F, 0.0F), p(1.75F, 0.0F, 0.0F), p(8.0F, 0.0F, 0.0F))), 0.0D, 1.0D);
    }

    private static DensityFunction biome_erosion(AscDensityDsl d) {
        return d.clamp(d.add(d.mul(d.ref(BIOME_EROSION_BASE), d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(BIOME_SURFACE_PROXIMITY)))), d.mul(d.ref(BIOME_EROSION_SURFACE), d.ref(BIOME_SURFACE_PROXIMITY))), -1.0D, 1.0D);
    }

    private static DensityFunction biome_temperature(AscDensityDsl d) {
        return d.clamp(d.add(d.flatCache(d.shiftedNoise2d(d.vanillaDensity("shift_x"), d.vanillaDensity("shift_z"), 0.25D, d.vanillaNoiseKey("temperature"))), d.mul(d.yGradient(64, 384, 0.0D, -0.62D), d.ref(BIOME_SURFACE_PROXIMITY))), -1.0D, 1.0D);
    }

    private static DensityFunction biome_weirdness(AscDensityDsl d) {
        return d.clamp(d.add(d.mul(d.vanillaDensity("overworld/ridges"), d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(BIOME_SURFACE_PROXIMITY)))), d.mul(d.ref(BIOME_WEIRDNESS_SURFACE), d.ref(BIOME_SURFACE_PROXIMITY))), -1.0D, 1.0D);
    }

    private static Point p(float location, float value, float derivative) {
        return new Point(location, value, derivative);
    }

    private static ResourceKey<DensityFunction> key(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, AscensionCraft.prefix("terrain/" + name));
    }
}
