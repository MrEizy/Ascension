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
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.blocks.crops.herbs.LilyPadHerbCropBlock;
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

        if (herbBlock instanceof LilyPadHerbCropBlock lilyPadHerb) {
            return placeLilyPadHerb(level, random, config, definition, lilyPadHerb, origin);
        }

        int placed = 0;

        for (int i = 0; i < config.tries(); i++) {
            int x = origin.getX() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int z = origin.getZ() + random.nextInt(config.spread() * 2 + 1) - config.spread();
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            BlockState targetState = level.getBlockState(pos);
            if (!targetState.isAir() && !targetState.is(Blocks.SNOW)) {
                continue;
            }
            if (!definition.canSpawn(level, pos, random)) {
                continue;
            }
            if (!definition.canWildSurviveOn(level.getBlockState(pos.below()))) {
                continue;
            }

            int ageTier = definition.chooseWildAgeTier(random);
            int qualityTier = definition.chooseWildQualityTier(random);
            BlockState state = herbBlock.matureState(true, ageTier, qualityTier);
            if (!state.canSurvive(level, pos)) {
                continue;
            }

            level.setBlock(pos, state, 2);
            placed++;
        }

        return placed > 0;
    }


    private boolean placeLilyPadHerb(WorldGenLevel level, RandomSource random, Configuration config, HerbDefinition definition, LilyPadHerbCropBlock herbBlock, BlockPos origin) {
        int placed = 0;

        for (int i = 0; i < config.tries(); i++) {
            BlockPos pos = findLilyPad(level, origin, config.spread(), random);
            if (pos == null) {
                continue;
            }
            if (!definition.canWildSurviveOn(level.getBlockState(pos))) {
                continue;
            }
            if (!definition.canSpawn(level, pos.above(), random)) {
                continue;
            }
            if (!herbBlock.canConvertLilyPad(level, pos)) {
                continue;
            }

            int ageTier = definition.chooseWildAgeTier(random);
            int qualityTier = definition.chooseWildQualityTier(random);
            BlockState state = herbBlock.matureState(true, ageTier, qualityTier);
            state = herbBlock.rollWildVariant(state, random);

            if (herbBlock.placeOnLilyPad(level, pos, state)) {
                placed++;
            }
        }

        return placed > 0;
    }

    private BlockPos findLilyPad(WorldGenLevel level, BlockPos origin, int spread, RandomSource random) {
        BlockPos selected = null;
        int candidates = 0;

        for (int dx = -spread; dx <= spread; dx++) {
            for (int dz = -spread; dz <= spread; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

                for (int dy = 1; dy >= -3; dy--) {
                    BlockPos check = new BlockPos(x, surfaceY + dy, z);
                    if (!level.getBlockState(check).is(Blocks.LILY_PAD)) {
                        continue;
                    }

                    candidates++;
                    if (random.nextInt(candidates) == 0) {
                        selected = check;
                    }
                    break;
                }
            }
        }

        return selected;
    }

    public record Configuration(Block herbBlock, int tries, int spread) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("herb_block").forGetter(Configuration::herbBlock),
                Codec.intRange(1, 64).optionalFieldOf("tries", 8).forGetter(Configuration::tries),
                Codec.intRange(0, 16).optionalFieldOf("spread", 3).forGetter(Configuration::spread)
        ).apply(instance, Configuration::new));
    }
}
