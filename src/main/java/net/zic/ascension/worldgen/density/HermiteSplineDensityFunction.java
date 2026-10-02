package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Comparator;
import java.util.List;

public record HermiteSplineDensityFunction(
        DensityFunction coordinate,
        List<Point> points
) implements DensityFunction {

    public record Point(float location, float value, float derivative) {
        public static final Codec<Point> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("location").forGetter(Point::location),
                Codec.FLOAT.fieldOf("value").forGetter(Point::value),
                Codec.FLOAT.fieldOf("derivative").forGetter(Point::derivative)
        ).apply(instance, Point::new));
    }

    public static final MapCodec<HermiteSplineDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("coordinate").forGetter(HermiteSplineDensityFunction::coordinate),
                    Point.CODEC.listOf().fieldOf("points").forGetter(HermiteSplineDensityFunction::points)
            ).apply(instance, HermiteSplineDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<HermiteSplineDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    public HermiteSplineDensityFunction {
        if (points.size() < 2) {
            throw new IllegalArgumentException("Hermite spline requires at least two points");
        }
        points = points.stream()
                .sorted(Comparator.comparingDouble(Point::location))
                .toList();
    }

    @Override
    public double compute(FunctionContext context) {
        return sample(coordinate.compute(context));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        coordinate.fillArray(values, contextProvider);
        for (int i = 0; i < values.length; i++) {
            values[i] = sample(values[i]);
        }
    }

    private double sample(double x) {
        Point first = points.getFirst();
        if (x <= first.location()) {
            return first.value() + first.derivative() * (x - first.location());
        }

        Point last = points.getLast();
        if (x >= last.location()) {
            return last.value() + last.derivative() * (x - last.location());
        }

        for (int i = 0; i < points.size() - 1; i++) {
            Point a = points.get(i);
            Point b = points.get(i + 1);
            if (x <= b.location()) {
                return sampleSegment(a, b, x);
            }
        }

        return last.value();
    }

    private static double sampleSegment(Point a, Point b, double x) {
        double width = b.location() - a.location();
        if (Math.abs(width) < 1.0E-7) {
            return b.value();
        }

        double t = (x - a.location()) / width;
        double t2 = t * t;
        double t3 = t2 * t;

        double h00 = 2.0 * t3 - 3.0 * t2 + 1.0;
        double h10 = t3 - 2.0 * t2 + t;
        double h01 = -2.0 * t3 + 3.0 * t2;
        double h11 = t3 - t2;

        return h00 * a.value()
                + h10 * width * a.derivative()
                + h01 * b.value()
                + h11 * width * b.derivative();
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new HermiteSplineDensityFunction(coordinate.mapAll(visitor), points);
    }

    @Override
    public double minValue() {
        return bounds()[0];
    }

    @Override
    public double maxValue() {
        return bounds()[1];
    }

    private double[] bounds() {
        double coordinateMin = coordinate.minValue();
        double coordinateMax = coordinate.maxValue();
        if (!Double.isFinite(coordinateMin) || !Double.isFinite(coordinateMax)) {
            double min = points.stream().mapToDouble(Point::value).min().orElse(-1.0);
            double max = points.stream().mapToDouble(Point::value).max().orElse(1.0);
            return new double[]{min - 4096.0, max + 4096.0};
        }

        if (coordinateMin > coordinateMax) {
            double swap = coordinateMin;
            coordinateMin = coordinateMax;
            coordinateMax = swap;
        }

        double min = Math.min(sample(coordinateMin), sample(coordinateMax));
        double max = Math.max(sample(coordinateMin), sample(coordinateMax));

        for (Point point : points) {
            if (point.location() >= coordinateMin && point.location() <= coordinateMax) {
                min = Math.min(min, point.value());
                max = Math.max(max, point.value());
            }
        }

        for (int i = 0; i < points.size() - 1; i++) {
            Point a = points.get(i);
            Point b = points.get(i + 1);
            double x0 = Math.max(coordinateMin, a.location());
            double x1 = Math.min(coordinateMax, b.location());
            if (x0 >= x1) {
                continue;
            }

            double width = b.location() - a.location();
            double cubicA = 2.0 * a.value() - 2.0 * b.value() + width * (a.derivative() + b.derivative());
            double cubicB = -3.0 * a.value() + 3.0 * b.value() - width * (2.0 * a.derivative() + b.derivative());
            double cubicC = width * a.derivative();

            double tMin = (x0 - a.location()) / width;
            double tMax = (x1 - a.location()) / width;
            double[] roots = derivativeRoots(3.0 * cubicA, 2.0 * cubicB, cubicC);
            for (double t : roots) {
                if (Double.isFinite(t) && t > tMin && t < tMax) {
                    double value = sampleSegment(a, b, a.location() + t * width);
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
            }
        }

        return new double[]{min, max};
    }

    private static double[] derivativeRoots(double a, double b, double c) {
        if (Math.abs(a) < 1.0E-12) {
            if (Math.abs(b) < 1.0E-12) {
                return new double[0];
            }
            return new double[]{-c / b};
        }

        double discriminant = b * b - 4.0 * a * c;
        if (discriminant < 0.0) {
            return new double[0];
        }

        double root = Math.sqrt(discriminant);
        return new double[]{(-b - root) / (2.0 * a), (-b + root) / (2.0 * a)};
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
