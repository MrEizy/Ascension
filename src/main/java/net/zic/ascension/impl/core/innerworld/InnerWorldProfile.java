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

    /**
     * density: chance per grid cell (spires, pillars, mesas, islands), share of area (pools), or tunnel/cut width (caves, ravines).
     * min/max_size: radius in blocks. min/max_height: spire/pillar/mesa height, island altitude, or ravine depth.
     */
    public record Feature(FeatureType type, Optional<Block> block, double density, int minSize, int maxSize, int minHeight, int maxHeight) {
        public static final Codec<Feature> CODEC = RecordCodecBuilder.create(i -> i.group(
                FeatureType.CODEC.fieldOf("type").forGetter(Feature::type),
                BLOCK.optionalFieldOf("block").forGetter(Feature::block),
                Codec.DOUBLE.optionalFieldOf("density", 0.15).forGetter(Feature::density),
                Codec.INT.optionalFieldOf("min_size", 3).forGetter(Feature::minSize),
                Codec.INT.optionalFieldOf("max_size", 8).forGetter(Feature::maxSize),
                Codec.INT.optionalFieldOf("min_height", 8).forGetter(Feature::minHeight),
                Codec.INT.optionalFieldOf("max_height", 24).forGetter(Feature::maxHeight)
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

    public record Resources(List<Ore> ores, List<Treasure> treasure) {
        public static final Resources NONE = new Resources(List.of(), List.of());
        public static final Codec<Resources> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ore.CODEC.listOf().optionalFieldOf("ores", List.of()).forGetter(Resources::ores),
                Treasure.CODEC.listOf().optionalFieldOf("treasure", List.of()).forGetter(Resources::treasure)
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

    /** biome: atmosphere biome (sky, fog, particles) applied in the biome step; terrain itself is always hand-placed. */
    public record Variant(double weight, Optional<Identifier> biome, Palette palette, Terrain terrain, List<Feature> features,
                          Qi qi, Resources resources) {
        public static final Codec<Variant> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(Variant::weight),
                Identifier.CODEC.optionalFieldOf("biome").forGetter(Variant::biome),
                Palette.CODEC.fieldOf("palette").forGetter(Variant::palette),
                Terrain.CODEC.optionalFieldOf("terrain", Terrain.DEFAULT).forGetter(Variant::terrain),
                Feature.CODEC.listOf().optionalFieldOf("features", List.of()).forGetter(Variant::features),
                Qi.CODEC.optionalFieldOf("qi", Qi.DEFAULT).forGetter(Variant::qi),
                Resources.CODEC.optionalFieldOf("resources", Resources.NONE).forGetter(Variant::resources)
        ).apply(i, Variant::new));

        /** Used only if a seeded theme's profile has vanished from the datapack, so a world never fails to generate. */
        public static Variant fallback() {
            return new Variant(1.0, Optional.empty(),
                    new Palette(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.STONE, Blocks.DEEPSLATE, List.of(),
                            List.of(new Decor(Blocks.SHORT_GRASS, 0.07))),
                    Terrain.DEFAULT, List.of(), Qi.DEFAULT, Resources.NONE);
        }
    }
}