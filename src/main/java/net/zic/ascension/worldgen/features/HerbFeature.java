package net.zic.ascension.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.herbs.HerbDefinition;

public class HerbFeature extends Feature<HerbFeature.Configuration> {
    public HerbFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Configuration config = context.config();

        if (!(config.herbBlock() instanceof HerbCropBlock herbBlock)) {
            return false;
        }

        HerbDefinition definition = herbBlock.definition();
        BlockPos origin = context.origin();
        int placed = 0;

        for (int i = 0; i < config.tries(); i++) {
            int x = origin.getX() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int z = origin.getZ() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            if (!level.isEmptyBlock(pos)) {
                continue;
            }
            if (!definition.canSpawn(level, pos, random)) {
                continue;
            }
            if (!definition.canWildSurviveOn(level.getBlockState(pos.below()))) {
                continue;
            }

            int ageTier = definition.chooseWildAgeTier(random);
            BlockState state = herbBlock.matureState(true, ageTier);
            if (!state.canSurvive(level, pos)) {
                continue;
            }

            level.setBlock(pos, state, 2);
            placed++;
        }

        return placed > 0;
    }

    public record Configuration(Block herbBlock, int tries, int spread) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("herb_block").forGetter(Configuration::herbBlock),
                Codec.intRange(1, 64).optionalFieldOf("tries", 8).forGetter(Configuration::tries),
                Codec.intRange(0, 16).optionalFieldOf("spread", 3).forGetter(Configuration::spread)
        ).apply(instance, Configuration::new));
    }
}
