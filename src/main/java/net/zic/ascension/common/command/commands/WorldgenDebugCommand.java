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
        return sendSample(context.getSource(), player.getBlockX(), player.getBlockZ());
    }

    private static int sampleCoordinates(CommandContext<CommandSourceStack> context) {
        int x = IntegerArgumentType.getInteger(context, "x");
        int z = IntegerArgumentType.getInteger(context, "z");
        return sendSample(context.getSource(), x, z);
    }

    private static int sendSample(CommandSourceStack source, int x, int z) {
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
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Biome climate: E %.3f | D %.3f | W %.3f | probe Y%d",
                climate.value("biome_erosion"),
                climate.value("biome_depth"),
                climate.value("biome_weirdness"),
                biomeProbeY
        )), false);

        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Land %.3f | Land gate %.3f | Orogeny %.3f | Province %.3f | Axis |x| %.3f",
                sample.value("land_mask"),
                sample.value("mountain_land_gate"),
                sample.value("orogeny"),
                sample.value("mountain_province"),
                sample.value("mountain_axis_abs")
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Foothill %.3f | Core %.3f | Ridge %.3f | Massif %.3f",
                sample.value("foothill_mask"),
                sample.value("core_mask"),
                sample.value("ridge_score"),
                sample.value("massif_noise")
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Hero gate %.3f | Hero score %.3f | Valley %.3f | Plateau %.3f",
                sample.value("hero_gate"),
                sample.value("hero_score"),
                sample.value("valley_score"),
                sample.value("plateau_mask")
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Targets: low %.1f | plateau %.1f | mountain %.1f | pre-cap %.1f | final %.1f",
                sample.value("lowland_target_y"),
                sample.value("plateau_target_y"),
                sample.value("mountain_eroded_y"),
                sample.value("mountain_target_raw_y"),
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
        int foothillSamples = 0;
        int mountainSamples = 0;
        int massifSamples = 0;
        int heroSamples = 0;
        int above200 = 0;
        int above250 = 0;
        int above300 = 0;
        int above340 = 0;
        int above370 = 0;
        int above400 = 0;
        int above450 = 0;
        int rawAbove320 = 0;
        int rawAbove360 = 0;
        int rawAbove400 = 0;
        int rawAbove450 = 0;

        WorldgenDebugSampler.TerrainSample highest = null;
        WorldgenDebugSampler.TerrainSample highestRaw = null;
        WorldgenDebugSampler.TerrainSample strongestOrogeny = null;
        WorldgenDebugSampler.TerrainSample strongestCore = null;
        WorldgenDebugSampler.TerrainSample strongestHero = null;
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
                if (highestRaw == null || sample.value("mountain_target_raw_y") > highestRaw.value("mountain_target_raw_y")) {
                    highestRaw = sample;
                }
                if (strongestOrogeny == null || sample.value("orogeny") > strongestOrogeny.value("orogeny")) {
                    strongestOrogeny = sample;
                }
                if (strongestCore == null || sample.value("core_mask") > strongestCore.value("core_mask")) {
                    strongestCore = sample;
                }
                if (strongestHero == null || sample.value("hero_score") > strongestHero.value("hero_score")) {
                    strongestHero = sample;
                }

                if (sample.value("foothill_mask") >= 0.34) foothillSamples++;
                if (sample.value("core_mask") >= 0.50) mountainSamples++;
                if (sample.value("core_mask") >= 0.62 && sample.value("massif_noise") >= 0.55) massifSamples++;
                if (sample.value("hero_score") >= 0.50) heroSamples++;
                if (y >= 200.0) above200++;
                if (y >= 250.0) above250++;
                if (y >= 300.0) above300++;
                if (y >= 340.0) above340++;
                if (y >= 370.0) above370++;
                if (y >= 400.0) above400++;
                if (y >= 450.0) above450++;

                double rawMountainY = sample.value("mountain_target_raw_y");
                if (rawMountainY >= 320.0) rawAbove320++;
                if (rawMountainY >= 360.0) rawAbove360++;
                if (rawMountainY >= 400.0) rawAbove400++;
                if (rawMountainY >= 450.0) rawAbove450++;
            }
        }

        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        final int finalSamples = samples;
        final int finalLandSamples = landSamples;
        final int finalFoothillSamples = foothillSamples;
        final int finalMountainSamples = mountainSamples;
        final int finalMassifSamples = massifSamples;
        final int finalHeroSamples = heroSamples;
        final int finalAbove200 = above200;
        final int finalAbove250 = above250;
        final int finalAbove300 = above300;
        final int finalAbove340 = above340;
        final int finalAbove370 = above370;
        final int finalAbove400 = above400;
        final int finalAbove450 = above450;
        final int finalRawAbove320 = rawAbove320;
        final int finalRawAbove360 = rawAbove360;
        final int finalRawAbove400 = rawAbove400;
        final int finalRawAbove450 = rawAbove450;
        final long finalElapsedMs = elapsedMs;
        final double finalMinLandY = minLandY;

        source.sendSuccess(() -> Component.literal("=== Ascension Worldgen Scan ==="), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Center %d,%d | radius %,d | step %d | %,d samples | %d ms",
                centerX, centerZ, radius, step, finalSamples, finalElapsedMs
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Land %,d | influence %.1f%% | core %.1f%% | massif %.1f%% | hero %.2f%%",
                finalLandSamples,
                percent(finalFoothillSamples, finalLandSamples),
                percent(finalMountainSamples, finalLandSamples),
                percent(finalMassifSamples, finalLandSamples),
                percent(finalHeroSamples, finalLandSamples)
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Targets >=Y200: %,d | >=Y250: %,d | >=Y300: %,d | >=Y340: %,d",
                finalAbove200, finalAbove250, finalAbove300, finalAbove340
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "High targets >=Y370: %,d | >=Y400: %,d | >=Y450: %,d",
                finalAbove370, finalAbove400, finalAbove450
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Raw mountain >=Y320: %,d | >=Y360: %,d | >=Y400: %,d | >=Y450: %,d",
                finalRawAbove320, finalRawAbove360, finalRawAbove400, finalRawAbove450
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
                    "Highest raw mountain target: Y %.1f at %d,%d | final %.1f",
                    result.value("mountain_target_raw_y"), result.x(), result.z(), result.predictedSurfaceY()
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
        if (strongestCore != null) {
            WorldgenDebugSampler.TerrainSample result = strongestCore;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest core: %.3f at %d,%d | axis %.3f | target %.1f",
                    result.value("core_mask"), result.x(), result.z(),
                    result.value("mountain_axis_abs"), result.predictedSurfaceY()
            )), false);
        }
        if (strongestHero != null) {
            WorldgenDebugSampler.TerrainSample result = strongestHero;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest hero: %.3f at %d,%d | gate %.3f | target %.1f",
                    result.value("hero_score"), result.x(), result.z(),
                    result.value("hero_gate"), result.predictedSurfaceY()
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

    private static double percent(int part, int total) {
        return total <= 0 ? 0.0 : (part * 100.0) / total;
    }
}
