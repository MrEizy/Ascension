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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.zic.ascension.common.blocks.ModBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Feature;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.FeatureType;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Palette;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Terrain;
import net.zic.ascension.impl.core.innerworld.InnerWorldProfile.Variant;

/**
 * The seeded terrain generator for one player's plot. Everything is a pure function of (seed, x, z, y), so chunks and
 * rings can be generated in any order and always line up.
 *
 * REGIONS (vanilla-style, no grid): two warped, multi-octave climate noises (u, v) place every column on a 2D "climate
 * map". Each of the seed's 3-5 themes owns a point on that map, and a column belongs to the nearest theme point (distance
 * scaled by the theme's path weight, so strongly cultivated paths own more land). Because the noise is smooth you get
 * continent-sized, irregular biomes with natural borders, exactly the way vanilla biomes sit on its climate map. The
 * core around the dais always belongs to theme 0 (the strongest path).
 *
 * STRUCTURE vs STYLE: palette and decoration may dither across a border, but every STRUCTURAL feature (mesas, spires,
 * pillars, floating islands) is decided once at its own centre, so it is always built whole, never riddled with holes
 * where a column happened to roll a neighbouring theme. Lakes, rivers, caves and ravines fade smoothly out towards a
 * border instead of stopping dead.
 *
 * WATER is sculpted, not punched in: a warped noise field defines each lake or river; inside it the bed dips smoothly
 * into a bowl, outside it the terrain eases down (or up) to meet the shoreline over a wide bank, and a band of shore
 * blocks (sand, basalt, calcite...) ties it together. Every pool in a plot shares one water level.
 *
 * Every column is self-contained (nothing writes into a neighbouring column), so the budgeted per-tick column loop in
 * InnerWorldGrowth can stop and resume anywhere.
 */
final class SeededWorld {
    private static final int SET_FLAGS = 18;
    private static final double DAIS_RADIUS = 5.5;
    private static final int WATER_LEVEL = InnerWorld.SURFACE_Y - 2;
    /** Largest region octave in blocks. Regions come out roughly 50-150 blocks across. Lower = smaller biomes. */
    private static final double REGION_SCALE = 120.0;

    private record Theme(Variant variant, double weight, Identifier path) {
    }

