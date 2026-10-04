package net.zic.ascension.common.blocks.crops.herbs;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.herbs.HerbDefinition;

import java.util.function.Supplier;

public class LilyPadHerbCropBlock extends HerbCropBlock {
    public static final BooleanProperty RARE_VARIANT = BooleanProperty.create("rare_variant");

    private final Supplier<? extends Item> rareHarvestItem;
    private final int rareVariantChance;

    public LilyPadHerbCropBlock(
            Properties properties,
            HerbDefinition definition,
            Supplier<? extends Item> harvestItem,
            Supplier<? extends Item> rareHarvestItem,
            int rareVariantChance
    ) {
        super(properties, definition, harvestItem);
        this.rareHarvestItem = rareHarvestItem;
        this.rareVariantChance = Math.max(0, rareVariantChance);
        registerDefaultState(defaultBlockState().setValue(RARE_VARIANT, false));
    }

    public BlockState rollWildVariant(BlockState state, RandomSource random) {
        if (!isMature(state) || rareVariantChance <= 0) {
            return state.setValue(RARE_VARIANT, false);
        }
        return state.setValue(RARE_VARIANT, random.nextInt(rareVariantChance) == 0);
    }

    public boolean canConvertLilyPad(LevelReader level, BlockPos plantPos) {
        if (!level.getBlockState(plantPos).is(Blocks.LILY_PAD)) {
            return false;
        }
        FluidState fluid = level.getFluidState(plantPos.below());
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }

    public boolean canPlantAboveSupport(LevelReader level, BlockPos supportPos) {
        BlockState support = level.getBlockState(supportPos);
        return support.is(ModBlocks.LOTUS_PAD_SUPPORT.get())
                && support.getValue(LotusPadSupportBlock.WATERLOGGED)
                && level.getBlockState(supportPos.above()).isAir();
    }

    public boolean placeOnLilyPad(LevelAccessor level, BlockPos plantPos, BlockState state) {
        if (!canConvertLilyPad(level, plantPos)) {
            return false;
        }

        BlockPos supportPos = plantPos.below();
        BlockState supportState = ModBlocks.LOTUS_PAD_SUPPORT.get().defaultBlockState()
                .setValue(LotusPadSupportBlock.WATERLOGGED, true);

        level.setBlock(supportPos, supportState, 2);
        if (!state.canSurvive(level, plantPos)) {
            level.setBlock(supportPos, Blocks.WATER.defaultBlockState(), 2);
            level.setBlock(plantPos, Blocks.LILY_PAD.defaultBlockState(), 2);
            return false;
        }

        if (!level.setBlock(plantPos, state, 2)) {
            level.setBlock(supportPos, Blocks.WATER.defaultBlockState(), 2);
            level.setBlock(plantPos, Blocks.LILY_PAD.defaultBlockState(), 2);
            return false;
        }
        return true;
    }

    public boolean placeAboveSupport(LevelAccessor level, BlockPos supportPos, BlockState state) {
        if (!canPlantAboveSupport(level, supportPos)) {
            return false;
        }
        return level.setBlock(supportPos.above(), state, 3);
    }

    @Override
    protected Item harvestItem(BlockState state) {
        if (state.getValue(RARE_VARIANT)) {
            return rareHarvestItem.get();
        }
        return super.harvestItem(state);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState support = level.getBlockState(pos.below());
        return support.is(ModBlocks.LOTUS_PAD_SUPPORT.get())
                && support.getValue(LotusPadSupportBlock.WATERLOGGED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RARE_VARIANT);
    }
}
