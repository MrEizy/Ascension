package net.zic.ascension.common.blocks.crops.herbs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zic.ascension.common.herbs.HerbDefinition;

import java.util.function.Supplier;

public class HerbStemCropBlock extends HerbCropBlock {

    private static final VoxelShape[] SHAPE_BY_AGE = {
            Block.box(7, 0, 7, 9, 2, 9),
            Block.box(7, 0, 7, 9, 4, 9),
            Block.box(7, 0, 7, 9, 6, 9),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D)
    };

    public HerbStemCropBlock(Properties properties, HerbDefinition definition, Supplier<? extends Item> harvestItem) {
        super(properties, definition, harvestItem);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[Math.min(growthStage(state), SHAPE_BY_AGE.length - 1)];
    }
}