    private final long seed;
    private final List<Theme> themes = new ArrayList<>();
    private final double[] pointU;
    private final double[] pointV;
    private final double[] distanceScale;
    private final double ampJitter;
    private final double freqJitter;
    private final double decorJitter;
    private final boolean hasTrees;

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
        int n = themes.size();
        double sum = 0.0;
        for (Theme theme : themes) {
            sum += theme.weight();
        }
        double average = sum / n;
        this.pointU = new double[n];
        this.pointV = new double[n];
        this.distanceScale = new double[n];
        for (int i = 0; i < n; i++) {
            pointU[i] = 0.2 + 0.6 * InnerWorldNoise.hash(seed + 40 + i * 3L, i, 1);
            pointV[i] = 0.2 + 0.6 * InnerWorldNoise.hash(seed + 41 + i * 3L, i, 2);
            // heavier theme = smaller effective distance = larger share of the map
            distanceScale[i] = 1.0 / Math.sqrt(Math.max(0.2, themes.get(i).weight() / average));
        }
        this.ampJitter = 0.75 + InnerWorldNoise.hash(seed + 101, 1, 1) * 0.5;
        this.freqJitter = 0.8 + InnerWorldNoise.hash(seed + 102, 2, 2) * 0.5;
        this.decorJitter = 0.5 + InnerWorldNoise.hash(seed + 103, 3, 3) * 1.0;
        boolean anyTrees = false;
        for (Theme theme : themes) {
            for (Feature f : theme.variant().features()) {
                anyTrees |= f.type() == FeatureType.TREES;
            }
        }
        this.hasTrees = anyTrees;
    }

    // ------------------------------------------------------------------------------------------------ regions

    private record Region(Theme a, Theme b, double wa, Theme dominant) {
        /** 1 deep inside region a, fading to 0 right on the border. Smoothly retires features that must not be cut off. */
        double fade() {
            return a == b ? 1.0 : Math.min(1.0, (wa - 0.5) * 2.0);
        }
    }

    private Region regionAt(int lx, int lz, double dist) {
        // Region size: the biggest octave is ~120 blocks and the smallest kept octave ~55, so no region is smaller than
        // roughly 50 blocks across (the old 260/110/45 scale gave ~250-block regions). Tune REGION_SCALE to resize them all.
        final double s1 = REGION_SCALE;
        double wx = lx + 22.0 * (InnerWorldNoise.noise(seed + 30, lx / (s1 * 0.6), lz / (s1 * 0.6)) - 0.5) * 2.0;
        double wz = lz + 22.0 * (InnerWorldNoise.noise(seed + 33, lx / (s1 * 0.6) + 17.3, lz / (s1 * 0.6) - 9.1) - 0.5) * 2.0;
        double u = 0.65 * InnerWorldNoise.noise(seed + 31, wx / s1, wz / s1)
                + 0.3 * InnerWorldNoise.noise(seed + 34, wx / (s1 * 0.5), wz / (s1 * 0.5))
                + 0.05 * InnerWorldNoise.noise(seed + 36, wx / (s1 * 0.3), wz / (s1 * 0.3));
        double v = 0.65 * InnerWorldNoise.noise(seed + 32, wx / s1 + 400.0, wz / s1 - 400.0)
                + 0.3 * InnerWorldNoise.noise(seed + 35, wx / (s1 * 0.5) + 400.0, wz / (s1 * 0.5) - 400.0)
                + 0.05 * InnerWorldNoise.noise(seed + 37, wx / (s1 * 0.3) + 400.0, wz / (s1 * 0.3) - 400.0);
        // value noise clusters around 0.5; stretch it so the theme points are reachable
        u = Mth.clamp(0.5 + (u - 0.5) * 2.1, 0.0, 1.0);
        v = Mth.clamp(0.5 + (v - 0.5) * 2.1, 0.0, 1.0);

        double core = 1.0 - InnerWorldNoise.smoothstep(45.0, 110.0, dist);   // the dais region of theme 0, sized to match smaller biomes
        double d1 = Double.MAX_VALUE;
        double d2 = Double.MAX_VALUE;
        int t1 = 0;
        int t2 = 0;
        for (int i = 0; i < themes.size(); i++) {
            double d = Math.hypot(u - pointU[i], v - pointV[i]) * distanceScale[i];
            if (i == 0) {
                d -= 1.2 * core;
            }
            if (d < d1) {
                d2 = d1;
                t2 = t1;
                d1 = d;
                t1 = i;
            } else if (d < d2) {
                d2 = d;
                t2 = i;
            }
        }
        Theme a = themes.get(t1);
        if (themes.size() == 1 || d2 == Double.MAX_VALUE) {
            return new Region(a, a, 1.0, a);
        }
        Theme b = themes.get(t2);
        double wa = 0.5 + 0.5 * InnerWorldNoise.smoothstep(0.0, 0.07, d2 - d1);
        double dither = 0.5 + (InnerWorldNoise.noise(seed + 12, lx / 2.5, lz / 2.5) - 0.5) * 1.8;
        return new Region(a, b, wa, dither < wa ? a : b);
    }

    /** The region variant at a plot-local point (for mob spawning and anything else that needs the local theme). */
    Variant variantAt(int lx, int lz) {
        return regionAt(lx, lz, Math.sqrt((double) lx * lx + (double) lz * lz)).dominant().variant();
    }

    /** Atmosphere biome of the nearest region: vanilla-style crisp biome borders, no dithering. */
    Optional<Identifier> biomeAt(int lx, int lz) {
        double dist = Math.sqrt((double) lx * lx + (double) lz * lz);
        return regionAt(lx, lz, dist).a().variant().biome();
    }

    // ------------------------------------------------------------------------------------------------ height

    private double height(Theme theme, int lx, int lz, double dist, boolean smooth) {
        Terrain t = theme.variant().terrain();
        double f = t.frequency() * freqJitter;
        double n = smooth
                ? InnerWorldNoise.noise(seed, lx / (48.0 / f), lz / (48.0 / f))
                : InnerWorldNoise.noise(seed, lx / (48.0 / f), lz / (48.0 / f)) * 0.6
                  + InnerWorldNoise.noise(seed + 1, lx / (20.0 / f), lz / (20.0 / f)) * 0.3
                  + InnerWorldNoise.noise(seed + 2, lx / (7.0 / f), lz / (7.0 / f)) * 0.1;
        double blend = InnerWorldNoise.smoothstep(6.0, 18.0, dist);
        double rel = t.offset() * blend + (n - 0.5) * t.amplitude() * ampJitter * blend;
        if (t.terrace() > 0 && !smooth) {
            rel = Math.round(rel / t.terrace()) * (double) t.terrace();
        }
        return InnerWorld.SURFACE_Y + rel;
    }

    /** Terrain height blended between the two nearest regions; smooth = drop the small bumpy octaves (used around water). */
    private double blendedHeight(Region region, int lx, int lz, double dist, boolean smooth) {
        Theme a = region.a();
        Theme b = region.b();
        if (a == b) {
            return height(a, lx, lz, dist, smooth);
        }
        double wa = region.wa();
        return wa * height(a, lx, lz, dist, smooth) + (1.0 - wa) * height(b, lx, lz, dist, smooth);
    }

    // ------------------------------------------------------------------------------------------------ water

    /** margin >= 0 means inside the pool; between -bank and 0 is the sloped bank. */
    private record Water(Feature feature, double margin, double bank, double span, int maxDepth) {
    }

    private Water waterFor(Region region, int lx, int lz, double dist) {
        Variant v = region.a().variant();
        double fade = region.fade();
        double freq = v.terrain().frequency() * freqJitter;
        double wx = lx + 9.0 * (InnerWorldNoise.noise(seed + 60, lx / 55.0, lz / 55.0) - 0.5) * 2.0;
        double wz = lz + 9.0 * (InnerWorldNoise.noise(seed + 61, lx / 55.0 + 31.7, lz / 55.0 - 12.9) - 0.5) * 2.0;
        // never near the dais, and fading out towards a region border so water doesn't end in a wall
        double penalty = 0.35 * (1.0 - fade) + 0.3 * (1.0 - InnerWorldNoise.smoothstep(14.0, 44.0, dist));

        Water best = null;
        for (Feature f : v.features()) {
            double margin;
            double bank;
            double span;
            int depth;
            if (f.type() == FeatureType.RIVERS) {
                double n = InnerWorldNoise.noise(seed + 75, wx / (95.0 / freq), wz / (95.0 / freq));
                double shape = 1.0 - Math.abs(n - 0.5) * 2.0;
                margin = shape - (1.0 - (0.035 + 0.06 * Math.min(1.0, f.density())));
                bank = 0.17;
                span = 0.04;
                depth = 2;
            } else if (f.type() == FeatureType.POOLS || f.type() == FeatureType.SPRINGS) {
                boolean spring = f.type() == FeatureType.SPRINGS;
                double scale = (spring ? 13.0 : 30.0) / freq;
                double n = 0.86 * InnerWorldNoise.noise(seed + 50, wx / scale, wz / scale)
                        + 0.14 * InnerWorldNoise.noise(seed + 52, wx / (scale * 0.6), wz / (scale * 0.6));
                n = 0.5 + (n - 0.5) * 1.5;
                margin = n - (0.5 + (1.0 - Math.min(1.0, f.density())) * 0.3);
                bank = spring ? 0.2 : 0.28;
                span = spring ? 0.12 : 0.18;
                depth = spring ? 3 : 4;
            } else {
                continue;
            }
            margin -= penalty;
            if (margin > -bank && (best == null || margin > best.margin())) {
                best = new Water(f, margin, bank, span, depth);
            }
        }
        return best;
    }

    private static Block fluidOf(Feature f) {
        return f.block().orElse(f.type() == FeatureType.SPRINGS ? ModBlocks.LIQUIFIED_SPIRITUAL_QI_BLOCK.get() : Blocks.WATER);
    }

    private static Block shoreOf(Feature f, Block fluid) {
        if (f.shore().isPresent()) {
            return f.shore().get();
        }
        if (f.type() == FeatureType.SPRINGS) {
            return Blocks.CALCITE;
        }
        if (fluid == Blocks.LAVA) {
            return Blocks.BASALT;
        }
        if (fluid == Blocks.ICE || fluid == Blocks.PACKED_ICE || fluid == Blocks.BLUE_ICE) {
            return Blocks.SNOW_BLOCK;
        }
        return Blocks.SAND;
    }

    // ------------------------------------------------------------------------------------------------ the column

    /** Everything about a column's silhouette. A pure function of (seed, lx, lz), so neighbours can be inspected without being built. */
    private record Shape(Region region, double dist, boolean dais, int ground, boolean pool, boolean shoreZone,
                         Block fluid, Block shore, Raise raise, int thickness) {
        int top() {
            return raise == null ? ground : raise.top();
        }
    }

    private Shape shapeAt(int lx, int lz) {
        double dist = Math.sqrt((double) lx * lx + (double) lz * lz);
        boolean dais = dist <= DAIS_RADIUS;
        Region region = regionAt(lx, lz, dist);
        int ground = (int) Math.round(blendedHeight(region, lx, lz, dist, false));

        // Structures are decided on the natural ground, and water never goes where one stands (no lava pits in a mesa top).
        Raise raise = !dais && dist > 14.0 ? raiseAt(lx, lz, ground) : null;

        boolean pool = false;
        boolean shoreZone = false;
        Block fluid = Blocks.WATER;
        Block shore = Blocks.SAND;
        Water water = dais || raise != null ? null : waterFor(region, lx, lz, dist);
        if (water != null) {
            fluid = fluidOf(water.feature());
            shore = shoreOf(water.feature(), fluid);
            if (water.margin() >= 0.0) {
                pool = true;
                // eased bowl: drops away from the shoreline quickly, then levels out, instead of a straight ramp
                double depthT = InnerWorldNoise.smoothstep(0.0, water.span(), water.margin());
                double eased = 1.0 - (1.0 - depthT) * (1.0 - depthT);
                ground = WATER_LEVEL - (int) Math.round(eased * water.maxDepth());
            } else {
                // calm the small bumps near the water, then ease the land to meet the shoreline exactly at the waterline
                double smoothGround = blendedHeight(region, lx, lz, dist, true);
                double calm = InnerWorldNoise.smoothstep(-water.bank(), -water.bank() * 0.35, water.margin());
                double base = Mth.lerp(calm, ground, smoothGround);
                double t = InnerWorldNoise.smoothstep(-water.bank(), 0.0, water.margin());
                ground = (int) Math.round(Mth.lerp(t, base, WATER_LEVEL + 1));
                shoreZone = water.margin() > -water.bank() * 0.5;
            }
        }
        int thickness = 24 + (int) (InnerWorldNoise.noise(seed + 3, lx / 30.0, lz / 30.0) * 10.0);
        return new Shape(region, dist, dais, ground, pool, shoreZone, fluid, shore, raise, thickness);
    }

    /** First free block above the surface (water surface included when asked). Used to give vanilla structures our terrain heights. */
    int surfaceY(int lx, int lz, boolean includeWater) {
        Shape shape = shapeAt(lx, lz);
        return includeWater && shape.pool() ? WATER_LEVEL + 1 : shape.top() + 1;
    }

    record ColumnInfo(int top, boolean wet, boolean raised, boolean dais) {
    }

    ColumnInfo columnInfo(int lx, int lz) {
        Shape shape = shapeAt(lx, lz);
        return new ColumnInfo(shape.top(), shape.pool() || shape.shoreZone(), shape.raise() != null, shape.dais());
    }

    void fillColumn(ServerLevel level, int x, int z, int centerX, int centerZ) {
        final int lx = x - centerX;
        final int lz = z - centerZ;
        final Shape shape = shapeAt(lx, lz);
        final Region region = shape.region();
        final double dist = shape.dist();
        final boolean dais = shape.dais();
        final boolean pool = shape.pool();
        final boolean shoreZone = shape.shoreZone();
        final Raise raise = shape.raise();

        Variant variant = region.dominant().variant();   // style: dithered across borders
        Variant regional = region.a().variant();         // structure: stable, never dithered
        Palette palette = variant.palette();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int groundTop = shape.ground();
        int top = shape.top();
        Block topBlock = pool || shoreZone ? shape.shore() : palette.top();
        Block subBlock = pool || shoreZone ? shape.shore() : palette.sub();

        // ---- the column itself (fluids are NOT placed here, see completeFluids)
        int bottom = groundTop - shape.thickness();
        List<Block> bands = palette.bands();
        for (int y = bottom; y <= top; y++) {
            BlockState state;
            if (y > groundTop) {
                if (raise.mode() == 2) {
                    List<Block> raiseBands = raise.palette().bands();
                    Block block = y == top ? raise.palette().top()
                            : raiseBands.isEmpty() ? raise.palette().stone() : raiseBands.get(Math.floorMod(y, raiseBands.size()));
                    state = block.defaultBlockState();
                } else {
                    state = raise.block().defaultBlockState();
                }
            } else if (y >= groundTop - 3) {
                state = dais ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
                        : (y == groundTop ? topBlock : subBlock).defaultBlockState();
            } else if (y <= bottom + 5) {
                state = palette.deep().defaultBlockState();
            } else if (!bands.isEmpty()) {
                state = bands.get(Math.floorMod(y, bands.size())).defaultBlockState();
            } else {
                state = palette.stone().defaultBlockState();
            }
            level.setBlock(pos.set(x, y, z), state, SET_FLAGS);
        }

        // ---- ores and treasure veins (stone only, below the surface layers)
        if (!dais) {
            placeOres(level, pos, variant, x, z, lx, lz, groundTop, bottom);
            placeTreasure(level, pos, x, z, lx, lz);
        }

        // ---- carving: caves and ravines, thinned out near region borders and never through raised structures or water
        boolean carved = false;
        double fade = region.fade();
        if (!dais && dist > 12.0 && fade > 0.12) {
            BlockState air = Blocks.AIR.defaultBlockState();
            for (Feature f : regional.features()) {
                if (f.type() == FeatureType.CAVES) {
                    double w = (0.035 + 0.05 * Math.min(1.0, f.density())) * fade;
                    for (int y = bottom + 7; y <= groundTop - 4; y++) {
                        double c1 = InnerWorldNoise.noise3(seed + 81, lx / 30.0, y / 14.0, lz / 30.0);
                        double c2 = InnerWorldNoise.noise3(seed + 82, lx / 30.0, y / 14.0, lz / 30.0);
                        if (Math.abs(c1 - 0.5) < w && Math.abs(c2 - 0.5) < w) {
                            level.setBlock(pos.set(x, y, z), air, SET_FLAGS);
                        }
                    }
                } else if (f.type() == FeatureType.RAVINES && !pool && !shoreZone && raise == null) {
                    double ridge = Math.abs(InnerWorldNoise.noise(seed + 70, lx / 70.0, lz / 70.0) - 0.5);
                    double width = (0.012 + 0.012 * Math.min(1.0, f.density())) * fade;
                    if (ridge < width) {
                        double depth = Mth.lerp(1.0 - ridge / width, 2.0, f.maxHeight());
                        for (int y = groundTop - (int) depth; y <= groundTop; y++) {
                            level.setBlock(pos.set(x, y, z), air, SET_FLAGS);
                        }
                        carved = true;
                    }
                }
            }
        }

        // ---- floating islands
        if (dist > 40.0) {
            islands(level, pos, x, z, lx, lz);
        }

        // ---- decoration
        boolean decoratable = !dais && !pool && !shoreZone && !carved && (raise == null || raise.mode() == 2);
        if (decoratable && !palette.decor().isEmpty()) {
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

        // ---- trees (drawn after decoration so a trunk replaces any plant on its column)
        if (hasTrees && !dais) {
            trees(level, pos, x, z, lx, lz);
        }

        // ---- fluids, once every neighbour exists
        if (pool || shoreZone) {
            completeFluids(level, pos, x, z, lx, lz, shape);
        }
    }

    // ---- containment: why lava and water no longer spill
    //
    // A liquid block placed next to AIR schedules a flow tick even with block-update flags off. During generation the
    // neighbouring column is often not built yet (next column, next chunk, next ring, or beyond the border), so the
    // liquid used to run into the gap or fall down the void. Now a pool column is only filled once all four neighbours
    // are provably there (a deep layer block that is never carved exists at their computed height) and each is either
    // another pool column or ground at or above the water level. Anything still waiting is completed later, when the
    // neighbour is built: every finished column re-checks the pool columns around it. This is stateless (it only reads
    // the world and the seed), so it also survives a restart in the middle of a ring.

    private static final int[][] AROUND = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private void completeFluids(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int z, int lx, int lz, Shape self) {
        tryFluid(level, pos, x, z, lx, lz, self);
        for (int[] d : AROUND) {
            Shape neighbour = shapeAt(lx + d[0], lz + d[1]);
            if (neighbour.pool()) {
                tryFluid(level, pos, x + d[0], z + d[1], lx + d[0], lz + d[1], neighbour);
            }
        }
    }

    private boolean isBuilt(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int z, Shape shape) {
        return !level.getBlockState(pos.set(x, shape.ground() - shape.thickness() + 2, z)).isAir();
    }

    private void tryFluid(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int z, int lx, int lz, Shape shape) {
        if (!shape.pool() || shape.ground() >= WATER_LEVEL || !isBuilt(level, pos, x, z, shape)) {
            return;
        }
        if (!level.getBlockState(pos.set(x, WATER_LEVEL, z)).isAir()) {
            return; // already filled
        }
        for (int[] d : AROUND) {
            Shape neighbour = shapeAt(lx + d[0], lz + d[1]);
            if (!isBuilt(level, pos, x + d[0], z + d[1], neighbour)) {
                return; // not there yet: it will call us back when it is built
            }
            if (!neighbour.pool() && neighbour.top() < WATER_LEVEL) {
                return; // would leak over lower ground: stay dry
            }
        }
        BlockState fluid = shape.fluid().defaultBlockState();
        for (int y = shape.ground() + 1; y <= WATER_LEVEL; y++) {
            level.setBlock(pos.set(x, y, z), fluid, SET_FLAGS);
        }
    }

    // ------------------------------------------------------------------------------------------------ trees
    //
    // Same rule as every other feature: a tree is a pure function of the seed, and each COLUMN draws whichever trees reach
    // it, so nothing writes into a neighbouring column and chunks still generate in any order. One tree per 6-block cell
    // (jittered), picked from the TREES features of the region that owns the cell, so a forest can mix big oaks and small
    // birches. A tree stands on the final ground height and never on water, banks, mesas, spires or the dais.

    private static final int TREE_CELL = 6;
    private static final int TREE_REACH_CELLS = 3;   // canopy radius is capped at 12, +width, so 3 cells (18 blocks) is enough

    private record Tree(int trunkX, int trunkZ, int baseY, int height, int radius, InnerWorldProfile.TreeShape shape,
                        int width, Block log, Block leaves) {
        int top() {
            return baseY + height;
        }

        double centreX() {
            return trunkX + (width == 2 ? 1.0 : 0.5);
        }

        double centreZ() {
            return trunkZ + (width == 2 ? 1.0 : 0.5);
        }
    }

    private static final Tree NO_TREE = new Tree(0, 0, 0, 0, 0, InnerWorldProfile.TreeShape.ROUND, 1, null, null);
    private final Map<Long, Tree> treeCache = new HashMap<>();

    private Tree treeInCell(int cx, int cz) {
        long key = ((long) (cx & 0xFFFFFFF) << 28) | (cz & 0xFFFFFFFL);
        Tree cached = treeCache.get(key);
        if (cached != null) {
            return cached == NO_TREE ? null : cached;
        }
        Tree made = computeTree(cx, cz);
        if (treeCache.size() > 60000) {
            treeCache.clear();
        }
        treeCache.put(key, made == null ? NO_TREE : made);
        return made;
    }

    private Tree computeTree(int cx, int cz) {
        double roll = InnerWorldNoise.hash(seed + 8000, cx, cz);
        double px = (cx + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 8001, cx, cz)) * TREE_CELL;
        double pz = (cz + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 8002, cx, cz)) * TREE_CELL;
        double fromOrigin = Math.hypot(px, pz);
        if (fromOrigin < 16.0) {
            return null;
        }
        Variant variant = regionAt((int) px, (int) pz, fromOrigin).a().variant();
        double total = 0.0;
        for (Feature f : variant.features()) {
            if (f.type() == FeatureType.TREES) {
                total += Math.min(1.0, f.density());
            }
        }
        if (total <= 0.0 || roll >= Math.min(1.0, total)) {
            return null;
        }
        Feature chosen = null;
        double acc = 0.0;
        for (Feature f : variant.features()) {
            if (f.type() == FeatureType.TREES) {
                acc += Math.min(1.0, f.density());
                if (roll < acc) {
                    chosen = f;
                    break;
                }
            }
        }
        if (chosen == null) {
            return null;
        }

        int tx = (int) Math.floor(px);
        int tz = (int) Math.floor(pz);
        Shape ground = shapeAt(tx, tz);
        if (ground.dais() || ground.pool() || ground.shoreZone() || ground.raise() != null) {
            return null;
        }
        int width = chosen.width() >= 2 ? 2 : 1;
        if (width == 2) {
            // a thick trunk needs reasonably level, dry ground under all four of its columns
            for (int[] d : new int[][]{{1, 0}, {0, 1}, {1, 1}}) {
                Shape other = shapeAt(tx + d[0], tz + d[1]);
                if (other.pool() || other.shoreZone() || other.raise() != null || Math.abs(other.ground() - ground.ground()) > 2) {
                    return null;
                }
            }
        }
        int radius = (int) Math.max(1, Math.min(12, Math.round(Mth.lerp(InnerWorldNoise.hash(seed + 8003, cx, cz), chosen.minSize(), chosen.maxSize()))));
        int height = (int) Math.max(2, Math.round(Mth.lerp(InnerWorldNoise.hash(seed + 8004, cx, cz), chosen.minHeight(), chosen.maxHeight())));
        Block log = chosen.block().orElse(Blocks.OAK_LOG);
        return new Tree(tx, tz, ground.ground(), height, radius, chosen.shape(), width, log, chosen.leaves().orElse(null));
    }

    /** -1 = no leaves at this height. Otherwise the canopy radius at height y. */
    private static double canopyRadius(Tree t, int y) {
        int dy = y - t.top();
        double r = t.radius();
        switch (t.shape()) {
            case ROUND, WEEPING -> {
                double e = dy >= 0 ? dy / (r * 0.85 + 0.5) : -dy / (r * 0.6 + 0.5);
                return e > 1.0 ? -1.0 : r * Math.sqrt(1.0 - e * e);
            }
            case DOME -> {
                if (dy < -2 || dy > r * 0.7) {
                    return -1.0;
                }
                if (dy < 0) {
                    return r * 0.85;
                }
                double e = dy / (r * 0.7 + 0.5);
                return r * Math.sqrt(1.0 - e * e);
            }
            case UMBRELLA -> {
                return dy == 0 ? r : dy == 1 ? r * 0.65 : dy == -1 ? r * 0.4 : -1.0;
            }
            case CONE -> {
                int span = (int) (t.height() * 0.85);
                int depth = t.top() + 1 - y;
                if (depth < 0 || depth > span) {
                    return -1.0;
                }
                double cone = r * Math.pow(depth / (double) (span + 1), 0.9);
                return depth % 2 == 1 ? Math.max(0.0, cone - 0.8) : cone;
            }
        }
        return -1.0;
    }

    private static BlockState leafState(Block block) {
        BlockState state = block.defaultBlockState();
        // persistent leaves never decay; without this the canopy would wither away on random ticks
        return state.hasProperty(BlockStateProperties.PERSISTENT) ? state.setValue(BlockStateProperties.PERSISTENT, true) : state;
    }

    private void trees(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int z, int lx, int lz) {
        if (!hasTrees) {
            return;
        }
        int gx = Math.floorDiv(lx, TREE_CELL);
        int gz = Math.floorDiv(lz, TREE_CELL);
        for (int dx = -TREE_REACH_CELLS; dx <= TREE_REACH_CELLS; dx++) {
            for (int dz = -TREE_REACH_CELLS; dz <= TREE_REACH_CELLS; dz++) {
                Tree tree = treeInCell(gx + dx, gz + dz);
                if (tree == null) {
                    continue;
                }
                double rx = lx + 0.5 - tree.centreX();
                double rz = lz + 0.5 - tree.centreZ();
                double dist = Math.sqrt(rx * rx + rz * rz);
                if (dist > tree.radius() + 1.5) {
                    continue;
                }

                // trunk
                double half = tree.width() / 2.0 + 0.01;
                if (Math.abs(rx) < half && Math.abs(rz) < half) {
                    BlockState log = tree.log().defaultBlockState();
                    for (int y = tree.baseY() + 1; y <= tree.top(); y++) {
                        level.setBlock(pos.set(x, y, z), log, SET_FLAGS);
                    }
                }

                // canopy
                if (tree.leaves() == null) {
                    continue;
                }
                BlockState leaves = leafState(tree.leaves());
                boolean weeping = tree.shape() == InnerWorldProfile.TreeShape.WEEPING;
                int yLo = tree.top() - (tree.shape() == InnerWorldProfile.TreeShape.CONE ? (int) (tree.height() * 0.85)
                        : (int) (tree.radius() * 0.65) + (weeping ? 8 : 0));
                int yHi = tree.top() + (tree.shape() == InnerWorldProfile.TreeShape.CONE ? 1 : tree.radius() + 1);
                for (int y = yLo; y <= yHi; y++) {
                    double r = canopyRadius(tree, y);
                    boolean strand = false;
                    if (r < 0.0 && weeping && dist >= tree.radius() * 0.55 && dist <= tree.radius()) {
                        int hangTop = tree.top() - (int) (tree.radius() * 0.55);
                        int length = 2 + (int) (InnerWorldNoise.hash(seed + 8200, x, z) * 5.0);
                        strand = y <= hangTop && y > hangTop - length;
                    }
                    if (!strand && (r < 0.0 || dist > r + 0.3)) {
                        continue;
                    }
                    if (!strand && dist > r * 0.75 && InnerWorldNoise.hash3(seed + 8100, x, y, z) < 0.18) {
                        continue; // ragged outer edge
                    }
                    pos.set(x, y, z);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, leaves, SET_FLAGS);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ structural features

    private static int gridSize(FeatureType kind) {
        return switch (kind) {
            case SPIRES -> 22;
            case PILLARS -> 18;
            default -> 64;
        };
    }

    /** One structural feature, rolled once for its grid cell from the theme that owns the cell's centre. */
    private record GridFeature(Feature feature, Variant variant, double px, double pz, double radius, double height) {
    }

    private static final GridFeature NONE = new GridFeature(null, null, 0, 0, 0, 0);
    private final Map<Long, GridFeature> gridCache = new HashMap<>();

    private GridFeature gridFeature(FeatureType kind, int cx, int cz) {
        long key = ((long) kind.ordinal() << 58) ^ ((long) (cx & 0x1FFFFFFF) << 29) ^ (cz & 0x1FFFFFFFL);
        GridFeature cached = gridCache.get(key);
        if (cached != null) {
            return cached == NONE ? null : cached;
        }
        GridFeature made = computeGridFeature(kind, cx, cz);
        if (gridCache.size() > 40000) {
            gridCache.clear();
        }
        gridCache.put(key, made == null ? NONE : made);
        return made;
    }

    private GridFeature computeGridFeature(FeatureType kind, int cx, int cz) {
        long s = seed + 1000L + kind.ordinal() * 977L;
        double roll = InnerWorldNoise.hash(s, cx, cz);
        if (roll >= 0.45) {
            return null; // cheap pre-gate: densities above 0.45 are capped
        }
        int g = gridSize(kind);
        double px = (cx + 0.2 + 0.6 * InnerWorldNoise.hash(s + 1, cx, cz)) * g;
        double pz = (cz + 0.2 + 0.6 * InnerWorldNoise.hash(s + 2, cx, cz)) * g;
        double fromOrigin = Math.hypot(px, pz);
        if (fromOrigin < (kind == FeatureType.FLOATING_ISLANDS ? 60.0 : 26.0)) {
            return null;
        }
        Variant variant = regionAt((int) px, (int) pz, fromOrigin).a().variant();
        Feature found = null;
        for (Feature f : variant.features()) {
            if (f.type() == kind) {
                found = f;
                break;
            }
        }
        if (found == null || roll >= found.density()) {
            return null;
        }
        double radius = Mth.lerp(InnerWorldNoise.hash(s + 3, cx, cz), found.minSize(), found.maxSize());
        double height = Mth.lerp(InnerWorldNoise.hash(s + 4, cx, cz), found.minHeight(), found.maxHeight());
        return new GridFeature(found, variant, px, pz, radius, height);
    }

    private record Raise(int top, int mode, Block block, Palette palette) {
    }

    private static final FeatureType[] RAISE_KINDS = {FeatureType.SPIRES, FeatureType.PILLARS, FeatureType.MESAS};

    private Raise raiseAt(int lx, int lz, int groundTop) {
        Raise best = null;
        for (FeatureType kind : RAISE_KINDS) {
            int g = gridSize(kind);
            int gx = Math.floorDiv(lx, g);
            int gz = Math.floorDiv(lz, g);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    GridFeature gf = gridFeature(kind, gx + dx, gz + dz);
                    if (gf == null) {
                        continue;
                    }
                    double radius = kind == FeatureType.MESAS
                            ? gf.radius() * (0.92 + 0.16 * InnerWorldNoise.noise(seed + 90, lx / 9.0, lz / 9.0))
                            : gf.radius();
                    double d = Math.hypot(lx - gf.px(), lz - gf.pz());
                    if (d >= radius) {
                        continue;
                    }
                    int candidate = switch (kind) {
                        case SPIRES -> groundTop + (int) (gf.height() * Math.pow(1.0 - d / radius, 1.4));
                        case PILLARS -> groundTop + (int) gf.height();
                        default -> Math.max(groundTop, InnerWorld.SURFACE_Y + (int) gf.height());
                    };
                    if (candidate > groundTop && (best == null || candidate > best.top())) {
                        best = new Raise(candidate, kind == FeatureType.MESAS ? 2 : 1,
                                gf.feature().block().orElse(gf.variant().palette().stone()), gf.variant().palette());
                    }
                }
            }
        }
        return best;
    }

    private void islands(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int z, int lx, int lz) {
        int g = gridSize(FeatureType.FLOATING_ISLANDS);
        int gx = Math.floorDiv(lx, g);
        int gz = Math.floorDiv(lz, g);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                GridFeature gf = gridFeature(FeatureType.FLOATING_ISLANDS, gx + dx, gz + dz);
                if (gf == null) {
                    continue;
                }
                double radius = gf.radius() * (0.9 + 0.2 * InnerWorldNoise.noise(seed + 91, lx / 7.0, lz / 7.0));
                double d = Math.hypot(lx - gf.px(), lz - gf.pz());
                if (d >= radius * 0.97) {
                    continue;
                }
                Palette palette = gf.variant().palette();
                Block body = gf.feature().block().orElse(palette.stone());
                int centerY = InnerWorld.SURFACE_Y + (int) gf.height();
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

    // ------------------------------------------------------------------------------------------------ ores and treasure

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

    // ------------------------------------------------------------------------------------------------ herbs

    /** A herb site: deterministic per seed, so herbs can be placed at generation and regrown later at the exact same spots. */
    record HerbSite(InnerWorldProfile.HerbSpawn spawn, double patchLuck, Variant variant, Identifier path) {
    }

    HerbSite herbAt(int lx, int lz) {
        double dist = Math.sqrt((double) lx * lx + (double) lz * lz);
        if (dist <= 10.0) {
            return null;
        }
        Region region = regionAt(lx, lz, dist);
        Variant variant = region.dominant().variant();
        List<InnerWorldProfile.HerbSpawn> herbs = variant.resources().herbs();
        for (int i = 0; i < herbs.size(); i++) {
            if (InnerWorldNoise.hash(seed + 5000L + i * 7L, lx, lz) < herbs.get(i).density()) {
                double luck = InnerWorldNoise.hash(seed + 6000L, Math.floorDiv(lx, 24), Math.floorDiv(lz, 24));
                return new HerbSite(herbs.get(i), luck, variant, region.dominant().path());
            }
        }
        return null;
    }

    /** Deterministic per-column random seed for herb age/quality rolls. */
    long herbRollSeed(int lx, int lz) {
        return (long) (InnerWorldNoise.hash(seed + 7000L, lx, lz) * Long.MAX_VALUE);
    }

    // ------------------------------------------------------------------------------------------------ ambient qi

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
                Water w = d > 16.0 ? waterFor(regionAt(sx, sz, d), sx, sz, d) : null;
                if (w != null && w.margin() >= 0.0 && w.feature().type() == FeatureType.SPRINGS) {
                    springs++;
                }
            }
        }
        boolean strongest = dominant.path() != null && dominant.path().equals(corePath);
        return new QiInfo(dominant.variant(), dominant.path(), strongest, springs / 16.0);
    }
}