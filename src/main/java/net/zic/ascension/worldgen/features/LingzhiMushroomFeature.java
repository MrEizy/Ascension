package net.zic.ascension.worldgen.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LingzhiMushroomFeature extends Feature<LingzhiMushroomConfiguration> {

    public LingzhiMushroomFeature(Codec<LingzhiMushroomConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LingzhiMushroomConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        LingzhiMushroomConfiguration config = context.config();

        List<BlockPos> supports = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos check = origin.offset(x, y, z);
                    if (config.validSupports().contains(level.getBlockState(check).getBlock().builtInRegistryHolder())) {
                        supports.add(check);
                    }
                }
            }
        }

        if (supports.isEmpty()) {
            return false;
        }

        Collections.shuffle(supports, new java.util.Random(random.nextLong()));

        for (BlockPos supportPos : supports) {
            List<Direction> dirs = new ArrayList<>(List.of(
                    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
            ));
            Collections.shuffle(dirs, new java.util.Random(random.nextLong()));

            for (Direction dir : dirs) {
                BlockPos placePos = supportPos.relative(dir);

                if (!level.isEmptyBlock(placePos)) {
                    continue;
                }

                BlockState state = config.mushroomBlock().defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, dir);

                if (state.canSurvive(level, placePos)) {
                    level.setBlock(placePos, state, 2);
                    return true;
                }
            }
        }
        return false;
    }
}