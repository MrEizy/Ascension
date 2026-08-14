package net.zic.ascension.common.blocks.crops.herbs;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
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

/**
 * Same idea as vanilla's CocoaBlock: a HorizontalDirectionalBlock whose support can be any
 * horizontal side, with the model rotated per FACING so its built-in "north" face always
 * points at the support — same shape-rotation approach as LingzhiMushroomBlock. Pass whatever
 * {@link HerbDefinition} governs its growth/valid supports and whatever item it should hand
 * over when harvested.
 */
public class PodHerbBlock extends HorizontalDirectionalBlock {

    public static final int MAX_STAGE = HerbDefinition.MAX_GROWTH_STAGES + HerbDefinition.MAX_AGE_TIERS - 2;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);
    public static final BooleanProperty WILD = BooleanProperty.create("wild");

    private static final VoxelShape[] NORTH_SHAPE_BY_GROWTH_STAGE = new VoxelShape[]{
            Shapes.box(0.4375, 0.39, 0.0, 0.5625, 0.65, 0.125),
            Shapes.box(0.375, 0.25, 0.0, 0.625, 0.6719, 0.25),
            Shapes.box(0.4106, 0.3563, 0.0, 0.5875, 0.6766, 0.19),
            Shapes.box(0.4106, 0.3438, 0.0, 0.6, 0.6828, 0.2031)
    };

    private static final Map<Direction, VoxelShape[]> SHAPES_BY_FACING = buildShapes();

    private final HerbDefinition definition;
    private final Supplier<? extends Item> harvestItem;

    public PodHerbBlock(Properties properties, HerbDefinition definition, Supplier<? extends Item> harvestItem) {
        super(properties);
        this.definition = definition;
        this.harvestItem = harvestItem;

        if (definition.growthStages() > NORTH_SHAPE_BY_GROWTH_STAGE.length) {
            throw new IllegalArgumentException(
                    "Pod herb " + definition.id() + " has " + definition.growthStages()
                            + " growth stages but PodHerbBlock only has "
                            + NORTH_SHAPE_BY_GROWTH_STAGE.length + " voxel shapes"
            );
        }

        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STAGE, 0)
                .setValue(WILD, false));
    }

    public HerbDefinition definition() {
        return definition;
    }

    public Item harvestItem() {
        return harvestItem.get();
    }

    public int growthStage(BlockState state) {
        return Math.min(state.getValue(STAGE), definition.maxGrowthStage());
    }

    public int ageTier(BlockState state) {
        return Mth.clamp(state.getValue(STAGE) - definition.maxGrowthStage(), 0, definition.maxAgeTier());
    }

    public boolean isMature(BlockState state) {
        return state.getValue(STAGE) >= definition.maxGrowthStage();
    }

    public int maxStage() {
        return definition.maxGrowthStage() + definition.maxAgeTier();
    }

    public BlockState wildState(Direction supportDirection, RandomSource random) {
        int ageTier = definition.chooseWildAgeTier(random);
        return defaultBlockState()
                .setValue(FACING, supportDirection)
                .setValue(STAGE, definition.maxGrowthStage() + ageTier)
                .setValue(WILD, true);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE, WILD);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int visualStage = Math.min(growthStage(state), NORTH_SHAPE_BY_GROWTH_STAGE.length - 1);
        return SHAPES_BY_FACING.get(state.getValue(FACING))[visualStage];
    }

    // --- Placement ---

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        Direction clicked = context.getClickedFace();
        if (clicked.getAxis().isHorizontal()) {
            Direction supportDirection = clicked.getOpposite();
            if (isValidSupport(level, pos.relative(supportDirection))) {
                return defaultBlockState()
                        .setValue(FACING, supportDirection)
                        .setValue(STAGE, 0)
                        .setValue(WILD, false);
            }
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (isValidSupport(level, pos.relative(direction))) {
                return defaultBlockState()
                        .setValue(FACING, direction)
                        .setValue(STAGE, 0)
                        .setValue(WILD, false);
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidSupport(level, pos.relative(state.getValue(FACING)));
    }

    private boolean isValidSupport(LevelReader level, BlockPos pos) {
        return definition.canWildSurviveOn(level.getBlockState(pos));
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (direction == state.getValue(FACING) && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    // --- Visual growth, then herb aging ---

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(STAGE) < maxStage();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        double environmentMultiplier = definition.growthMultiplier(level, pos, state);
        if (environmentMultiplier <= 0.0D) {
            return;
        }

        int stage = state.getValue(STAGE);

        if (stage < definition.maxGrowthStage()) {
            double chance = Math.min(1.0D, definition.baseGrowthChance() * environmentMultiplier);
            if (random.nextDouble() < chance) {
                level.setBlock(pos, state.setValue(STAGE, stage + 1), 2);
            }
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

        double chance = Math.min(1.0D, environmentMultiplier / threshold.averageRandomTicksToNext());
        if (random.nextDouble() < chance) {
            level.setBlock(pos, state.setValue(STAGE, stage + 1), 2);
        }
    }

    // --- Right-click harvest then regrow from visual stage 0 ---

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!isMature(state)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return harvest(state, level, pos, player);
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!isMature(state)) {
            return InteractionResult.PASS;
        }
        return harvest(state, level, pos, player);
    }

    private InteractionResult harvest(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            for (ItemStack drop : Block.getDrops(state, serverLevel, pos, null, player, ItemStack.EMPTY)) {
                popResource(level, pos, drop);
            }

            level.setBlock(pos, state.setValue(STAGE, 0), 2);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + level.getRandom().nextFloat() * 0.4F
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!isMature(state)) {
            return drops;
        }

        boolean wild = state.getValue(WILD);
        int ageTier = ageTier(state);
        Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
        BlockPos pos = origin == null ? BlockPos.ZERO : BlockPos.containing(origin);
        HerbDefinition.Quality quality = definition.resolveQuality(params.getLevel(), pos, state, wild);

        AscensionComponents.HerbData herbData = new AscensionComponents.HerbData(
                ageTier,
                quality.ordinal(),
                wild
        );

        for (ItemStack drop : drops) {
            if (drop.getItem() == harvestItem.get()) {
                drop.set(AscensionComponents.HERB_DATA.get(), herbData);
            }
        }

        return drops;
    }

    // --- Shape rotation ---

    private static Map<Direction, VoxelShape[]> buildShapes() {
        Map<Direction, VoxelShape[]> map = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape[] shapes = new VoxelShape[NORTH_SHAPE_BY_GROWTH_STAGE.length];
            for (int stage = 0; stage < NORTH_SHAPE_BY_GROWTH_STAGE.length; stage++) {
                shapes[stage] = rotateShape(Direction.NORTH, direction, NORTH_SHAPE_BY_GROWTH_STAGE[stage]);
            }
            map.put(direction, shapes);
        }
        return map;
    }

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        VoxelShape current = shape;
        for (int i = 0; i < times; i++) {
            VoxelShape[] rotated = {Shapes.empty()};
            current.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    rotated[0] = Shapes.or(
                            rotated[0],
                            Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)
                    ));
            current = rotated[0];
        }
        return current;
    }
}
