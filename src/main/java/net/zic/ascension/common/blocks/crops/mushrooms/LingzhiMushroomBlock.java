package net.zic.ascension.common.blocks.crops.mushrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

public class LingzhiMushroomBlock extends HorizontalDirectionalBlock {

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(3, 9, 9, 13, 11, 16),
            Block.box(5, 8, 12, 11, 9, 16)
    );

    private static final Map<Direction, VoxelShape> SHAPES_BY_FACING = buildShapes();

    private final Predicate<BlockState> surviveOn;

    public LingzhiMushroomBlock(Properties properties, TagKey<Block> surviveTag) {
        this(properties, state -> state.is(surviveTag));
    }

    public LingzhiMushroomBlock(Properties properties, Block... surviveBlocks) {
        this(properties, blockSetPredicate(surviveBlocks));
    }

    public LingzhiMushroomBlock(Properties properties, Predicate<BlockState> surviveOn) {
        super(properties);
        this.surviveOn = surviveOn;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static Predicate<BlockState> blockSetPredicate(Block... blocks) {
        Set<Block> set = Set.of(blocks);
        return state -> set.contains(state.getBlock());
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_FACING.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        Direction clicked = context.getClickedFace();
        if (clicked.getAxis().isHorizontal() && isValidSupport(level, pos.relative(clicked.getOpposite()))) {
            return this.defaultBlockState().setValue(FACING, clicked);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (isValidSupport(level, pos.relative(direction.getOpposite()))) {
                return this.defaultBlockState().setValue(FACING, direction);
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return isValidSupport(level, pos.relative(facing.getOpposite()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction.getOpposite() == state.getValue(FACING) && !this.canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    private boolean isValidSupport(LevelReader level, BlockPos pos) {
        return surviveOn.test(level.getBlockState(pos));
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            map.put(direction, rotateShape(Direction.NORTH, direction, NORTH_SHAPE));
        }
        return map;
    }

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        VoxelShape current = shape;
        for (int i = 0; i < times; i++) {
            VoxelShape[] rotated = {Shapes.empty()};
            current.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    rotated[0] = Shapes.or(rotated[0], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            current = rotated[0];
        }
        return current;
    }
}
