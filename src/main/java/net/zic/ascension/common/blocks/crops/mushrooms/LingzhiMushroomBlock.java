package net.zic.ascension.common.blocks.crops.mushrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.components.AscensionComponents;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LingzhiMushroomBlock extends HorizontalDirectionalBlock {
    public static final IntegerProperty AGE_TIER = IntegerProperty.create("age_tier", 0, HerbDefinition.MAX_AGE_TIERS - 1);
    public static final BooleanProperty WILD = BooleanProperty.create("wild");

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(3, 9, 9, 13, 11, 16),
            Block.box(5, 8, 12, 11, 9, 16)
    );

    private static final Map<Direction, VoxelShape> SHAPES_BY_FACING = buildShapes();

    private final HerbDefinition definition;
    private final Supplier<? extends Item> harvestItem;

    public LingzhiMushroomBlock(Properties properties, HerbDefinition definition, Supplier<? extends Item> harvestItem) {
        super(properties);
        this.definition = definition;
        this.harvestItem = harvestItem;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AGE_TIER, 0)
                .setValue(WILD, false));
    }

    public HerbDefinition definition() {
        return definition;
    }

    public int ageTier(BlockState state) {
        return Mth.clamp(state.getValue(AGE_TIER), 0, definition.maxAgeTier());
    }

    public BlockState wildState(int ageTier) {
        return defaultBlockState()
                .setValue(AGE_TIER, Mth.clamp(ageTier, 0, definition.maxAgeTier()))
                .setValue(WILD, true);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE_TIER, WILD);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        int ageTier = ageTier(state);
        if (ageTier >= definition.maxAgeTier()) {
            return;
        }

        HerbDefinition.AgeThreshold threshold = definition.ageThreshold(ageTier);
        if (!threshold.canAdvance()) {
            return;
        }

        double environmentMultiplier = definition.growthMultiplier(level, pos, state);
        if (environmentMultiplier <= 0.0D) {
            return;
        }

        double chance = Math.min(1.0D, environmentMultiplier / threshold.averageRandomTicksToNext());
        if (random.nextDouble() < chance) {
            level.setBlock(pos, state.setValue(AGE_TIER, ageTier + 1), 2);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (direction.getOpposite() == state.getValue(FACING) && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);

        boolean wild = state.getValue(WILD);
        int ageTier = ageTier(state);
        Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
        BlockPos pos = origin == null ? BlockPos.ZERO : BlockPos.containing(origin);
        HerbDefinition.Quality quality = definition.resolveQuality(params.getLevel(), pos, state, wild);

        for (ItemStack drop : drops) {
            if (drop.getItem() == harvestItem.get()) {
                drop.set(
                        AscensionComponents.HERB_DATA.get(),
                        new AscensionComponents.HerbData(ageTier, quality.ordinal(), wild)
                );
            }
        }

        return drops;
    }

    private boolean isValidSupport(LevelReader level, BlockPos pos) {
        return definition.canWildSurviveOn(level.getBlockState(pos));
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
