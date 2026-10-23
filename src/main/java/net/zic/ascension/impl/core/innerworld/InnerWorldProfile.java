package net.zic.ascension.impl.core.innerworld;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * What a cultivation path does to an Inner World. One datapack JSON per path:
 *   data/<ns>/ascension/inner_world_profile/<name>.json
 * A profile holds several VARIANTS (themes). A player's seed picks 3-5 variants from the profiles of
 * the paths they cultivate, weighted by how far along each path is, so ship at least 3 variants per path.
 *
 * Every field except the nested "path" and "variants" is optional with a sensible default, so a variant
 * can be as small as a palette.
 */
public record InnerWorldProfile(Identifier path, double weightMultiplier, List<Variant> variants) {

    /**
     * MUST stay above CODEC. CODEC's initializer builds the nested codecs (Palette, Decor, Feature...), which read this
     * field; if it were declared after CODEC it would still be null at that point and class init would fail.
     */
    private static final Codec<Block> BLOCK = BuiltInRegistries.BLOCK.byNameCodec();

    public static final Codec<InnerWorldProfile> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("path").forGetter(InnerWorldProfile::path),
            Codec.DOUBLE.optionalFieldOf("weight_multiplier", 1.0).forGetter(InnerWorldProfile::weightMultiplier),
            Variant.CODEC.listOf().fieldOf("variants").forGetter(InnerWorldProfile::variants)
    ).apply(i, InnerWorldProfile::new));

    /** A decoration placed on top of the surface, e.g. {"block":"minecraft:fern","chance":0.05}. Chances stack in list order. */
    public record Decor(Block block, double chance) {
        public static final Codec<Decor> CODEC = RecordCodecBuilder.create(i -> i.group(
                BLOCK.fieldOf("block").forGetter(Decor::block),
                Codec.DOUBLE.fieldOf("chance").forGetter(Decor::chance)
        ).apply(i, Decor::new));
    }

    /** "bands" (optional) replaces the stone layer with a repeating vertical stripe, for badlands-style cliffs. */
    public record Palette(Block top, Block sub, Block stone, Block deep, List<Block> bands, List<Decor> decor) {
        public static final Codec<Palette> CODEC = RecordCodecBuilder.create(i -> i.group(
                BLOCK.fieldOf("top").forGetter(Palette::top),
                BLOCK.optionalFieldOf("sub", Blocks.DIRT).forGetter(Palette::sub),
                BLOCK.optionalFieldOf("stone", Blocks.STONE).forGetter(Palette::stone),
                BLOCK.optionalFieldOf("deep", Blocks.DEEPSLATE).forGetter(Palette::deep),
                BLOCK.listOf().optionalFieldOf("bands", List.of()).forGetter(Palette::bands),
                Decor.CODEC.listOf().optionalFieldOf("decor", List.of()).forGetter(Palette::decor)
        ).apply(i, Palette::new));
    }

    /**
     * offset: raises/lowers the whole region (blocks). amplitude: hill height. frequency: feature size multiplier
     * (higher = smaller, busier hills). terrace: if above 0, heights snap to steps of this size (plateaus).
     */
    public record Terrain(double offset, double amplitude, double frequency, int terrace) {
        public static final Terrain DEFAULT = new Terrain(0.0, 28.0, 1.0, 0);
        public static final Codec<Terrain> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.optionalFieldOf("offset", 0.0).forGetter(Terrain::offset),
                Codec.DOUBLE.optionalFieldOf("amplitude", 28.0).forGetter(Terrain::amplitude),
                Codec.DOUBLE.optionalFieldOf("frequency", 1.0).forGetter(Terrain::frequency),
                Codec.INT.optionalFieldOf("terrace", 0).forGetter(Terrain::terrace)
        ).apply(i, Terrain::new));
    }

    public enum FeatureType {
        /** Pools of a themed fluid. block = the fluid (water, lava, a custom liquid block). density 0-1 = share of area. */
        POOLS,
        /**
         * Spirit springs: pools of liquified spiritual qi (block defaults to it). Same shape as POOLS, but flagged as springs
         * so ambient qi concentrates around them and the spring effects apply inside them.
         */
        SPRINGS,
        /** Tapering rock spires. block = optional material override. */
        SPIRES,
        /** Constant-width pillars. */
        PILLARS,
        /** Flat-topped plateaus with cliff walls. */
        MESAS,
        /** Lens-shaped islands hovering above the ground. min/max_height = altitude above the surface. */
        FLOATING_ISLANDS,
        /** Winding channels of fluid that meander across the region, with sloped banks. block = the fluid. density = width. */
        RIVERS,
        /**
         * Trees. block = the log, leaves = the canopy block (leave it out for a bare dead tree). density = chance per ~6 block cell.
         * min/max_size = canopy radius (max 12), min/max_height = trunk height, shape = round | cone | dome | umbrella | weeping,
         * width = 1 or 2 (2 = a thick 2x2 trunk, for big trees). A region can list several tree features (e.g. big oaks plus small birches).
         */
        TREES,
        /** Winding underground tunnels. */
        CAVES,
        /** Deep narrow cuts into the surface. min/max_height = depth. */
        RAVINES;

        public static final Codec<FeatureType> CODEC = Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(valueOf(s.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown inner world feature type: " + s);
            }
        }, t -> t.name().toLowerCase(Locale.ROOT));
    }

    public enum TreeShape {
        ROUND, CONE, DOME, UMBRELLA, WEEPING;

        public static final Codec<TreeShape> CODEC = Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(valueOf(s.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown tree shape: " + s);
            }
        }, t -> t.name().toLowerCase(Locale.ROOT));
    }

    /**
     * density: chance per grid cell (spires, pillars, mesas, islands), share of area (pools), or tunnel/cut width (caves, ravines).
     * shore (pools, springs, rivers): the bank and bed block; defaults by fluid (sand for water, basalt for lava, calcite for springs).
     * min/max_size: radius in blocks. min/max_height: spire/pillar/mesa height, island altitude, or ravine depth.
     */
    public record Feature(FeatureType type, Optional<Block> block, double density, int minSize, int maxSize, int minHeight, int maxHeight,
                          Optional<Block> shore, Optional<Block> leaves, TreeShape shape, int width) {
        public static final Codec<Feature> CODEC = RecordCodecBuilder.create(i -> i.group(
                FeatureType.CODEC.fieldOf("type").forGetter(Feature::type),
                BLOCK.optionalFieldOf("block").forGetter(Feature::block),
                Codec.DOUBLE.optionalFieldOf("density", 0.15).forGetter(Feature::density),
                Codec.INT.optionalFieldOf("min_size", 3).forGetter(Feature::minSize),
                Codec.INT.optionalFieldOf("max_size", 8).forGetter(Feature::maxSize),
                Codec.INT.optionalFieldOf("min_height", 8).forGetter(Feature::minHeight),
                Codec.INT.optionalFieldOf("max_height", 24).forGetter(Feature::maxHeight),
                BLOCK.optionalFieldOf("shore").forGetter(Feature::shore),
                BLOCK.optionalFieldOf("leaves").forGetter(Feature::leaves),
                TreeShape.CODEC.optionalFieldOf("shape", TreeShape.ROUND).forGetter(Feature::shape),
                Codec.INT.optionalFieldOf("width", 1).forGetter(Feature::width)
        ).apply(i, Feature::new));
    }

    /**
     * Blobby vein ore replacing stone. density ~0.02 = rare, ~0.1 = common. min/max_depth = blocks below the local surface.
     * "scale" is the vein blob size (bigger = larger, rarer-looking clumps).
     */
    public record Ore(Block block, double density, int minDepth, int maxDepth, double scale) {
        public static final Codec<Ore> CODEC = RecordCodecBuilder.create(i -> i.group(
                BLOCK.fieldOf("block").forGetter(Ore::block),
                Codec.DOUBLE.optionalFieldOf("density", 0.05).forGetter(Ore::density),
                Codec.INT.optionalFieldOf("min_depth", 4).forGetter(Ore::minDepth),
                Codec.INT.optionalFieldOf("max_depth", 30).forGetter(Ore::maxDepth),
                Codec.DOUBLE.optionalFieldOf("scale", 5.0).forGetter(Ore::scale)
        ).apply(i, Ore::new));
    }

    /**
     * A rare buried treasure deposit unique to the seed. density = chance per ~80 block grid cell. depth = blocks below
     * SURFACE_Y at the centre. radius = size of the clump. weight picks between entries when a cell rolls a vein.
     */
    public record Treasure(Block block, double density, int minDepth, int maxDepth, int radius, double weight) {
        public static final Codec<Treasure> CODEC = RecordCodecBuilder.create(i -> i.group(
                BLOCK.fieldOf("block").forGetter(Treasure::block),
                Codec.DOUBLE.optionalFieldOf("density", 0.12).forGetter(Treasure::density),
                Codec.INT.optionalFieldOf("min_depth", 12).forGetter(Treasure::minDepth),
                Codec.INT.optionalFieldOf("max_depth", 28).forGetter(Treasure::maxDepth),
                Codec.INT.optionalFieldOf("radius", 2).forGetter(Treasure::radius),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(Treasure::weight)
        ).apply(i, Treasure::new));
    }

    /**
     * A wild herb that can grow in this region. herb = a ModHerbs id. density = chance per surface column. The herb only
     * appears where its own naturalSupport accepts the surface block, so list herbs that fit the palette's top block
     * (e.g. fire herbs on sand). quality_bonus adds flat quality tiers on top of the tier, patch luck and the herb's own wild weights.
     */
    public record HerbSpawn(Identifier herb, double density, int qualityBonus) {
        public static final Codec<HerbSpawn> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("herb").forGetter(HerbSpawn::herb),
                Codec.DOUBLE.optionalFieldOf("density", 0.005).forGetter(HerbSpawn::density),
                Codec.INT.optionalFieldOf("quality_bonus", 0).forGetter(HerbSpawn::qualityBonus)
        ).apply(i, HerbSpawn::new));
    }

    public record Resources(List<Ore> ores, List<Treasure> treasure, List<HerbSpawn> herbs) {
        public static final Resources NONE = new Resources(List.of(), List.of(), List.of());
        public static final Codec<Resources> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ore.CODEC.listOf().optionalFieldOf("ores", List.of()).forGetter(Resources::ores),
                Treasure.CODEC.listOf().optionalFieldOf("treasure", List.of()).forGetter(Resources::treasure),
                HerbSpawn.CODEC.listOf().optionalFieldOf("herbs", List.of()).forGetter(Resources::herbs)
        ).apply(i, Resources::new));
    }

    /**
     * Ambient atmospheric qi for chunks of this region (base values, before scaling by the world's tier, the strongest-path
     * bonus and proximity to springs). affinity = how strongly the region's path is felt in the air.
     */
    public record Qi(long capacity, long regen, double affinity) {
        public static final Qi DEFAULT = new Qi(300L, 3L, 1.0);
        public static final Codec<Qi> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.optionalFieldOf("capacity", 300L).forGetter(Qi::capacity),
                Codec.LONG.optionalFieldOf("regen", 3L).forGetter(Qi::regen),
                Codec.DOUBLE.optionalFieldOf("affinity", 1.0).forGetter(Qi::affinity)
        ).apply(i, Qi::new));
    }

    /**
     * One mob this region can spawn. entity = any entity type id (vanilla or modded). weight = relative chance. The group is
     * rolled between min_group and max_group. min_realm / max_realm (optional) override the region's mob cultivation range
     * for just this mob, e.g. a boss-like mob that only ever appears at higher realms.
     */
    public record MobSpawn(Identifier entity, double weight, int minGroup, int maxGroup, Optional<Integer> minRealm, Optional<Integer> maxRealm) {
        public static final Codec<MobSpawn> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("entity").forGetter(MobSpawn::entity),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(MobSpawn::weight),
                Codec.INT.optionalFieldOf("min_group", 1).forGetter(MobSpawn::minGroup),
                Codec.INT.optionalFieldOf("max_group", 3).forGetter(MobSpawn::maxGroup),
                Codec.INT.optionalFieldOf("min_realm").forGetter(MobSpawn::minRealm),
                Codec.INT.optionalFieldOf("max_realm").forGetter(MobSpawn::maxRealm)
        ).apply(i, MobSpawn::new));
    }

    /**
     * Mob spawning for a region. min_realm / max_realm = the mob cultivation realm range mobs roll from WITHOUT the Life Dao;
     * max_realm is the per-theme cap. With the Life Dao (life path at major realm 1+) the range is multiplied:
     * scale(v) = (v + 1) x (1 + life_multiplier_per_realm x lifeMajorRealm) - 1, so mobs also START at a higher realm.
     */
    public record Mobs(int minRealm, int maxRealm, double lifeMultiplierPerRealm, List<MobSpawn> spawns) {
        public static final Mobs NONE = new Mobs(0, 2, 0.5, List.of());
        public static final Codec<Mobs> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("min_realm", 0).forGetter(Mobs::minRealm),
                Codec.INT.optionalFieldOf("max_realm", 2).forGetter(Mobs::maxRealm),
                Codec.DOUBLE.optionalFieldOf("life_multiplier_per_realm", 0.5).forGetter(Mobs::lifeMultiplierPerRealm),
                MobSpawn.CODEC.listOf().optionalFieldOf("spawns", List.of()).forGetter(Mobs::spawns)
        ).apply(i, Mobs::new));
    }

    /**
     * A structure this region can hold (any structure id, vanilla or modded). requires_life = only generates for a player with
     * the Life Dao (default true); set false for the few simple ones everyone gets (ruins, wells). spacing = blocks between
     * attempts. NOTE: structure generation itself needs the custom chunk generator; until that exists this is data only.
     */
    public record StructureEntry(Identifier structure, double weight, boolean requiresLife, int spacing) {
        public static final Codec<StructureEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("structure").forGetter(StructureEntry::structure),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(StructureEntry::weight),
                Codec.BOOL.optionalFieldOf("requires_life", true).forGetter(StructureEntry::requiresLife),
                Codec.INT.optionalFieldOf("spacing", 192).forGetter(StructureEntry::spacing)
        ).apply(i, StructureEntry::new));
    }

    /** biome: atmosphere biome (sky, fog, particles) applied in the biome step; terrain itself is always hand-placed. */
    public record Variant(double weight, Optional<Identifier> biome, Palette palette, Terrain terrain, List<Feature> features,
                          Qi qi, Resources resources, Mobs mobs, List<StructureEntry> structures) {
        public static final Codec<Variant> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(Variant::weight),
                Identifier.CODEC.optionalFieldOf("biome").forGetter(Variant::biome),
                Palette.CODEC.fieldOf("palette").forGetter(Variant::palette),
                Terrain.CODEC.optionalFieldOf("terrain", Terrain.DEFAULT).forGetter(Variant::terrain),
                Feature.CODEC.listOf().optionalFieldOf("features", List.of()).forGetter(Variant::features),
                Qi.CODEC.optionalFieldOf("qi", Qi.DEFAULT).forGetter(Variant::qi),
                Resources.CODEC.optionalFieldOf("resources", Resources.NONE).forGetter(Variant::resources),
                Mobs.CODEC.optionalFieldOf("mobs", Mobs.NONE).forGetter(Variant::mobs),
                StructureEntry.CODEC.listOf().optionalFieldOf("structures", List.of()).forGetter(Variant::structures)
        ).apply(i, Variant::new));

        /** Used only if a seeded theme's profile has vanished from the datapack, so a world never fails to generate. */
        public static Variant fallback() {
            return new Variant(1.0, Optional.empty(),
                    new Palette(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.STONE, Blocks.DEEPSLATE, List.of(),
                            List.of(new Decor(Blocks.SHORT_GRASS, 0.07))),
                    Terrain.DEFAULT, List.of(), Qi.DEFAULT, Resources.NONE, Mobs.NONE, List.of());
        }
    }
}