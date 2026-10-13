package net.zic.ascension.worldgen.density;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.density.HermiteSplineDensityFunction.Point;
import net.zic.ascension.worldgen.noise.AscNoises;

import java.util.List;

public final class AscDensityFunctions {
    public static final ResourceKey<DensityFunction> BASE_HEIGHT = key("base_height");
    public static final ResourceKey<DensityFunction> BASE_TARGET_Y = key("base_target_y");
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
    public static final ResourceKey<DensityFunction> CONTINENT_WARP_X = key("continent_warp_x");
    public static final ResourceKey<DensityFunction> CONTINENT_WARP_Z = key("continent_warp_z");
    public static final ResourceKey<DensityFunction> CONTINENTAL_TARGET_Y = key("continental_target_y");
    public static final ResourceKey<DensityFunction> CONTINENTS = key("continents");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY = key("final_density");
    public static final ResourceKey<DensityFunction> FOLD_NOISE = key("fold_noise");
    public static final ResourceKey<DensityFunction> FOLD_RELIEF_Y = key("fold_relief_y");
    public static final ResourceKey<DensityFunction> INITIAL_DENSITY_WITHOUT_JAGGEDNESS = key("initial_density_without_jaggedness");
    public static final ResourceKey<DensityFunction> LAND_MASK = key("land_mask");
    public static final ResourceKey<DensityFunction> LOCAL_DETAIL_NOISE = key("local_detail_noise");
    public static final ResourceKey<DensityFunction> LOWLAND_DETAIL_Y = key("lowland_detail_y");
    public static final ResourceKey<DensityFunction> MASSIF_BOOST_Y = key("massif_boost_y");
    public static final ResourceKey<DensityFunction> MASSIF_NOISE = key("massif_noise");
    public static final ResourceKey<DensityFunction> MASSIF_SCORE = key("massif_score");
    public static final ResourceKey<DensityFunction> MOUNTAIN_AXIS = key("mountain_axis");
    public static final ResourceKey<DensityFunction> MOUNTAIN_AXIS_ABS = key("mountain_axis_abs");
    public static final ResourceKey<DensityFunction> MOUNTAIN_LAND_GATE = key("mountain_land_gate");
    public static final ResourceKey<DensityFunction> MOUNTAIN_MASK = key("mountain_mask");
    public static final ResourceKey<DensityFunction> MOUNTAIN_PROVINCE = key("mountain_province");
    public static final ResourceKey<DensityFunction> MOUNTAIN_RAW_Y = key("mountain_raw_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_RELIEF_Y = key("mountain_relief_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_SHOULDER = key("mountain_shoulder");
    public static final ResourceKey<DensityFunction> MOUNTAIN_UPLIFT_Y = key("mountain_uplift_y");
    public static final ResourceKey<DensityFunction> MOUNTAIN_WARP_X = key("mountain_warp_x");
    public static final ResourceKey<DensityFunction> MOUNTAIN_WARP_Z = key("mountain_warp_z");
    public static final ResourceKey<DensityFunction> MOUNTAIN_WEATHERING_NOISE = key("mountain_weathering_noise");
    public static final ResourceKey<DensityFunction> OROGENY = key("orogeny");
    public static final ResourceKey<DensityFunction> PROVINCE_RELIEF_Y = key("province_relief_y");
    public static final ResourceKey<DensityFunction> RANGE_PROFILE = key("range_profile");
    public static final ResourceKey<DensityFunction> ROLLING_HILLS_NOISE = key("rolling_hills_noise");
    public static final ResourceKey<DensityFunction> RIDGE_ASYMMETRY = key("ridge_asymmetry");
    public static final ResourceKey<DensityFunction> RIDGE_BOOST_Y = key("ridge_boost_y");
    public static final ResourceKey<DensityFunction> RIDGE_PRIMARY = key("ridge_primary");
    public static final ResourceKey<DensityFunction> RIDGE_PRIMARY_COORD = key("ridge_primary_coord");
    public static final ResourceKey<DensityFunction> RIDGE_SCORE = key("ridge_score");
    public static final ResourceKey<DensityFunction> RIDGE_SECONDARY = key("ridge_secondary");
    public static final ResourceKey<DensityFunction> RIDGE_SECONDARY_COORD = key("ridge_secondary_coord");
    public static final ResourceKey<DensityFunction> STRUCTURAL_TARGET_Y = key("structural_target_y");
    public static final ResourceKey<DensityFunction> SURFACE_TARGET_Y = key("surface_target_y");
    public static final ResourceKey<DensityFunction> TERRACE_INPUT_Y = key("terrace_input_y");
    public static final ResourceKey<DensityFunction> TERRACE_MASK = key("terrace_mask");
    public static final ResourceKey<DensityFunction> TERRACE_OFFSET_Y = key("terrace_offset_y");
    public static final ResourceKey<DensityFunction> TERRACED_RAW_Y = key("terraced_raw_y");
    public static final ResourceKey<DensityFunction> TERRACED_TARGET_Y = key("terraced_target_y");
    public static final ResourceKey<DensityFunction> TERRAIN_SLOPE = key("terrain_slope");
    public static final ResourceKey<DensityFunction> VALLEY_AXIS = key("valley_axis");
    public static final ResourceKey<DensityFunction> VALLEY_CUT_Y = key("valley_cut_y");
    public static final ResourceKey<DensityFunction> VALLEY_FLOOR_Y = key("valley_floor_y");
    public static final ResourceKey<DensityFunction> VALLEY_SCORE = key("valley_score");
    public static final ResourceKey<DensityFunction> WEATHERING_RELIEF_Y = key("weathering_relief_y");

    private AscDensityFunctions() {
    }

    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        AscDensityDsl d = new AscDensityDsl(context);
        context.register(BIOME_EROSION_BASE, biome_erosion_base(d));
        context.register(BIOME_HUMIDITY, biome_humidity(d));
        context.register(CONTINENT_WARP_X, continent_warp_x(d));
        context.register(CONTINENT_WARP_Z, continent_warp_z(d));
        context.register(LOCAL_DETAIL_NOISE, local_detail_noise(d));
        context.register(ROLLING_HILLS_NOISE, rolling_hills_noise(d));
        context.register(MOUNTAIN_WARP_X, mountain_warp_x(d));
        context.register(MOUNTAIN_WARP_Z, mountain_warp_z(d));
        context.register(MOUNTAIN_WEATHERING_NOISE, mountain_weathering_noise(d));
        context.register(CONTINENTS, continents(d));
        context.register(OROGENY, orogeny(d));
        context.register(MOUNTAIN_AXIS, mountain_axis(d));
        context.register(MASSIF_NOISE, massif_noise(d));
        context.register(FOLD_NOISE, fold_noise(d));
        context.register(RIDGE_PRIMARY_COORD, ridge_primary_coord(d));
        context.register(RIDGE_SECONDARY_COORD, ridge_secondary_coord(d));
        context.register(VALLEY_AXIS, valley_axis(d));
        context.register(CONTINENTAL_TARGET_Y, continental_target_y(d));
        context.register(LAND_MASK, land_mask(d));
        context.register(MOUNTAIN_LAND_GATE, mountain_land_gate(d));
        context.register(PROVINCE_RELIEF_Y, province_relief_y(d));
        context.register(MOUNTAIN_PROVINCE, mountain_province(d));
        context.register(MOUNTAIN_AXIS_ABS, mountain_axis_abs(d));
        context.register(RANGE_PROFILE, range_profile(d));
        context.register(MOUNTAIN_MASK, mountain_mask(d));
        context.register(LOWLAND_DETAIL_Y, lowland_detail_y(d));
        context.register(BASE_TARGET_Y, base_target_y(d));
        context.register(MOUNTAIN_UPLIFT_Y, mountain_uplift_y(d));
        context.register(MASSIF_SCORE, massif_score(d));
        context.register(MASSIF_BOOST_Y, massif_boost_y(d));
        context.register(RIDGE_PRIMARY, ridge_primary(d));
        context.register(RIDGE_SECONDARY, ridge_secondary(d));
        context.register(RIDGE_SCORE, ridge_score(d));
        context.register(RIDGE_ASYMMETRY, ridge_asymmetry(d));
        context.register(RIDGE_BOOST_Y, ridge_boost_y(d));
        context.register(VALLEY_SCORE, valley_score(d));
        context.register(FOLD_RELIEF_Y, fold_relief_y(d));
        context.register(VALLEY_CUT_Y, valley_cut_y(d));
        context.register(VALLEY_FLOOR_Y, valley_floor_y(d));
        context.register(MOUNTAIN_RELIEF_Y, mountain_relief_y(d));
        context.register(MOUNTAIN_RAW_Y, mountain_raw_y(d));
        context.register(STRUCTURAL_TARGET_Y, structural_target_y(d));
        context.register(MOUNTAIN_SHOULDER, mountain_shoulder(d));
        context.register(TERRACE_MASK, terrace_mask(d));
        context.register(TERRACE_OFFSET_Y, terrace_offset_y(d));
        context.register(TERRACE_INPUT_Y, terrace_input_y(d));
        context.register(TERRACED_RAW_Y, terraced_raw_y(d));
        context.register(TERRACED_TARGET_Y, terraced_target_y(d));
        context.register(WEATHERING_RELIEF_Y, weathering_relief_y(d));
        context.register(SURFACE_TARGET_Y, surface_target_y(d));
        context.register(TERRAIN_SLOPE, terrain_slope(d));
        context.register(BIOME_CONTINENTALNESS, biome_continentalness(d));
        context.register(BIOME_EROSION_SURFACE, biome_erosion_surface(d));
        context.register(BIOME_WEIRDNESS_SURFACE, biome_weirdness_surface(d));
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

    private static DensityFunction local_detail_noise(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.noise(AscNoises.LOCAL_DETAIL, 1.0D, 0.0D), -1.0D, 1.0D));
    }

    private static DensityFunction rolling_hills_noise(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.noise(AscNoises.ROLLING_HILLS, 1.0D, 0.0D), -1.0D, 1.0D));
    }

    private static DensityFunction mountain_warp_x(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.MOUNTAIN_WARP_X, 1.0D, 0.0D));
    }

    private static DensityFunction mountain_warp_z(AscDensityDsl d) {
        return d.flatCache(d.noise(AscNoises.MOUNTAIN_WARP_Z, 1.0D, 0.0D));
    }

    private static DensityFunction mountain_weathering_noise(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.noise(AscNoises.WEATHERING, 1.0D, 0.0D), -1.0D, 1.0D));
    }

    private static DensityFunction continents(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.CONTINENTS, 1.0D, 0.0D),
                d.ref(CONTINENT_WARP_X),
                d.ref(CONTINENT_WARP_Z),
                640.0D
        ), -1.0D, 1.0D));
    }

    private static DensityFunction orogeny(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.OROGENY, 1.0D, 0.0D),
                d.ref(MOUNTAIN_WARP_X),
                d.ref(MOUNTAIN_WARP_Z),
                980.0D
        ), -1.0D, 1.0D));
    }

    private static DensityFunction mountain_axis(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.MOUNTAIN_BELTS, 1.0D, 0.0D),
                d.ref(MOUNTAIN_WARP_X),
                d.ref(MOUNTAIN_WARP_Z),
                1120.0D
        ), -1.0D, 1.0D));
    }

    private static DensityFunction massif_noise(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.MASSIFS, 1.0D, 0.0D),
                d.mul(d.constant(0.46D), d.ref(MOUNTAIN_WARP_X)),
                d.mul(d.constant(0.46D), d.ref(MOUNTAIN_WARP_Z)),
                560.0D
        ), -1.0D, 1.0D));
    }

    private static DensityFunction fold_noise(AscDensityDsl d) {
        return d.flatCache(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.MOUNTAIN_FOLDS, 1.0D, 0.0D),
                d.mul(d.constant(0.34D), d.ref(MOUNTAIN_WARP_X)),
                d.mul(d.constant(-0.34D), d.ref(MOUNTAIN_WARP_Z)),
                360.0D
        ), -1.0D, 1.0D));
    }

    private static DensityFunction ridge_primary_coord(AscDensityDsl d) {
        return d.flatCache(d.abs(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.MOUNTAIN_RIDGES, 1.0D, 0.0D),
                d.mul(d.constant(0.42D), d.ref(MOUNTAIN_WARP_X)),
                d.mul(d.constant(0.42D), d.ref(MOUNTAIN_WARP_Z)),
                430.0D
        ), -1.0D, 1.0D)));
    }

    private static DensityFunction ridge_secondary_coord(AscDensityDsl d) {
        return d.flatCache(d.abs(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.SECONDARY_RIDGES, 1.0D, 0.0D),
                d.mul(d.constant(0.24D), d.ref(MOUNTAIN_WARP_Z)),
                d.mul(d.constant(-0.24D), d.ref(MOUNTAIN_WARP_X)),
                280.0D
        ), -1.0D, 1.0D)));
    }

    private static DensityFunction valley_axis(AscDensityDsl d) {
        return d.flatCache(d.abs(d.clamp(new DomainWarpDensityFunction(
                d.noise(AscNoises.GREAT_VALLEYS, 1.0D, 0.0D),
                d.mul(d.constant(0.62D), d.ref(MOUNTAIN_WARP_X)),
                d.mul(d.constant(0.62D), d.ref(MOUNTAIN_WARP_Z)),
                760.0D
        ), -1.0D, 1.0D)));
    }

    private static DensityFunction continental_target_y(AscDensityDsl d) {
        return d.spline(d.ref(CONTINENTS), List.of(
                p(-1.0F, -28.0F, 0.0F),
                p(-0.76F, -2.0F, 70.0F),
                p(-0.56F, 31.0F, 120.0F),
                p(-0.38F, 52.0F, 82.0F),
                p(-0.24F, 60.0F, 38.0F),
                p(-0.12F, 64.0F, 22.0F),
                p(0.0F, 67.0F, 12.0F),
                p(0.35F, 69.5F, 3.0F),
                p(1.0F, 72.0F, 0.0F)
        ));
    }

    private static DensityFunction land_mask(AscDensityDsl d) {
        return d.spline(d.ref(CONTINENTS), List.of(
                p(-1.0F, 0.0F, 0.0F),
                p(-0.34F, 0.0F, 0.0F),
                p(-0.22F, 0.04F, 0.7F),
                p(-0.1F, 0.42F, 3.2F),
                p(0.04F, 0.84F, 2.0F),
                p(0.16F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
    }

    private static DensityFunction mountain_land_gate(AscDensityDsl d) {
        return d.spline(d.ref(LAND_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.14F, 0.0F, 0.0F),
                p(0.28F, 0.14F, 1.6F),
                p(0.44F, 0.56F, 3.0F),
                p(0.62F, 0.9F, 1.5F),
                p(0.76F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
    }

    private static DensityFunction province_relief_y(AscDensityDsl d) {
        DensityFunction relief = d.spline(d.ref(OROGENY), List.of(
                p(-1.0F, -5.0F, 0.0F),
                p(-0.6F, -2.0F, 7.0F),
                p(-0.28F, 0.0F, 6.0F),
                p(-0.08F, 4.0F, 22.0F),
                p(0.12F, 11.0F, 34.0F),
                p(0.32F, 21.0F, 48.0F),
                p(0.52F, 33.0F, 56.0F),
                p(0.72F, 45.0F, 48.0F),
                p(0.88F, 53.0F, 30.0F),
                p(1.0F, 58.0F, 0.0F)
        ));
        return d.flatCache(d.mul(d.ref(LAND_MASK), relief));
    }

    private static DensityFunction mountain_province(AscDensityDsl d) {
        return d.spline(d.ref(OROGENY), List.of(
                p(-1.0F, 0.0F, 0.0F),
                p(-0.32F, 0.0F, 0.0F),
                p(-0.16F, 0.05F, 0.6F),
                p(0.0F, 0.18F, 1.1F),
                p(0.16F, 0.42F, 1.6F),
                p(0.34F, 0.68F, 1.3F),
                p(0.52F, 0.9F, 0.8F),
                p(0.68F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
    }

    private static DensityFunction mountain_axis_abs(AscDensityDsl d) {
        return d.flatCache(d.abs(d.ref(MOUNTAIN_AXIS)));
    }

    private static DensityFunction range_profile(AscDensityDsl d) {
        return d.spline(d.ref(MOUNTAIN_AXIS_ABS), List.of(
                p(0.0F, 1.0F, 0.0F),
                p(0.08F, 1.0F, 0.0F),
                p(0.18F, 0.98F, -0.35F),
                p(0.32F, 0.87F, -1.1F),
                p(0.48F, 0.68F, -1.25F),
                p(0.64F, 0.46F, -1.25F),
                p(0.78F, 0.24F, -1.65F),
                p(0.9F, 0.05F, -1.1F),
                p(1.0F, 0.0F, 0.0F)
        ));
    }

    private static DensityFunction mountain_mask(AscDensityDsl d) {
        DensityFunction rangeWithinProvince = d.min(d.ref(MOUNTAIN_PROVINCE), d.ref(RANGE_PROFILE));
        return d.flatCache(d.clamp(
                d.mul(d.ref(MOUNTAIN_LAND_GATE), rangeWithinProvince),
                0.0D,
                1.0D
        ));
    }

    private static DensityFunction lowland_detail_y(AscDensityDsl d) {
        DensityFunction rolling = d.spline(d.ref(ROLLING_HILLS_NOISE), List.of(
                p(-1.0F, -6.0F, 0.0F),
                p(-0.62F, -3.0F, 8.0F),
                p(-0.25F, -1.0F, 6.0F),
                p(0.1F, 2.0F, 11.0F),
                p(0.4F, 7.0F, 18.0F),
                p(0.7F, 13.0F, 20.0F),
                p(1.0F, 18.0F, 0.0F)
        ));
        DensityFunction mountainSuppression = d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 1.0F, 0.0F),
                p(0.22F, 1.0F, 0.0F),
                p(0.42F, 0.82F, -1.0F),
                p(0.62F, 0.56F, -1.2F),
                p(0.82F, 0.32F, -0.8F),
                p(1.0F, 0.2F, 0.0F)
        ));
        DensityFunction broadRelief = d.mul(rolling, mountainSuppression);
        DensityFunction localRelief = d.mul(d.constant(3.0D), d.ref(LOCAL_DETAIL_NOISE));
        return d.flatCache(d.mul(d.ref(LAND_MASK), d.add(broadRelief, localRelief)));
    }

    private static DensityFunction base_target_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(
                d.add(d.add(d.ref(CONTINENTAL_TARGET_Y), d.ref(PROVINCE_RELIEF_Y)), d.ref(LOWLAND_DETAIL_Y)),
                -34.0D,
                148.0D
        ));
    }

    private static DensityFunction mountain_uplift_y(AscDensityDsl d) {
        return d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.06F, 0.0F, 0.0F),
                p(0.14F, 9.0F, 105.0F),
                p(0.24F, 26.0F, 185.0F),
                p(0.36F, 50.0F, 210.0F),
                p(0.5F, 78.0F, 200.0F),
                p(0.64F, 107.0F, 195.0F),
                p(0.78F, 137.0F, 170.0F),
                p(0.9F, 158.0F, 105.0F),
                p(1.0F, 170.0F, 0.0F)
        ));
    }

    private static DensityFunction massif_score(AscDensityDsl d) {
        DensityFunction massifShape = d.spline(d.ref(MASSIF_NOISE), List.of(
                p(-1.0F, 0.0F, 0.0F),
                p(-0.38F, 0.0F, 0.0F),
                p(-0.22F, 0.08F, 0.75F),
                p(-0.05F, 0.24F, 1.05F),
                p(0.18F, 0.48F, 1.05F),
                p(0.4F, 0.7F, 0.95F),
                p(0.62F, 0.88F, 0.7F),
                p(0.82F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
        DensityFunction mountainContext = d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.16F, 0.0F, 0.0F),
                p(0.26F, 0.25F, 2.3F),
                p(0.38F, 0.55F, 2.2F),
                p(0.5F, 0.78F, 1.55F),
                p(0.64F, 0.94F, 0.7F),
                p(0.8F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
        return d.flatCache(d.clamp(d.min(mountainContext, massifShape), 0.0D, 1.0D));
    }

    private static DensityFunction massif_boost_y(AscDensityDsl d) {
        return d.spline(d.ref(MASSIF_SCORE), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.12F, 0.0F, 0.0F),
                p(0.25F, 5.0F, 48.0F),
                p(0.4F, 14.0F, 68.0F),
                p(0.55F, 26.0F, 78.0F),
                p(0.7F, 39.0F, 76.0F),
                p(0.84F, 52.0F, 54.0F),
                p(1.0F, 62.0F, 0.0F)
        ));
    }

    private static DensityFunction fold_relief_y(AscDensityDsl d) {
        DensityFunction foldShape = d.spline(d.ref(FOLD_NOISE), List.of(
                p(-1.0F, -13.0F, 0.0F),
                p(-0.68F, -9.0F, 10.0F),
                p(-0.34F, -4.0F, 14.0F),
                p(-0.08F, -1.0F, 12.0F),
                p(0.16F, 3.0F, 22.0F),
                p(0.38F, 9.0F, 30.0F),
                p(0.62F, 17.0F, 31.0F),
                p(0.82F, 23.0F, 20.0F),
                p(1.0F, 26.0F, 0.0F)
        ));
        DensityFunction mountainContext = d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.14F, 0.0F, 0.0F),
                p(0.24F, 0.22F, 2.3F),
                p(0.36F, 0.56F, 2.7F),
                p(0.5F, 0.84F, 1.65F),
                p(0.66F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
        DensityFunction valleySuppression = d.clamp(
                d.add(d.constant(1.0D), d.mul(d.constant(-0.5D), d.ref(VALLEY_SCORE))),
                0.45D,
                1.0D
        );
        return d.flatCache(d.clamp(
                d.mul(foldShape, d.mul(mountainContext, valleySuppression)),
                -14.0D,
                27.0D
        ));
    }

    private static DensityFunction ridge_primary(AscDensityDsl d) {
        return d.spline(d.ref(RIDGE_PRIMARY_COORD), List.of(
                p(0.0F, 1.0F, 0.0F),
                p(0.045F, 0.99F, -0.3F),
                p(0.1F, 0.82F, -3.0F),
                p(0.17F, 0.5F, -3.7F),
                p(0.25F, 0.2F, -2.4F),
                p(0.34F, 0.03F, -1.0F),
                p(0.4F, 0.0F, 0.0F),
                p(1.0F, 0.0F, 0.0F)
        ));
    }

    private static DensityFunction ridge_secondary(AscDensityDsl d) {
        return d.spline(d.ref(RIDGE_SECONDARY_COORD), List.of(
                p(0.0F, 1.0F, 0.0F),
                p(0.05F, 0.96F, -0.6F),
                p(0.11F, 0.68F, -4.0F),
                p(0.18F, 0.34F, -3.2F),
                p(0.26F, 0.1F, -1.8F),
                p(0.34F, 0.0F, 0.0F),
                p(1.0F, 0.0F, 0.0F)
        ));
    }

    private static DensityFunction ridge_score(AscDensityDsl d) {
        DensityFunction ridgeShape = d.max(d.ref(RIDGE_PRIMARY), d.mul(d.constant(0.76D), d.ref(RIDGE_SECONDARY)));
        DensityFunction mountainContext = d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.12F, 0.0F, 0.0F),
                p(0.22F, 0.3F, 2.8F),
                p(0.34F, 0.62F, 2.35F),
                p(0.48F, 0.85F, 1.25F),
                p(0.64F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
        return d.flatCache(d.clamp(d.min(mountainContext, ridgeShape), 0.0D, 1.0D));
    }

    private static DensityFunction ridge_asymmetry(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.spline(d.ref(FOLD_NOISE), List.of(
                p(-1.0F, 0.68F, 0.0F),
                p(-0.58F, 0.76F, 0.2F),
                p(-0.2F, 0.9F, 0.55F),
                p(0.12F, 1.0F, 0.5F),
                p(0.42F, 1.13F, 0.4F),
                p(0.72F, 1.23F, 0.2F),
                p(1.0F, 1.28F, 0.0F)
        )), 0.65D, 1.3D));
    }

    private static DensityFunction ridge_boost_y(AscDensityDsl d) {
        DensityFunction ridgeRelief = d.spline(d.ref(RIDGE_SCORE), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.12F, 0.0F, 0.0F),
                p(0.24F, 5.0F, 42.0F),
                p(0.4F, 13.0F, 55.0F),
                p(0.58F, 24.0F, 60.0F),
                p(0.74F, 34.0F, 48.0F),
                p(0.88F, 41.0F, 24.0F),
                p(1.0F, 44.0F, 0.0F)
        ));
        return d.flatCache(d.mul(ridgeRelief, d.ref(RIDGE_ASYMMETRY)));
    }

    private static DensityFunction valley_score(AscDensityDsl d) {
        DensityFunction valleyShape = d.spline(d.ref(VALLEY_AXIS), List.of(
                p(0.0F, 1.0F, 0.0F),
                p(0.06F, 1.0F, 0.0F),
                p(0.14F, 0.88F, -2.2F),
                p(0.23F, 0.55F, -3.2F),
                p(0.33F, 0.22F, -2.5F),
                p(0.43F, 0.02F, -0.75F),
                p(0.5F, 0.0F, 0.0F),
                p(1.0F, 0.0F, 0.0F)
        ));
        DensityFunction mountainContext = d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.2F, 0.0F, 0.0F),
                p(0.3F, 0.2F, 2.0F),
                p(0.42F, 0.5F, 2.3F),
                p(0.56F, 0.78F, 1.55F),
                p(0.7F, 1.0F, 0.0F),
                p(1.0F, 1.0F, 0.0F)
        ));
        return d.flatCache(d.clamp(d.min(mountainContext, valleyShape), 0.0D, 1.0D));
    }

    private static DensityFunction valley_cut_y(AscDensityDsl d) {
        return d.spline(d.ref(VALLEY_SCORE), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.12F, 0.0F, 0.0F),
                p(0.22F, 6.0F, 58.0F),
                p(0.36F, 17.0F, 82.0F),
                p(0.5F, 31.0F, 94.0F),
                p(0.66F, 46.0F, 88.0F),
                p(0.82F, 58.0F, 58.0F),
                p(1.0F, 68.0F, 0.0F)
        ));
    }

    private static DensityFunction valley_floor_y(AscDensityDsl d) {
        return d.flatCache(d.add(d.ref(BASE_TARGET_Y), d.mul(d.constant(-5.0D), d.ref(VALLEY_SCORE))));
    }

    private static DensityFunction mountain_relief_y(AscDensityDsl d) {
        return d.flatCache(d.add(
                d.add(d.ref(MOUNTAIN_UPLIFT_Y), d.ref(MASSIF_BOOST_Y)),
                d.add(d.ref(FOLD_RELIEF_Y), d.ref(RIDGE_BOOST_Y))
        ));
    }

    private static DensityFunction mountain_raw_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(d.ref(BASE_TARGET_Y), d.ref(MOUNTAIN_RELIEF_Y)), -34.0D, 420.0D));
    }

    private static DensityFunction structural_target_y(AscDensityDsl d) {
        DensityFunction carved = d.add(d.ref(MOUNTAIN_RAW_Y), d.mul(d.constant(-1.0D), d.ref(VALLEY_CUT_Y)));
        return d.flatCache(d.clamp(d.max(carved, d.ref(VALLEY_FLOOR_Y)), -34.0D, 412.0D));
    }

    private static DensityFunction terrace_mask(AscDensityDsl d) {
        DensityFunction baseTerrace = d.add(
                d.mul(d.constant(0.34D), d.ref(MOUNTAIN_SHOULDER)),
                d.mul(d.constant(0.035D), d.ref(RIDGE_SCORE))
        );
        DensityFunction terraceSelector = d.spline(d.ref(MOUNTAIN_WEATHERING_NOISE), List.of(
                p(-1.0F, 0.0F, 0.0F),
                p(-0.5F, 0.0F, 0.0F),
                p(-0.22F, 0.04F, 0.35F),
                p(0.02F, 0.18F, 0.8F),
                p(0.24F, 0.48F, 1.6F),
                p(0.46F, 0.82F, 1.25F),
                p(0.7F, 1.0F, 0.0F),
                p(1.0F, 0.9F, 0.0F)
        ));
        DensityFunction valleySuppression = d.clamp(
                d.add(d.constant(1.0D), d.mul(d.constant(-0.72D), d.ref(VALLEY_SCORE))),
                0.22D,
                1.0D
        );
        DensityFunction summitSuppression = d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.16F, 0.0F, 0.0F),
                p(0.26F, 0.45F, 4.0F),
                p(0.38F, 0.92F, 2.5F),
                p(0.54F, 1.0F, 0.0F),
                p(0.7F, 0.82F, -1.2F),
                p(0.84F, 0.42F, -2.0F),
                p(1.0F, 0.12F, 0.0F)
        ));
        return d.flatCache(d.clamp(
                d.mul(baseTerrace, d.mul(terraceSelector, d.mul(valleySuppression, summitSuppression))),
                0.0D,
                0.34D
        ));
    }

    private static DensityFunction terrace_offset_y(AscDensityDsl d) {
        return d.flatCache(d.mul(
                d.constant(1.75D),
                d.mul(d.ref(MOUNTAIN_WEATHERING_NOISE), d.ref(TERRACE_MASK))
        ));
    }

    private static DensityFunction terrace_input_y(AscDensityDsl d) {
        return d.flatCache(d.add(d.ref(STRUCTURAL_TARGET_Y), d.ref(TERRACE_OFFSET_Y)));
    }

    private static DensityFunction terraced_raw_y(AscDensityDsl d) {
        DensityFunction terraced = new TerraceDensityFunction(d.ref(TERRACE_INPUT_Y), 9.0D, 0.62D, 0.34D);
        return d.flatCache(d.add(terraced, d.mul(d.constant(-1.0D), d.ref(TERRACE_OFFSET_Y))));
    }

    private static DensityFunction terraced_target_y(AscDensityDsl d) {
        DensityFunction terraceDelta = d.add(d.ref(TERRACED_RAW_Y), d.mul(d.constant(-1.0D), d.ref(STRUCTURAL_TARGET_Y)));
        return d.flatCache(d.add(d.ref(STRUCTURAL_TARGET_Y), d.mul(d.ref(TERRACE_MASK), terraceDelta)));
    }

    private static DensityFunction weathering_relief_y(AscDensityDsl d) {
        return d.flatCache(d.mul(
                d.constant(3.75D),
                d.mul(d.ref(MOUNTAIN_WEATHERING_NOISE), d.ref(MOUNTAIN_MASK))
        ));
    }

    private static DensityFunction surface_target_y(AscDensityDsl d) {
        return d.flatCache(d.clamp(
                d.add(d.ref(TERRACED_TARGET_Y), d.ref(WEATHERING_RELIEF_Y)),
                -34.0D,
                396.0D
        ));
    }

    private static DensityFunction mountain_shoulder(AscDensityDsl d) {
        return d.flatCache(d.spline(d.ref(MOUNTAIN_MASK), List.of(
                p(0.0F, 0.0F, 0.0F),
                p(0.12F, 0.0F, 0.0F),
                p(0.22F, 0.25F, 2.4F),
                p(0.34F, 0.72F, 3.0F),
                p(0.48F, 1.0F, 0.0F),
                p(0.62F, 0.9F, -0.85F),
                p(0.78F, 0.62F, -1.45F),
                p(1.0F, 0.32F, 0.0F)
        )));
    }

    private static DensityFunction terrain_slope(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.max(
                d.ref(MOUNTAIN_SHOULDER),
                d.max(d.ref(RIDGE_SCORE), d.mul(d.constant(0.86D), d.ref(VALLEY_SCORE)))
        ), 0.0D, 1.0D));
    }

    private static DensityFunction biome_continentalness(AscDensityDsl d) {
        DensityFunction continent = d.spline(d.ref(CONTINENTS), List.of(
                p(-1.0F, -0.95F, 0.0F),
                p(-0.74F, -0.8F, 0.65F),
                p(-0.54F, -0.58F, 1.0F),
                p(-0.36F, -0.36F, 1.0F),
                p(-0.22F, -0.24F, 0.9F),
                p(-0.12F, -0.15F, 0.85F),
                p(-0.04F, -0.09F, 0.8F),
                p(0.04F, 0.0F, 0.85F),
                p(0.16F, 0.14F, 0.9F),
                p(0.42F, 0.43F, 0.7F),
                p(1.0F, 0.75F, 0.0F)
        ));
        return d.flatCache(d.clamp(d.add(
                continent,
                d.add(d.mul(d.constant(0.24D), d.ref(MOUNTAIN_MASK)), d.mul(d.constant(0.05D), d.ref(MASSIF_SCORE)))
        ), -1.0D, 1.0D));
    }

    private static DensityFunction biome_erosion_surface(AscDensityDsl d) {
        return d.flatCache(d.clamp(d.add(
                d.add(
                        d.add(d.constant(0.6D), d.mul(d.constant(-0.78D), d.ref(MOUNTAIN_MASK))),
                        d.mul(d.constant(-0.2D), d.ref(RIDGE_SCORE))
                ),
                d.add(d.mul(d.constant(-0.1D), d.ref(MASSIF_SCORE)), d.mul(d.constant(0.36D), d.ref(VALLEY_SCORE)))
        ), -1.0D, 1.0D));
    }

    private static DensityFunction biome_weirdness_surface(AscDensityDsl d) {
        DensityFunction nonMountain = d.clamp(d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(MOUNTAIN_MASK))), 0.0D, 1.0D);
        return d.flatCache(d.clamp(d.add(
                d.mul(d.vanillaDensity("overworld/ridges"), nonMountain),
                d.add(
                        d.add(d.mul(d.constant(0.5D), d.ref(MOUNTAIN_MASK)), d.mul(d.constant(0.12D), d.ref(RIDGE_SCORE))),
                        d.add(d.mul(d.constant(0.06D), d.ref(MASSIF_SCORE)), d.mul(d.constant(-0.3D), d.ref(VALLEY_SCORE)))
                )
        ), -1.0D, 1.0D));
    }

    private static DensityFunction base_height(AscDensityDsl d) {
        return d.flatCache(new HeightTargetDensityFunction(d.ref(SURFACE_TARGET_Y), 64.0D, 64.0D));
    }

    private static DensityFunction biome_depth(AscDensityDsl d) {
        return d.clamp(d.add(
                new HeightTargetDensityFunction(d.ref(SURFACE_TARGET_Y), 0.0D, 32.0D),
                d.yGradient(-64, 416, 2.0D, -13.0D)
        ), -1.0D, 1.0D);
    }

    private static DensityFunction biome_surface_depth(AscDensityDsl d) {
        return d.add(
                new HeightTargetDensityFunction(d.ref(SURFACE_TARGET_Y), 0.0D, 64.0D),
                d.yGradient(-64, 416, 1.0D, -6.5D)
        );
    }

    private static DensityFunction final_density(AscDensityDsl d) {
        DensityFunction terrain = d.add(d.yGradient(-64, 416, 2.0D, -5.5D), d.ref(BASE_HEIGHT));
        return d.squeeze(d.mul(d.constant(0.64D), d.interpolated(d.blendDensity(terrain))));
    }

    private static DensityFunction initial_density_without_jaggedness(AscDensityDsl d) {
        return d.clamp(d.add(d.yGradient(-64, 416, 2.0D, -5.5D), d.ref(BASE_HEIGHT)), -64.0D, 64.0D);
    }

    private static DensityFunction biome_surface_proximity(AscDensityDsl d) {
        return d.clamp(d.spline(d.ref(BIOME_SURFACE_DEPTH), List.of(
                p(-8.0F, 1.0F, 0.0F),
                p(0.75F, 1.0F, 0.0F),
                p(1.75F, 0.0F, 0.0F),
                p(8.0F, 0.0F, 0.0F)
        )), 0.0D, 1.0D);
    }

    private static DensityFunction biome_erosion(AscDensityDsl d) {
        DensityFunction nonSurface = d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(BIOME_SURFACE_PROXIMITY)));
        return d.clamp(d.add(
                d.mul(d.ref(BIOME_EROSION_BASE), nonSurface),
                d.mul(d.ref(BIOME_EROSION_SURFACE), d.ref(BIOME_SURFACE_PROXIMITY))
        ), -1.0D, 1.0D);
    }

    private static DensityFunction biome_temperature(AscDensityDsl d) {
        return d.clamp(d.add(
                d.flatCache(d.shiftedNoise2d(d.vanillaDensity("shift_x"), d.vanillaDensity("shift_z"), 0.25D, d.vanillaNoiseKey("temperature"))),
                d.mul(d.yGradient(64, 384, 0.0D, -0.62D), d.ref(BIOME_SURFACE_PROXIMITY))
        ), -1.0D, 1.0D);
    }

    private static DensityFunction biome_weirdness(AscDensityDsl d) {
        DensityFunction nonSurface = d.add(d.constant(1.0D), d.mul(d.constant(-1.0D), d.ref(BIOME_SURFACE_PROXIMITY)));
        return d.clamp(d.add(
                d.mul(d.vanillaDensity("overworld/ridges"), nonSurface),
                d.mul(d.ref(BIOME_WEIRDNESS_SURFACE), d.ref(BIOME_SURFACE_PROXIMITY))
        ), -1.0D, 1.0D);
    }

    private static Point p(float location, float value, float derivative) {
        return new Point(location, value, derivative);
    }

    private static ResourceKey<DensityFunction> key(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, AscensionCraft.prefix("terrain/" + name));
    }
}
