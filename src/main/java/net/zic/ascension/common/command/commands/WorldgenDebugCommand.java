package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zic.ascension.worldgen.debug.WorldgenDebugSampler;

import java.util.Locale;

/** Temporary terrain-development commands for worldgen */
public final class WorldgenDebugCommand {

    private static final int DEFAULT_SCAN_RADIUS = 5_000;
    private static final int DEFAULT_SCAN_STEP = 128;

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
                        .then(Commands.argument("radius", IntegerArgumentType.integer(256, 20_000))
                                .executes(context -> scan(
                                        context,
                                        IntegerArgumentType.getInteger(context, "radius"),
                                        DEFAULT_SCAN_STEP
                                ))
                                .then(Commands.argument("step", IntegerArgumentType.integer(64, 1_024))
                                        .executes(context -> scan(
                                                context,
                                                IntegerArgumentType.getInteger(context, "radius"),
                                                IntegerArgumentType.getInteger(context, "step")
                                        )))));
    }

    private static int sampleCurrent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return sendSample(context.getSource(), player.getBlockX(), player.getBlockZ(), true);
    }

    private static int sampleCoordinates(CommandContext<CommandSourceStack> context) {
        int x = IntegerArgumentType.getInteger(context, "x");
        int z = IntegerArgumentType.getInteger(context, "z");
        return sendSample(context.getSource(), x, z, true);
    }

    private static int sendSample(CommandSourceStack source, int x, int z, boolean includeActualSurface) {
        WorldgenDebugSampler.TerrainSample sample = WorldgenDebugSampler.sample(source.registryAccess(), x, z);

        source.sendSuccess(() -> Component.literal("=== Ascension Worldgen Sample ==="), false);
        source.sendSuccess(() -> Component.literal("XZ: " + x + ", " + z + " | Region: " + sample.regionName()), false);

        if (includeActualSurface) {
            ServerLevel level = source.getLevel();
            int actualY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Target surface: %.1f | Actual surface: %d | Delta: %+.1f",
                    sample.predictedSurfaceY(), actualY, actualY - sample.predictedSurfaceY()
            )), false);
        } else {
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Target surface: %.1f",
                    sample.predictedSurfaceY()
            )), false);
        }

        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Land %.3f | Orogeny %.3f | Province %.3f | Axis |x| %.3f",
                sample.value("land_mask"),
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
                "Targets: low %.1f | plateau %.1f | mountain %.1f (raw %.1f)",
                sample.value("lowland_target_y"),
                sample.value("plateau_target_y"),
                sample.value("mountain_eroded_y"),
                sample.value("mountain_target_uncarved_y")
        )), false);

        return 1;
    }

    private static int scan(CommandContext<CommandSourceStack> context, int radius, int step) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int centerX = player.getBlockX();
        int centerZ = player.getBlockZ();

        long started = System.nanoTime();
        int samples = 0;
        int landSamples = 0;
        int mountainSamples = 0;
        int above200 = 0;
        int above250 = 0;
        int above280 = 0;

        WorldgenDebugSampler.TerrainSample highest = null;
        WorldgenDebugSampler.TerrainSample strongestCore = null;
        WorldgenDebugSampler.TerrainSample strongestHero = null;
        double minLandY = Double.POSITIVE_INFINITY;

        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;

        for (int x = minX; x <= maxX; x += step) {
            for (int z = minZ; z <= maxZ; z += step) {
                WorldgenDebugSampler.TerrainSample sample = WorldgenDebugSampler.sample(source.registryAccess(), x, z);
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
                if (strongestCore == null || sample.value("core_mask") > strongestCore.value("core_mask")) {
                    strongestCore = sample;
                }
                if (strongestHero == null || sample.value("hero_score") > strongestHero.value("hero_score")) {
                    strongestHero = sample;
                }

                if (sample.value("core_mask") >= 0.45) {
                    mountainSamples++;
                }
                if (y >= 200.0) above200++;
                if (y >= 250.0) above250++;
                if (y >= 280.0) above280++;
            }
        }

        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        final int finalSamples = samples;
        final int finalLandSamples = landSamples;
        final int finalMountainSamples = mountainSamples;
        final int finalAbove200 = above200;
        final int finalAbove250 = above250;
        final int finalAbove280 = above280;
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
                "Land %,d | mountain-core %,d (%.1f%% of land)",
                finalLandSamples,
                finalMountainSamples,
                percent(finalMountainSamples, finalLandSamples)
        )), false);
        source.sendSuccess(() -> Component.literal(String.format(
                Locale.ROOT,
                "Land targets >=Y200: %,d | >=Y250: %,d | >=Y280: %,d",
                finalAbove200, finalAbove250, finalAbove280
        )), false);

        if (highest != null) {
            WorldgenDebugSampler.TerrainSample result = highest;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Highest target: Y %.1f at %d,%d (%s)",
                    result.predictedSurfaceY(), result.x(), result.z(), result.regionName()
            )), false);
            source.sendSuccess(() -> Component.literal(
                    "Teleport candidate: /tp @s " + result.x() + " 300 " + result.z()
            ), false);
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Land relief in scan: %.1f blocks (min target %.1f)",
                    result.predictedSurfaceY() - finalMinLandY,
                    finalMinLandY
            )), false);
        }

        if (strongestCore != null) {
            WorldgenDebugSampler.TerrainSample result = strongestCore;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest core: %.3f at %d,%d | target %.1f",
                    result.value("core_mask"), result.x(), result.z(), result.predictedSurfaceY()
            )), false);
        }
        if (strongestHero != null) {
            WorldgenDebugSampler.TerrainSample result = strongestHero;
            source.sendSuccess(() -> Component.literal(String.format(
                    Locale.ROOT,
                    "Strongest hero: %.3f at %d,%d | target %.1f",
                    result.value("hero_score"), result.x(), result.z(), result.predictedSurfaceY()
            )), false);
        }

        return 1;
    }

    private static double percent(int part, int total) {
        return total <= 0 ? 0.0 : (part * 100.0) / total;
    }
}
