package net.zic.ascension.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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

public final class SurfaceScatterFeature extends Feature<SurfaceScatterFeature.Configuration> {
    public SurfaceScatterFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Configuration config = context.config();
        BlockPos origin = context.origin();
        int placed = 0;

        for (int i = 0; i < config.tries(); i++) {
            int x = origin.getX() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int z = origin.getZ() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            BlockState target = level.getBlockState(pos);
            if (!target.isAir() && !target.is(Blocks.SNOW)) {
                continue;
            }

            Block block = config.blocks().get(random.nextInt(config.blocks().size()));
            BlockState state = block.defaultBlockState();
            if (!state.canSurvive(level, pos)) {
                continue;
            }

            level.setBlock(pos, state, 2);
            placed++;
        }

        return placed > 0;
    }

    public record Configuration(
            List<Block> blocks,
            int tries,
            int spread
    ) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("blocks").forGetter(Configuration::blocks),
                Codec.intRange(1, 96).optionalFieldOf("tries", 24).forGetter(Configuration::tries),
                Codec.intRange(1, 16).optionalFieldOf("spread", 6).forGetter(Configuration::spread)
        ).apply(instance, Configuration::new));

        public Configuration {
            blocks = List.copyOf(blocks);
            if (blocks.isEmpty()) {
                throw new IllegalArgumentException("Surface scatter feature requires at least one block");
            }
        }
    }
}
