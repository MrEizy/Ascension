package net.zic.ascension.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

/**
 * Small reusable surface boulder/crag feature.
 */
public final class SurfaceRockFeature extends Feature<SurfaceRockFeature.Configuration> {
    public SurfaceRockFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Configuration config = context.config();
        BlockPos origin = context.origin();

        int surfaceY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                origin.getX(),
                origin.getZ()
        );

        BlockPos ground = new BlockPos(origin.getX(), surfaceY - 1, origin.getZ());
        if (!canReplace(level.getBlockState(ground)) || !level.getFluidState(ground.above()).isEmpty()) {
            return false;
        }

        int radius = config.minRadius();
        if (config.maxRadius() > config.minRadius()) {
            radius += random.nextInt(config.maxRadius() - config.minRadius() + 1);
        }

        int height = 1 + random.nextInt(config.maxHeight());
        Block primary = config.blocks().get(random.nextInt(config.blocks().size()));
        int placed = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= height; dy++) {
                    double horizontal = (dx * dx + dz * dz) / (double) (radius * radius);
                    double vertical = (dy * dy) / (double) ((height + 0.5D) * (height + 0.5D));
                    double edgeNoise = random.nextDouble() * 0.22D;

                    if (horizontal + vertical > 1.0D + edgeNoise) {
                        continue;
                    }

                    BlockPos pos = ground.offset(dx, dy, dz);
                    BlockState existing = level.getBlockState(pos);
                    if (!canReplace(existing)) {
                        continue;
                    }

                    Block chosen = primary;
                    if (config.blocks().size() > 1 && random.nextFloat() < 0.18F) {
                        chosen = config.blocks().get(random.nextInt(config.blocks().size()));
                    }

                    level.setBlock(pos, chosen.defaultBlockState(), 2);
                    placed++;
                }
            }
        }

        return placed > 0;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir()
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK);
    }

    public record Configuration(
            List<Block> blocks,
            int minRadius,
            int maxRadius,
            int maxHeight
    ) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("blocks").forGetter(Configuration::blocks),
                Codec.intRange(1, 4).fieldOf("min_radius").forGetter(Configuration::minRadius),
                Codec.intRange(1, 5).fieldOf("max_radius").forGetter(Configuration::maxRadius),
                Codec.intRange(1, 4).fieldOf("max_height").forGetter(Configuration::maxHeight)
        ).apply(instance, Configuration::new));

        public Configuration {
            blocks = List.copyOf(blocks);
            if (blocks.isEmpty()) {
                throw new IllegalArgumentException("Surface rock feature requires at least one block");
            }
            if (maxRadius < minRadius) {
                throw new IllegalArgumentException("max_radius must be >= min_radius");
            }
        }
    }
}
