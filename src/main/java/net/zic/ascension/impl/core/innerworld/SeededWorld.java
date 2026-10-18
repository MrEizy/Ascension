package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.zic.ascension.common.blocks.ModBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Feature;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.FeatureType;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Palette;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Terrain;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Variant;

/**
 * The seeded terrain generator for one player's plot. Everything is a pure function of (seed, x, z, y), so
 * chunks and rings can be generated in any order and always line up.
 *
 * Layout: the plot is tiled with jittered cells (~250 blocks). Each cell is assigned one of the seed's 3-5
 * themes by weighted roll, so theme area follows path weight. A fixed core around the dais always belongs to
 * theme 0 (the strongest path's heaviest variant). Near a border between two regions the column's terrain
 * height is blended and its palette/features are chosen by a per-column dither, which gives the soft border.
 *
 * Every column is self-contained (nothing writes into a neighbouring column), so the budgeted per-tick column
 * loop in InnerWorldGrowth can stop and resume anywhere.
 */
final class SeededWorld {
    private static final int SET_FLAGS = 18;
    private static final double DAIS_RADIUS = 5.5;
    private static final double BLEND_WIDTH = 36.0;
    private static final double CORE_CLEAR_RADIUS = 140.0;

    private record Theme(Variant variant, double weight, Identifier path) {
    }

    private final long seed;
    private final List<Theme> themes = new ArrayList<>();
    private final double totalWeight;
    private final int cell;
    private final double ampJitter;
    private final double freqJitter;
    private final double decorJitter;

    static SeededWorld build(RegistryAccess access, InnerWorldSeed.Seed seed) {
        List<Theme> resolved = new ArrayList<>();
        var lookup = access.lookupOrThrow(InnerWorldProfiles.REGISTRY);
        for (InnerWorldSeed.ThemeRef ref : seed.themes) {
            var holder = lookup.get(ResourceKey.create(InnerWorldProfiles.REGISTRY, Identifier.parse(ref.profile)));
            if (holder.isEmpty()) {
                continue;
            }
            List<Variant> variants = holder.get().value().variants();
            if (ref.variant < 0 || ref.variant >= variants.size()) {
                continue;
            }
            resolved.add(new Theme(variants.get(ref.variant), Math.max(0.01, ref.weight), holder.get().value().path()));
        }
        if (resolved.isEmpty()) {
            resolved.add(new Theme(Variant.fallback(), 1.0, null));
        }
        return new SeededWorld(seed.value, resolved);
    }

    private SeededWorld(long seed, List<Theme> resolved) {
        this.seed = seed;
        this.themes.addAll(resolved);
        double sum = 0.0;
        for (Theme theme : themes) {
            sum += theme.weight();
        }
        this.totalWeight = sum;
        this.cell = (int) (250 * (0.85 + 0.3 * InnerWorldNoise.hash(seed + 9, 0, 0)));
        this.ampJitter = 0.75 + InnerWorldNoise.hash(seed + 101, 1, 1) * 0.5;
        this.freqJitter = 0.8 + InnerWorldNoise.hash(seed + 102, 2, 2) * 0.5;
        this.decorJitter = 0.5 + InnerWorldNoise.hash(seed + 103, 3, 3) * 1.0;
    }

    private int themeForCell(int cx, int cz) {
        double roll = InnerWorldNoise.hash(seed + 5, cx, cz) * totalWeight;
        for (int i = 0; i < themes.size(); i++) {
            roll -= themes.get(i).weight();
            if (roll <= 0.0) {
                return i;
            }
        }
        return themes.size() - 1;
    }

    private double height(Theme theme, int lx, int lz, double dist) {
        Terrain t = theme.variant().terrain();
        double f = t.frequency() * freqJitter;
        double n = InnerWorldNoise.noise(seed, lx / (48.0 / f), lz / (48.0 / f)) * 0.6
                + InnerWorldNoise.noise(seed + 1, lx / (20.0 / f), lz / (20.0 / f)) * 0.3
                + InnerWorldNoise.noise(seed + 2, lx / (7.0 / f), lz / (7.0 / f)) * 0.1;
        double blend = InnerWorldNoise.smoothstep(6.0, 18.0, dist);
        double rel = t.offset() * blend + (n - 0.5) * t.amplitude() * ampJitter * blend;
        if (t.terrace() > 0) {
            rel = Math.round(rel / t.terrace()) * (double) t.terrace();
        }
        return InnerWorld.SURFACE_Y + rel;
    }

    private record Region(Theme a, Theme b, double wa, Theme dominant) {
    }

