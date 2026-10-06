package net.zic.ascension.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * A larger version of vanilla's lake (up to 44x44 instead of 16x16) built from many overlapping blobs.
 *
 * <p>Like vanilla, the whole lake is cancelled if any block around or under the water is not solid, so on
 * floating islands it never generates where it could spill over an edge, through an underside or into a cave.
 * Unlike vanilla, only the shell under the waterline is re-lined, so grass shores stay intact.
 *
 * <p>The lake is centred on its chunk so it stays inside the area a feature may write to; place it without in_square.
 */
public class ContainedLakeFeature extends Feature<ContainedLakeFeature.Configuration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    /** Features may write one chunk around their own; a centred lake of this width (plus its shell) still fits. */
    public static final int MAX_WIDTH = 44;

    public ContainedLakeFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    @SuppressWarnings("deprecation") //liquid() and isSolid() have no replacement; vanilla LakeFeature uses them too
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Configuration config = context.config();
        int width = config.width();
        int depth = config.depth();
        //the grid is as tall above the waterline as it is deep, so the bank is cut back as far as the water goes down
        int height = depth * 2;

        int centreX = SectionPos.blockToSectionCoord(context.origin().getX()) * 16 + 8;
        int centreZ = SectionPos.blockToSectionCoord(context.origin().getZ()) * 16 + 8;
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centreX, centreZ);
        if (surface <= level.getMinY() + depth + 2) {
            return false;
        }
        //water surface replaces the top terrain layer, as in vanilla
        BlockPos origin = new BlockPos(centreX - width / 2, surface - depth, centreZ - width / 2);

        boolean[] grid = new boolean[width * width * height];
        int spots = config.spots().sample(random);
        for (int i = 0; i < spots; i++) {
            double xr = (random.nextDouble() * 0.37D + 0.19D) * width;
            double zr = (random.nextDouble() * 0.37D + 0.19D) * width;
            double yr = random.nextDouble() * (depth - 1) + 2.0D;
            double xp = random.nextDouble() * (width - xr - 2.0D) + 1.0D + xr / 2.0D;
            double yp = random.nextDouble() * (height - yr - depth) + 2.0D + yr / 2.0D;
            double zp = random.nextDouble() * (width - zr - 2.0D) + 1.0D + zr / 2.0D;

            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < width - 1; z++) {
                    for (int y = 1; y < height - 1; y++) {
                        double xd = (x - xp) / (xr / 2.0D);
                        double yd = (y - yp) / (yr / 2.0D);
                        double zd = (z - zp) / (zr / 2.0D);
                        if (xd * xd + yd * yd + zd * zd < 1.0D) {
                            grid[index(x, y, z, width, height)] = true;
                        }
                    }
                }
            }
        }

        BlockState fluid = config.fluid().getState(level, random, origin);

        //a neighbouring chunk may already have its trees; carving through them leaves floating leaves that decay into item drops
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                for (int y = 0; y < height; y++) {
                    if (grid[index(x, y, z, width, height)] && isPlantStructure(level.getBlockState(origin.offset(x, y, z)))) {
                        return false;
                    }
                }
            }
        }

        //containment: nothing around the water may be open, and nothing around the cut-out above it may be liquid
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                for (int y = 0; y < height; y++) {
                    if (!isShell(grid, x, y, z, width, height)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (y >= depth && state.liquid()) {
                        return false;
                    }
                    if (y < depth && !state.isSolid() && state != fluid) {
                        return false;
                    }
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                for (int y = 0; y < height; y++) {
                    if (!grid[index(x, y, z, width, height)]) {
                        continue;
                    }
                    BlockPos pos = origin.offset(x, y, z);
                    if (canReplaceBlock(level.getBlockState(pos))) {
                        boolean placeAir = y >= depth;
                        level.setBlock(pos, placeAir ? AIR : fluid, 2);
                        if (placeAir) {
                            level.scheduleTick(pos, AIR.getBlock(), 0);
                            this.markAboveForPostProcessing(level, pos);
                        }
                    }
                }
            }
        }

        //re-line the lake bed and walls under the waterline only
        BlockState shore = config.shore().getState(level, random, origin);
        if (!shore.isAir()) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < width; z++) {
                    for (int y = 0; y < depth; y++) {
                        if (!isShell(grid, x, y, z, width, height)) {
                            continue;
                        }
                        BlockPos pos = origin.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (state.isSolid() && !state.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                            level.setBlock(pos, shore, 2);
                        }
                    }
                }
            }
        }

        if (fluid.getFluidState().is(FluidTags.WATER)) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < width; z++) {
                    BlockPos pos = origin.offset(x, depth, z);
                    if (level.getBiome(pos).value().shouldFreeze(level, pos, false) && canReplaceBlock(level.getBlockState(pos))) {
                        level.setBlock(pos, Blocks.ICE.defaultBlockState(), 2);
                    }
                }
            }
        }

        return true;
    }

    /** A cell outside the lake that touches it on one of its six sides. */
    private static boolean isShell(boolean[] grid, int x, int y, int z, int width, int height) {
        if (grid[index(x, y, z, width, height)]) {
            return false;
        }
        return x < width - 1 && grid[index(x + 1, y, z, width, height)]
                || x > 0 && grid[index(x - 1, y, z, width, height)]
                || z < width - 1 && grid[index(x, y, z + 1, width, height)]
                || z > 0 && grid[index(x, y, z - 1, width, height)]
                || y < height - 1 && grid[index(x, y + 1, z, width, height)]
                || y > 0 && grid[index(x, y - 1, z, width, height)];
    }

    private static int index(int x, int y, int z, int width, int height) {
        return (x * width + z) * height + y;
    }

    private static boolean isPlantStructure(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.BAMBOO);
    }

    private static boolean canReplaceBlock(BlockState state) {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    /**
     * @param shore lines the lake bed and walls under the waterline (air keeps the existing blocks)
     * @param width lake grid width in blocks, at most {@link #MAX_WIDTH}
     * @param depth maximum water depth
     * @param spots number of overlapping blobs that make up the lake
     */
    public record Configuration(BlockStateProvider fluid, BlockStateProvider shore, int width, int depth, IntProvider spots)
            implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockStateProvider.CODEC.fieldOf("fluid").forGetter(Configuration::fluid),
                BlockStateProvider.CODEC.fieldOf("shore").forGetter(Configuration::shore),
                Codec.intRange(16, MAX_WIDTH).optionalFieldOf("width", 40).forGetter(Configuration::width),
                Codec.intRange(3, 10).optionalFieldOf("depth", 5).forGetter(Configuration::depth),
                IntProviders.codec(1, 64).optionalFieldOf("spots", UniformInt.of(8, 14)).forGetter(Configuration::spots)
        ).apply(instance, Configuration::new));
    }
}
