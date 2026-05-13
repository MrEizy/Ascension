package net.thejadeproject.ascension.common.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.thejadeproject.ascension.common.blocks.entity.ModBlockEntities;
import net.thejadeproject.ascension.common.blocks.entity.SpiritVeinBlockEntity;
import org.jetbrains.annotations.Nullable;


public class SpiritVeinBlock extends Block implements EntityBlock {
    public static final IntegerProperty VEIN_LEVEL = IntegerProperty.create("vein", 0, 3);
    public static final int MAX_VEIN = 3;
    public static final Direction[] SPREAD_DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };
    public static final int CYCLE_TICKS = 24000;


    public SpiritVeinBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(-1.0f, 3600000.0f)
                .sound(SoundType.DEEPSLATE)
        );

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VEIN_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VEIN_LEVEL);
    }
    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return state.getValue(VEIN_LEVEL) * 5;
    }

    public static float getVeinProgress(BlockState state, long gameTime) {
        int vein = state.getValue(VEIN_LEVEL);
        if (vein == MAX_VEIN) return 1.0f;

        return vein / (float)MAX_VEIN + 0.1f * Mth.sin(gameTime * 0.05f);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritVeinBlockEntity(pos, state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.SPIRIT_VEIN_BE.get(),
                SpiritVeinBlockEntity::tick);
    }

    private static <E extends BlockEntity, A extends BlockEntity>
    BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> type,
                                            BlockEntityType<E> targetType,
                                            BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>)ticker : null;
    }
}