    /** Which two regions a point sits between, how strongly, and which one wins the per-column dither. */
    private Region regionAt(int lx, int lz, double dist) {
        // The origin is a site for theme 0; real cell sites inside the core are ignored.
        int gx = Math.floorDiv(lx, cell);
        int gz = Math.floorDiv(lz, cell);
        double d1 = dist;
        double d2 = Double.MAX_VALUE;
        int t1 = 0;
        int t2 = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = gx + dx;
                int cz = gz + dz;
                double sx = (cx + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 3, cx, cz)) * cell;
                double sz = (cz + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 4, cx, cz)) * cell;
                if (Math.hypot(sx, sz) < CORE_CLEAR_RADIUS) {
                    continue;
                }
                double d = Math.hypot(lx - sx, lz - sz);
                int t = themeForCell(cx, cz);
                if (d < d1) {
                    d2 = d1;
                    t2 = t1;
                    d1 = d;
                    t1 = t;
                } else if (d < d2) {
                    d2 = d;
                    t2 = t;
                }
            }
        }
        Theme a = themes.get(t1);
        Theme b = d2 == Double.MAX_VALUE ? a : themes.get(t2);
        double wa = d2 == Double.MAX_VALUE ? 1.0 : 0.5 + 0.5 * InnerWorldNoise.smoothstep(0.0, BLEND_WIDTH, d2 - d1);
        Theme dominant = InnerWorldNoise.hash(seed + 11, lx, lz) < wa ? a : b;

        return new Region(a, b, wa, dominant);
    }

    /** Atmosphere biome of the region at plot-local coordinates, if that theme's variant names one. */
    Optional<Identifier> biomeAt(int lx, int lz) {
        double dist = Math.sqrt((double) lx * lx + (double) lz * lz);
        return regionAt(lx, lz, dist).dominant().variant().biome();
    }

    void fillColumn(ServerLevel level, int x, int z, int centerX, int centerZ) {
        final int lx = x - centerX;
        final int lz = z - centerZ;
        final double dist = Math.sqrt((double) lx * lx + (double) lz * lz);
        final boolean dais = dist <= DAIS_RADIUS;

        Region region = regionAt(lx, lz, dist);
        Theme a = region.a();
        Theme b = region.b();
        double wa = region.wa();
        Theme dominant = region.dominant();

        double blended = a == b ? height(a, lx, lz, dist) : wa * height(a, lx, lz, dist) + (1.0 - wa) * height(b, lx, lz, dist);
        int groundTop = (int) Math.round(blended);

        Variant variant = dominant.variant();
        Palette palette = variant.palette();
        List<Feature> features = variant.features();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // ---- pools: lower the ground and flood it
        final int waterLevel = InnerWorld.SURFACE_Y - 2;
        Feature poolFeature = dais || dist <= 16.0 ? null : poolFeatureAt(variant, lx, lz);
        boolean pool = poolFeature != null;
        Block fluid = pool ? fluidOf(poolFeature) : Blocks.WATER;
        if (pool) {
            groundTop = Math.min(groundTop, waterLevel - 2);
        }

        // ---- raised features: spires, pillars, mesas
        int top = groundTop;
        int raiseMode = 0; // 1 = spire/pillar material, 2 = mesa (palette top + body)
        Block raiseBlock = palette.stone();
        if (!pool && !dais && dist > 14.0) {
            for (int i = 0; i < features.size(); i++) {
                Feature f = features.get(i);
                if (f.type() != FeatureType.SPIRES && f.type() != FeatureType.PILLARS && f.type() != FeatureType.MESAS) {
                    continue;
                }
                int raised = gridRaise(f, i, groundTop, lx, lz);
                if (raised > top) {
                    top = raised;
                    if (f.type() == FeatureType.MESAS) {
                        raiseMode = 2;
                    } else {
                        raiseMode = 1;
                        raiseBlock = f.block().orElse(palette.stone());
                    }
                }
            }
        }

        // ---- the column itself
        int thickness = 24 + (int) (InnerWorldNoise.noise(seed + 3, lx / 30.0, lz / 30.0) * 10.0);
        int bottom = groundTop - thickness;
        List<Block> bands = palette.bands();
        for (int y = bottom; y <= top; y++) {
            BlockState state;
            if (y > groundTop) {
                if (raiseMode == 2) {
                    state = (y == top ? palette.top() : bands.isEmpty() ? palette.stone() : bands.get(Math.floorMod(y, bands.size()))).defaultBlockState();
                } else {
                    state = raiseBlock.defaultBlockState();
                }
            } else if (y >= groundTop - 3) {
                state = dais ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
                        : (y == groundTop ? palette.top() : palette.sub()).defaultBlockState();
            } else if (y <= bottom + 5) {
                state = palette.deep().defaultBlockState();
            } else if (!bands.isEmpty()) {
                state = bands.get(Math.floorMod(y, bands.size())).defaultBlockState();
            } else {
                state = palette.stone().defaultBlockState();
            }
            level.setBlock(pos.set(x, y, z), state, SET_FLAGS);
        }

        if (pool) {
            BlockState fluidState = fluid.defaultBlockState();
            for (int y = groundTop + 1; y <= waterLevel; y++) {
                level.setBlock(pos.set(x, y, z), fluidState, SET_FLAGS);
            }
        }

        // ---- ores and treasure veins (stone only, below the surface layers)
        if (!dais) {
            placeOres(level, pos, variant, x, z, lx, lz, groundTop, bottom);
            placeTreasure(level, pos, x, z, lx, lz);
        }

        // ---- carving: caves and ravines
        boolean carved = false;
        if (!dais && dist > 12.0) {
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int i = 0; i < features.size(); i++) {
                Feature f = features.get(i);
                if (f.type() == FeatureType.CAVES) {
                    double w = 0.035 + 0.05 * Math.min(1.0, f.density());
                    for (int y = bottom + 7; y <= groundTop - 4; y++) {
                        double c1 = InnerWorldNoise.noise3(seed + 81, lx / 30.0, y / 14.0, lz / 30.0);
                        double c2 = InnerWorldNoise.noise3(seed + 82, lx / 30.0, y / 14.0, lz / 30.0);
                        if (Math.abs(c1 - 0.5) < w && Math.abs(c2 - 0.5) < w) {
                            level.setBlock(pos.set(x, y, z), air, SET_FLAGS);
                        }
                    }
                } else if (f.type() == FeatureType.RAVINES && !pool) {
                    double ridge = Math.abs(InnerWorldNoise.noise(seed + 70, lx / 70.0, lz / 70.0) - 0.5);
                    double width = 0.012 + 0.012 * Math.min(1.0, f.density());
                    if (ridge < width) {
                        double depth = Mth.lerp(1.0 - ridge / width, f.minHeight(), f.maxHeight());
                        for (int y = groundTop - (int) depth; y <= top; y++) {
                            level.setBlock(pos.set(x, y, z), air, SET_FLAGS);
                        }
                        carved = true;
                    }
                }
            }
        }

        // ---- floating islands (above the column, independent of the ground)
        if (dist > 40.0) {
            for (int i = 0; i < features.size(); i++) {
                if (features.get(i).type() == FeatureType.FLOATING_ISLANDS) {
                    islands(level, pos, features.get(i), i, palette, lx, lz, x, z);
                }
            }
        }

        // ---- decoration
        if (!dais && !pool && !carved && raiseMode != 1 && !palette.decor().isEmpty()) {
            double roll = InnerWorldNoise.hash(seed + 7, lx, lz) / decorJitter;
            double acc = 0.0;
            for (InnerWorldProfile.Decor decor : palette.decor()) {
                acc += decor.chance();
                if (roll < acc) {
                    level.setBlock(pos.set(x, top + 1, z), decor.block().defaultBlockState(), SET_FLAGS);
                    break;
                }
            }
        }
    }

    private Feature poolFeatureAt(Variant v, int lx, int lz) {
        double freq = v.terrain().frequency() * freqJitter;
        for (Feature f : v.features()) {
            if (f.type() != FeatureType.POOLS && f.type() != FeatureType.SPRINGS) {
                continue;
            }
            double n = InnerWorldNoise.noise(seed + 50, lx / (26.0 / freq), lz / (26.0 / freq));
            if (n > 0.5 + (1.0 - Math.min(1.0, f.density())) * 0.32) {
                return f;
            }
        }
        return null;
    }

    private static Block fluidOf(Feature f) {
        return f.block().orElse(f.type() == FeatureType.SPRINGS ? ModBlocks.LIQUIFIED_SPIRITUAL_QI_BLOCK.get() : Blocks.WATER);
    }

    private void placeOres(ServerLevel level, BlockPos.MutableBlockPos pos, Variant variant, int x, int z, int lx, int lz,
                           int groundTop, int bottom) {
        List<InnerWorldProfile.Ore> ores = variant.resources().ores();
        for (int i = 0; i < ores.size(); i++) {
            InnerWorldProfile.Ore ore = ores.get(i);
            int minY = Math.max(bottom + 7, groundTop - ore.maxDepth());
            int maxY = Math.min(groundTop - 4, groundTop - ore.minDepth());
            double threshold = 1.0 - Mth.clamp(ore.density(), 0.0, 1.0) * 1.5;
            double scale = Math.max(1.5, ore.scale());
            BlockState state = ore.block().defaultBlockState();
            for (int y = minY; y <= maxY; y++) {
                if (InnerWorldNoise.noise3(seed + 200L + i * 13L, lx / scale, y / scale, lz / scale) > threshold) {
                    level.setBlock(pos.set(x, y, z), state, SET_FLAGS);
                }
            }
        }
    }

    // ---- treasure veins: deterministic per seed, so they can be recomputed for particles and Divine Sense without storage
    private static final int VEIN_CELL = 80;

    private record Vein(double px, double pz, int y, int radius, Block block) {
    }

    private Vein veinInCell(int cx, int cz) {
        double px = (cx + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 4001, cx, cz)) * VEIN_CELL;
        double pz = (cz + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 4002, cx, cz)) * VEIN_CELL;
        double fromOrigin = Math.hypot(px, pz);
        if (fromOrigin < 30.0) {
            return null;
        }
        Variant variant = regionAt((int) px, (int) pz, fromOrigin).a().variant();
        List<InnerWorldProfile.Treasure> list = variant.resources().treasure();
        if (list.isEmpty()) {
            return null;
        }
        double total = 0.0;
        for (InnerWorldProfile.Treasure t : list) {
            total += Math.max(0.0001, t.weight());
        }
        double pick = InnerWorldNoise.hash(seed + 4004, cx, cz) * total;
        InnerWorldProfile.Treasure chosen = list.get(list.size() - 1);
        for (InnerWorldProfile.Treasure t : list) {
            pick -= Math.max(0.0001, t.weight());
            if (pick <= 0.0) {
                chosen = t;
                break;
            }
        }
        if (InnerWorldNoise.hash(seed + 4003, cx, cz) >= chosen.density()) {
            return null;
        }
        int depth = (int) Mth.lerp(InnerWorldNoise.hash(seed + 4005, cx, cz), chosen.minDepth(), chosen.maxDepth());
        return new Vein(px, pz, InnerWorld.SURFACE_Y - depth, Math.max(1, chosen.radius()), chosen.block());
    }

    private void placeTreasure(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int z, int lx, int lz) {
        int gx = Math.floorDiv(lx, VEIN_CELL);
        int gz = Math.floorDiv(lz, VEIN_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Vein vein = veinInCell(gx + dx, gz + dz);
                if (vein == null) {
                    continue;
                }
                double ddx = lx - vein.px();
                double ddz = lz - vein.pz();
                double horizontal = ddx * ddx + ddz * ddz;
                double r2 = vein.radius() * vein.radius() + 0.5;
                if (horizontal > r2) {
                    continue;
                }
                BlockState state = vein.block().defaultBlockState();
                for (int y = vein.y() - vein.radius(); y <= vein.y() + vein.radius(); y++) {
                    double dy = y - vein.y();
                    if (horizontal + dy * dy <= r2) {
                        level.setBlock(pos.set(x, y, z), state, SET_FLAGS);
                    }
                }
            }
        }
    }

    /** World positions of treasure vein centres within radius of a point. Used for particle hints and Divine Sense. */
    List<BlockPos> veinCentersNear(int plotCenterX, int plotCenterZ, double aroundX, double aroundZ, double radius) {
        List<BlockPos> result = new ArrayList<>();
        int minGx = Math.floorDiv((int) (aroundX - plotCenterX - radius), VEIN_CELL);
        int maxGx = Math.floorDiv((int) (aroundX - plotCenterX + radius), VEIN_CELL);
        int minGz = Math.floorDiv((int) (aroundZ - plotCenterZ - radius), VEIN_CELL);
        int maxGz = Math.floorDiv((int) (aroundZ - plotCenterZ + radius), VEIN_CELL);
        for (int gx = minGx; gx <= maxGx; gx++) {
            for (int gz = minGz; gz <= maxGz; gz++) {
                Vein vein = veinInCell(gx, gz);
                if (vein == null) {
                    continue;
                }
                double wx = plotCenterX + vein.px();
                double wz = plotCenterZ + vein.pz();
                if (Math.hypot(wx - aroundX, wz - aroundZ) <= radius) {
                    result.add(BlockPos.containing(wx, vein.y(), wz));
                }
            }
        }
        return result;
    }

    record QiInfo(Variant variant, Identifier path, boolean strongest, double springFraction) {
    }

    /** What ambient qi a chunk should have: its region's qi block, whether it is the strongest path's region, how spring-covered it is. */
    QiInfo qiAt(int chunkX, int chunkZ, int centerX, int centerZ) {
        int lx = chunkX * 16 + 8 - centerX;
        int lz = chunkZ * 16 + 8 - centerZ;
        Theme dominant = regionAt(lx, lz, Math.sqrt((double) lx * lx + (double) lz * lz)).dominant();
        Identifier corePath = themes.get(0).path();
        int springs = 0;
        for (int qx = 0; qx < 4; qx++) {
            for (int qz = 0; qz < 4; qz++) {
                int sx = chunkX * 16 + qx * 4 + 2 - centerX;
                int sz = chunkZ * 16 + qz * 4 + 2 - centerZ;
                double d = Math.sqrt((double) sx * sx + (double) sz * sz);
                Feature f = d > 16.0 ? poolFeatureAt(regionAt(sx, sz, d).dominant().variant(), sx, sz) : null;
                if (f != null && f.type() == FeatureType.SPRINGS) {
                    springs++;
                }
            }
        }
        boolean strongest = dominant.path() != null && dominant.path().equals(corePath);
        return new QiInfo(dominant.variant(), dominant.path(), strongest, springs / 16.0);
    }

    /** One candidate feature per grid cell, checked over the 3x3 neighbourhood. Returns the (possibly raised) top. */
    private int gridRaise(Feature f, int index, int current, int lx, int lz) {
        int g = switch (f.type()) {
            case SPIRES -> 22;
            case PILLARS -> 18;
            default -> 64;
        };
        long s = seed + 1000L + index * 131L + f.type().ordinal() * 17L;
        int gx = Math.floorDiv(lx, g);
        int gz = Math.floorDiv(lz, g);
        int result = current;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = gx + dx;
                int cz = gz + dz;
                if (InnerWorldNoise.hash(s, cx, cz) >= f.density()) {
                    continue;
                }
                double px = (cx + 0.2 + 0.6 * InnerWorldNoise.hash(s + 1, cx, cz)) * g;
                double pz = (cz + 0.2 + 0.6 * InnerWorldNoise.hash(s + 2, cx, cz)) * g;
                double radius = Mth.lerp(InnerWorldNoise.hash(s + 3, cx, cz), f.minSize(), f.maxSize());
                double height = Mth.lerp(InnerWorldNoise.hash(s + 4, cx, cz), f.minHeight(), f.maxHeight());
                double d = Math.hypot(lx - px, lz - pz);
                if (d >= radius) {
                    continue;
                }
                int candidate = switch (f.type()) {
                    case SPIRES -> current + (int) (height * Math.pow(1.0 - d / radius, 1.4));
                    case PILLARS -> current + (int) height;
                    default -> Math.max(current, InnerWorld.SURFACE_Y + (int) height);
                };
                result = Math.max(result, candidate);
            }
        }
        return result;
    }

    private void islands(ServerLevel level, BlockPos.MutableBlockPos pos, Feature f, int index, Palette palette,
                         int lx, int lz, int x, int z) {
        final int g = 64;
        long s = seed + 3000L + index * 97L;
        int gx = Math.floorDiv(lx, g);
        int gz = Math.floorDiv(lz, g);
        Block body = f.block().orElse(palette.stone());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = gx + dx;
                int cz = gz + dz;
                if (InnerWorldNoise.hash(s, cx, cz) >= f.density()) {
                    continue;
                }
                double px = (cx + 0.25 + 0.5 * InnerWorldNoise.hash(s + 1, cx, cz)) * g;
                double pz = (cz + 0.25 + 0.5 * InnerWorldNoise.hash(s + 2, cx, cz)) * g;
                double radius = Mth.lerp(InnerWorldNoise.hash(s + 3, cx, cz), f.minSize(), f.maxSize());
                double d = Math.hypot(lx - px, lz - pz);
                if (d >= radius) {
                    continue;
                }
                int centerY = InnerWorld.SURFACE_Y + (int) Mth.lerp(InnerWorldNoise.hash(s + 4, cx, cz), f.minHeight(), f.maxHeight());
                double k = 1.0 - (d / radius) * (d / radius);
                int yTop = Math.min(300, centerY + (int) (radius * 0.14 * k));
                int yBottom = centerY - (int) (radius * 0.6 * k);
                for (int y = yBottom; y <= yTop; y++) {
                    Block block = y == yTop ? palette.top() : y >= yTop - 2 ? palette.sub() : body;
                    level.setBlock(pos.set(x, y, z), block.defaultBlockState(), SET_FLAGS);
                }
            }
        }
    }
}