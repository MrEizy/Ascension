package net.zic.ascension.impl.core.innerworld;

import net.minecraft.util.Mth;

/** Deterministic value noise + hashing. Pure functions of (seed, coordinates), so rings can be generated in any order. */
final class InnerWorldNoise {
    private InnerWorldNoise() {
    }

    static double hash(long seed, int x, int z) {
        long h = seed ^ (x * 341873128712L) ^ (z * 132897987541L);
        h *= 0x9E3779B97F4A7C15L;
        h ^= (h >>> 32);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 29);
        return ((h >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
    }

    static double hash3(long seed, int x, int y, int z) {
        long h = seed ^ (x * 341873128712L) ^ (y * 132897987541L) ^ (z * 1442695040888963407L);
        h *= 0x9E3779B97F4A7C15L;
        h ^= (h >>> 32);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 29);
        return ((h >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
    }

    static double smoothstep(double edge0, double edge1, double value) {
        double t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    static double noise(long seed, double x, double z) {
        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        double u = fx * fx * (3.0 - 2.0 * fx);
        double v = fz * fz * (3.0 - 2.0 * fz);
        double a = hash(seed, x0, z0);
        double b = hash(seed, x0 + 1, z0);
        double c = hash(seed, x0, z0 + 1);
        double d = hash(seed, x0 + 1, z0 + 1);
        return Mth.lerp(v, Mth.lerp(u, a, b), Mth.lerp(u, c, d));
    }

    static double noise3(long seed, double x, double y, double z) {
        int x0 = Mth.floor(x);
        int y0 = Mth.floor(y);
        int z0 = Mth.floor(z);
        double fx = x - x0;
        double fy = y - y0;
        double fz = z - z0;
        double u = fx * fx * (3.0 - 2.0 * fx);
        double v = fy * fy * (3.0 - 2.0 * fy);
        double w = fz * fz * (3.0 - 2.0 * fz);
        double c00 = Mth.lerp(u, hash3(seed, x0, y0, z0), hash3(seed, x0 + 1, y0, z0));
        double c10 = Mth.lerp(u, hash3(seed, x0, y0 + 1, z0), hash3(seed, x0 + 1, y0 + 1, z0));
        double c01 = Mth.lerp(u, hash3(seed, x0, y0, z0 + 1), hash3(seed, x0 + 1, y0, z0 + 1));
        double c11 = Mth.lerp(u, hash3(seed, x0, y0 + 1, z0 + 1), hash3(seed, x0 + 1, y0 + 1, z0 + 1));
        return Mth.lerp(w, Mth.lerp(v, c00, c10), Mth.lerp(v, c01, c11));
    }
}