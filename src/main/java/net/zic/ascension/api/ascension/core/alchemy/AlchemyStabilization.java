package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class AlchemyStabilization {
    public static final int PHASE_TICKS = 80;
    public static final int MAX_TARGETS = 12;
    public static final double INTERACTION_REACH = 3.5D;
    public static final double TARGET_HIT_RADIUS = 0.25D;
    public static final double SERVER_AIM_TOLERANCE = 0.36D;
    private static final int[] ALTERNATING_ORDER = {0, 4, 1, 3, 2};
    private static final double[][] POINTS = {{-0.20D, 0.64D}, {0.0D, 0.70D}, {0.20D, 0.64D}, {-0.15D, 0.48D}, {0.15D, 0.48D}};

    private AlchemyStabilization() {
    }

    public enum Pattern {
        SWEEP, ALTERNATING, SHUFFLED;

        public static final Codec<Pattern> CODEC = Codec.STRING.comapFlatMap(value -> {
            try {
                return DataResult.success(Pattern.valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                return DataResult.error(() -> "Unknown stabilization pattern: " + value);
            }
        }, value -> value.name().toLowerCase(Locale.ROOT));
    }

    public record Settings(int targetCount, int prepareTicks, int approachTicks, int intervalTicks, int perfectWindow,
                           int goodWindow, double requiredScore, Pattern pattern) {
        public static final Settings DEFAULT = new Settings(5, 20, 30, 36, 4, 8, 0.70D, Pattern.SHUFFLED);
        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("target_count", DEFAULT.targetCount()).forGetter(Settings::targetCount),
                Codec.INT.optionalFieldOf("prepare_ticks", DEFAULT.prepareTicks()).forGetter(Settings::prepareTicks),
                Codec.INT.optionalFieldOf("approach_ticks", DEFAULT.approachTicks()).forGetter(Settings::approachTicks),
                Codec.INT.optionalFieldOf("interval_ticks", DEFAULT.intervalTicks()).forGetter(Settings::intervalTicks),
                Codec.INT.optionalFieldOf("perfect_window", DEFAULT.perfectWindow()).forGetter(Settings::perfectWindow),
                Codec.INT.optionalFieldOf("good_window", DEFAULT.goodWindow()).forGetter(Settings::goodWindow),
                Codec.DOUBLE.optionalFieldOf("required_score", DEFAULT.requiredScore()).forGetter(Settings::requiredScore),
                Pattern.CODEC.optionalFieldOf("pattern", Pattern.SHUFFLED).forGetter(Settings::pattern)
        ).apply(instance, Settings::new));

        public Settings {
            targetCount = Mth.clamp(targetCount, 1, MAX_TARGETS);
            prepareTicks = Mth.clamp(prepareTicks, 0, 60);
            approachTicks = Mth.clamp(approachTicks, 12, 50);
            intervalTicks = Mth.clamp(intervalTicks, 12, 60);
            perfectWindow = Mth.clamp(perfectWindow, 1, 8);
            goodWindow = Mth.clamp(goodWindow, perfectWindow, 12);
            requiredScore = Double.isFinite(requiredScore) ? Mth.clamp(requiredScore, 0.0D, 1.0D) : 0.70D;
            pattern = pattern == null ? Pattern.SHUFFLED : pattern;
        }

        public int hitTick(int index) {
            return prepareTicks + approachTicks + index * intervalTicks;
        }

        public int durationTicks() {
            return hitTick(targetCount - 1) + goodWindow + 12;
        }
    }

    public static int targetPosition(Settings settings, long seed, int index) {
        int offset = Math.floorMod(seed, POINTS.length);
        return switch (settings.pattern()) {
            case SWEEP -> (offset + index) % POINTS.length;
            case ALTERNATING -> (offset + ALTERNATING_ORDER[index % POINTS.length]) % POINTS.length;
            case SHUFFLED -> {
                int start = Math.floorMod(seed ^ (seed >>> 17), POINTS.length);
                int step = 1 + Math.floorMod(seed >>> 8, POINTS.length - 1);
                yield (start + index * step) % POINTS.length;
            }
        };
    }

    public static Vec3 position(BlockPos pos, BlockState state, int target) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        double lateral = POINTS[Math.floorMod(target, POINTS.length)][0];
        double vertical = POINTS[Math.floorMod(target, POINTS.length)][1];
        double forward = -0.435D;
        return new Vec3(pos.getX() + 0.5D - facing.getStepZ() * lateral + facing.getStepX() * forward,
                pos.getY() + vertical,
                pos.getZ() + 0.5D + facing.getStepX() * lateral + facing.getStepZ() * forward);
    }

    public static int points(Settings settings, int index, int tick) {
        int offset = Math.abs(tick - settings.hitTick(index));
        if (offset <= settings.perfectWindow()) return 100;
        if (offset <= settings.goodWindow()) return 65;
        return 0;
    }
}
