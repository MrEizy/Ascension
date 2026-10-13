package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zic.ascension.worldgen.debug.WorldgenDebugSampler;

import java.util.Locale;

/** Temporary terrain-development commands for worldgen. */
public final class WorldgenDebugCommand {

    private static final int DEFAULT_SCAN_RADIUS = 5_000;
    private static final int DEFAULT_SCAN_STEP = 256;

    private WorldgenDebugCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("worldgen")
                .then(Commands.literal("sample")
                        .executes(WorldgenDebugCommand::sampleCurrent)
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(WorldgenDebugCommand::sampleCoordinates))))
                .then(Commands.literal("scan")
                        .executes(context -> scan(context, DEFAULT_SCAN_RADIUS, DEFAULT_SCAN_STEP))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(256, 50_000))
                                .executes(context -> scan(
                                        context,
                                        IntegerArgumentType.getInteger(context, "radius"),
                                        DEFAULT_SCAN_STEP
                                ))
                                .then(Commands.argument("step", IntegerArgumentType.integer(64, 2_048))
                                        .executes(context -> scan(
                                                context,
                                                IntegerArgumentType.getInteger(context, "radius"),
                                                IntegerArgumentType.getInteger(context, "step")
                                        )))));
    }

    private static int sampleCurrent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return sendSample(context.getSource(), player.getBlockX(), player.getBlockZ(), player.getBlockY());
    }

    private static int sampleCoordinates(CommandContext<CommandSourceStack> context) {
        int x = IntegerArgumentType.getInteger(context, "x");
        int z = IntegerArgumentType.getInteger(context, "z");
        return sendSample(context.getSource(), x, z, null);
    }

    private static int sendSample(CommandSourceStack source, int x, int z, Integer currentProbeY) {
        WorldgenDebugSampler.Sampler sampler = createSampler(source);
        if (sampler == null) {
            return 0;
        }

        WorldgenDebugSampler.TerrainSample sample = sampler.sample(x, z);
        ServerLevel level = source.getLevel();
        int actualY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        WorldgenDebugSampler.GeneratorColumnSample generator = sampler.sampleGeneratorColumn(
                x, z, sample.predictedSurfaceY(), actualY
        );
        int biomeProbeY = actualY - 1;
        WorldgenDebugSampler.BiomeClimateSample climate = sampler.sampleBiomeClimate(x, biomeProbeY, z);
        String biomeId = level.getBiome(new BlockPos(x, biomeProbeY, z))
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("unregistered");

        source.sendSuccess(() -> Component.literal("=== Ascension Worldgen Sample ==="), false);
        source.sendSuccess(() -> Component.literal("XZ: " + x + ", " + z + " | Region: " + sample.regionName()), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Ascension target: %.1f | Generator surface: %s | Actual surface: %d",
                sample.predictedSurfaceY(),
                generator.foundSurface() ? Integer.toString(generator.surfaceY()) : "none",
                actualY
        )), false);
        if (generator.foundSurface()) {
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Target->generator: %+.1f | Generator->actual: %+d",
                    generator.surfaceY() - sample.predictedSurfaceY(),
                    actualY - generator.surfaceY()
            )), false);
        }
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Generator density: target Y%d = %.4f | actual Y%d = %.4f",
                generator.targetProbeY(), generator.densityAtTarget(),
                generator.actualProbeY(), generator.densityAtActual()
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Biome: %s | T %.3f | H %.3f | C %.3f",
                biomeId,
                climate.value("biome_temperature"),
                climate.value("biome_humidity"),
                climate.value("biome_continentalness")
        )), false);
        double weirdness = climate.value("biome_weirdness");
        double peaksAndValleys = peaksAndValleys(weirdness);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Biome climate: E %.3f | D %.3f | W %.3f | PV %.3f | probe Y%d",
                climate.value("biome_erosion"),
                climate.value("biome_depth"),
                weirdness,
                peaksAndValleys,
                biomeProbeY
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Surface biome influence: %.3f | depth below target: %.1f blocks",
                climate.value("biome_surface_proximity"),
                climate.value("biome_surface_depth") * 64.0
        )), false);

        if (currentProbeY != null && Math.abs(currentProbeY - biomeProbeY) > 2) {
            WorldgenDebugSampler.BiomeClimateSample currentClimate = sampler.sampleBiomeClimate(x, currentProbeY, z);
            String currentBiomeId = level.getBiome(new BlockPos(x, currentProbeY, z))
                    .unwrapKey()
                    .map(key -> key.identifier().toString())
                    .orElse("unregistered");
            double currentWeirdness = currentClimate.value("biome_weirdness");
            double currentPeaksAndValleys = peaksAndValleys(currentWeirdness);

            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Current Y%d biome: %s | E %.3f | D %.3f | W %.3f | PV %.3f",
                    currentProbeY,
                    currentBiomeId,
                    currentClimate.value("biome_erosion"),
                    currentClimate.value("biome_depth"),
                    currentWeirdness,
                    currentPeaksAndValleys
            )), false);
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Current surface influence: %.3f | depth below target: %.1f blocks",
                    currentClimate.value("biome_surface_proximity"),
                    currentClimate.value("biome_surface_depth") * 64.0
            )), false);
        }

        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Continent %.3f | Land %.3f | Orogeny %.3f | Province lift %+.1f | Rolling %+.1f",
                sample.value("continents"),
                sample.value("land_mask"),
                sample.value("orogeny"),
                sample.value("province_relief_y"),
                sample.value("lowland_detail_y")
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Province %.3f | Axis |x| %.3f | Range %.3f | Mountain %.3f",
                sample.value("mountain_province"),
                sample.value("mountain_axis_abs"),
                sample.value("range_profile"),
                sample.value("mountain_mask")
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Massif %.3f | Fold %+.1f | Ridge %.3f x%.2f | Valley %.3f | Terrace %.3f",
                sample.value("massif_score"),
                sample.value("fold_relief_y"),
                sample.value("ridge_score"),
                sample.value("ridge_asymmetry"),
                sample.value("valley_score"),
                sample.value("terrace_mask")
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Relief: base %.1f | uplift +%.1f | massif +%.1f | fold %+.1f | ridge +%.1f | valley -%.1f",
                sample.value("base_target_y"),
                sample.value("mountain_uplift_y"),
                sample.value("massif_boost_y"),
                sample.value("fold_relief_y"),
                sample.value("ridge_boost_y"),
                sample.value("valley_cut_y")
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Targets: raw %.1f | carved %.1f | terraced %.1f | weathering %+.1f | final %.1f",
                sample.value("mountain_raw_y"),
                sample.value("structural_target_y"),
                sample.value("terraced_target_y"),
                sample.value("weathering_relief_y"),
                sample.value("surface_target_y")
        )), false);

        return 1;
    }

    private static int scan(CommandContext<CommandSourceStack> context, int radius, int step) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int centerX = player.getBlockX();
        int centerZ = player.getBlockZ();

        WorldgenDebugSampler.Sampler sampler = createSampler(source);
        if (sampler == null) {
            return 0;
        }

        long started = System.nanoTime();
        int samples = 0;
        int landSamples = 0;
        int uplandSamples = 0;
        int mountainInfluenceSamples = 0;
        int mountainCoreSamples = 0;
        int massifSamples = 0;
        int foldedSamples = 0;
        int terraceSamples = 0;
        int valleySamples = 0;
        int above120 = 0;
        int above160 = 0;
        int above220 = 0;
        int above280 = 0;
        int above340 = 0;
        int above380 = 0;
        int above390 = 0;
        int rawAbove300 = 0;
        int rawAbove340 = 0;
        int rawAbove380 = 0;
        int rawAbove400 = 0;

        WorldgenDebugSampler.TerrainSample highest = null;
        WorldgenDebugSampler.TerrainSample highestRaw = null;
        WorldgenDebugSampler.TerrainSample strongestOrogeny = null;
        WorldgenDebugSampler.TerrainSample strongestMountain = null;
        WorldgenDebugSampler.TerrainSample strongestMassif = null;
        WorldgenDebugSampler.TerrainSample strongestValley = null;
        double minLandY = Double.POSITIVE_INFINITY;

        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;

        for (int x = minX; x <= maxX; x += step) {
            for (int z = minZ; z <= maxZ; z += step) {
                WorldgenDebugSampler.TerrainSample sample = sampler.sampleForScan(x, z);
                samples++;

                double land = sample.value("land_mask");
                if (land < 0.35) {
                    continue;
                }

                landSamples++;
                double y = sample.predictedSurfaceY();
                minLandY = Math.min(minLandY, y);

                if (highest == null || y > highest.predictedSurfaceY()) {
                    highest = sample;
                }
                if (highestRaw == null || sample.value("mountain_raw_y") > highestRaw.value("mountain_raw_y")) {
                    highestRaw = sample;
                }
                if (strongestOrogeny == null || sample.value("orogeny") > strongestOrogeny.value("orogeny")) {
                    strongestOrogeny = sample;
                }
                if (strongestMountain == null || sample.value("mountain_mask") > strongestMountain.value("mountain_mask")) {
                    strongestMountain = sample;
                }
                if (strongestMassif == null || sample.value("massif_score") > strongestMassif.value("massif_score")) {
                    strongestMassif = sample;
                }
                if (strongestValley == null || sample.value("valley_score") > strongestValley.value("valley_score")) {
                    strongestValley = sample;
                }

                if (sample.value("province_relief_y") >= 16.0) uplandSamples++;
                if (sample.value("mountain_mask") >= 0.18) mountainInfluenceSamples++;
                if (sample.value("mountain_mask") >= 0.5) mountainCoreSamples++;
                if (sample.value("massif_score") >= 0.45) massifSamples++;
                if (Math.abs(sample.value("fold_relief_y")) >= 6.0) foldedSamples++;
                if (sample.value("terrace_mask") >= 0.10) terraceSamples++;
                if (sample.value("valley_score") >= 0.35) valleySamples++;
                if (y >= 120.0) above120++;
                if (y >= 160.0) above160++;
                if (y >= 220.0) above220++;
                if (y >= 280.0) above280++;
                if (y >= 340.0) above340++;
                if (y >= 380.0) above380++;
                if (y >= 390.0) above390++;

                double rawMountainY = sample.value("mountain_raw_y");
                if (rawMountainY >= 300.0) rawAbove300++;
                if (rawMountainY >= 340.0) rawAbove340++;
                if (rawMountainY >= 380.0) rawAbove380++;
                if (rawMountainY >= 400.0) rawAbove400++;
            }
        }

        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        final int finalSamples = samples;
        final int finalLandSamples = landSamples;
        final int finalUplandSamples = uplandSamples;
        final int finalMountainInfluenceSamples = mountainInfluenceSamples;
        final int finalMountainCoreSamples = mountainCoreSamples;
        final int finalMassifSamples = massifSamples;
        final int finalFoldedSamples = foldedSamples;
        final int finalTerraceSamples = terraceSamples;
        final int finalValleySamples = valleySamples;
        final int finalAbove120 = above120;
        final int finalAbove160 = above160;
        final int finalAbove220 = above220;
        final int finalAbove280 = above280;
        final int finalAbove340 = above340;
        final int finalAbove380 = above380;
        final int finalAbove390 = above390;
        final int finalRawAbove300 = rawAbove300;
        final int finalRawAbove340 = rawAbove340;
        final int finalRawAbove380 = rawAbove380;
        final int finalRawAbove400 = rawAbove400;
        final long finalElapsedMs = elapsedMs;
        final double finalMinLandY = minLandY;

        source.sendSuccess(() -> Component.literal("=== Ascension Worldgen V2.2 Scan ==="), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Center %d,%d | radius %,d | step %d | %,d samples | %d ms",
                centerX, centerZ, radius, step, finalSamples, finalElapsedMs
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Land %,d | upland %.1f%% | mountain influence %.1f%% | core %.1f%% | massif %.1f%% | folded %.1f%% | terraced %.1f%% | valley %.1f%%",
                finalLandSamples,
                percent(finalUplandSamples, finalLandSamples),
                percent(finalMountainInfluenceSamples, finalLandSamples),
                percent(finalMountainCoreSamples, finalLandSamples),
                percent(finalMassifSamples, finalLandSamples),
                percent(finalFoldedSamples, finalLandSamples),
                percent(finalTerraceSamples, finalLandSamples),
                percent(finalValleySamples, finalLandSamples)
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Targets >=Y120: %,d | >=Y160: %,d | >=Y220: %,d | >=Y280: %,d",
                finalAbove120, finalAbove160, finalAbove220, finalAbove280
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "High targets >=Y340: %,d | >=Y380: %,d | >=Y390: %,d",
                finalAbove340, finalAbove380, finalAbove390
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Raw mountain >=Y300: %,d | >=Y340: %,d | >=Y380: %,d | >=Y400: %,d",
                finalRawAbove300, finalRawAbove340, finalRawAbove380, finalRawAbove400
        )), false);

        if (highest != null) {
            WorldgenDebugSampler.TerrainSample result = highest;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Highest target: Y %.1f at %d,%d (%s)",
                    result.predictedSurfaceY(), result.x(), result.z(), result.regionName()
            )), false);
            int teleportY = Math.min(500, Math.max(80, (int) Math.ceil(result.predictedSurfaceY()) + 12));
            source.sendSuccess(() -> Component.literal(
                    "Teleport candidate: /tp @s " + result.x() + " " + teleportY + " " + result.z()
            ), false);
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Land relief: %.1f blocks | min target %.1f",
                    result.predictedSurfaceY() - finalMinLandY,
                    finalMinLandY
            )), false);
        }

        if (highestRaw != null) {
            WorldgenDebugSampler.TerrainSample result = highestRaw;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Highest raw mountain: Y %.1f at %d,%d | carved %.1f | final %.1f",
                    result.value("mountain_raw_y"), result.x(), result.z(),
                    result.value("structural_target_y"), result.predictedSurfaceY()
            )), false);
        }

        if (strongestOrogeny != null) {
            WorldgenDebugSampler.TerrainSample result = strongestOrogeny;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest orogeny: %.3f at %d,%d | province %.3f | target %.1f",
                    result.value("orogeny"), result.x(), result.z(),
                    result.value("mountain_province"), result.predictedSurfaceY()
            )), false);
        }
        if (strongestMountain != null) {
            WorldgenDebugSampler.TerrainSample result = strongestMountain;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest mountain mask: %.3f at %d,%d | range %.3f | target %.1f",
                    result.value("mountain_mask"), result.x(), result.z(),
                    result.value("range_profile"), result.predictedSurfaceY()
            )), false);
        }
        if (strongestMassif != null) {
            WorldgenDebugSampler.TerrainSample result = strongestMassif;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest massif: %.3f at %d,%d | ridge %.3f | target %.1f",
                    result.value("massif_score"), result.x(), result.z(),
                    result.value("ridge_score"), result.predictedSurfaceY()
            )), false);
        }
        if (strongestValley != null) {
            WorldgenDebugSampler.TerrainSample result = strongestValley;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest valley: %.3f at %d,%d | cut %.1f | target %.1f",
                    result.value("valley_score"), result.x(), result.z(),
                    result.value("valley_cut_y"), result.predictedSurfaceY()
            )), false);
        }

        return 1;
    }

    private static WorldgenDebugSampler.Sampler createSampler(CommandSourceStack source) {
        try {
            return WorldgenDebugSampler.create(source.getLevel());
        } catch (RuntimeException exception) {
            source.sendFailure(Component.literal("Worldgen debugger could not create a seeded sampler: " + exception.getMessage()));
            return null;
        }
    }

    private static double peaksAndValleys(double weirdness) {
        return -(Math.abs(Math.abs(weirdness) - 2.0 / 3.0) - 1.0 / 3.0) * 3.0;
    }

    private static double percent(int part, int total) {
        return total <= 0 ? 0.0 : (part * 100.0) / total;
    }
}
