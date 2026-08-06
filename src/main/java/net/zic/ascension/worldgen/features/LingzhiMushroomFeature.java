package net.zic.ascension.worldgen.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.zic.ascension.common.blocks.ModBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LingzhiMushroomFeature extends Feature<NoneFeatureConfiguration> {

    public LingzhiMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();


        List<BlockPos> logs = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos check = origin.offset(x, y, z);
                    if (level.getBlockState(check).is(BlockTags.LOGS)) {
                        logs.add(check);
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            return false;
        }

        Collections.shuffle(logs, new java.util.Random(random.nextLong()));

        for (BlockPos logPos : logs) {
            List<Direction> dirs = new ArrayList<>(List.of(
                    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
            ));
            Collections.shuffle(dirs, new java.util.Random(random.nextLong()));

            for (Direction dir : dirs) {
                BlockPos placePos = logPos.relative(dir);

                if (!level.isEmptyBlock(placePos)) {
                    continue;
                }

                BlockState state = ModBlocks.LINGZHI_MUSHROOM_B.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, dir);

                if (state.canSurvive(level, placePos)) {
                    level.setBlock(placePos, state, 2);
                    return true;
                }
            }
        }
        return false;
    }
}