package net.thejadeproject.ascension.common.blocks.custom.fires;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CrimsonLotusFire extends BaseFireBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;

    private static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    private final float damage;
    private final int spreadDelay;
    private final int extinguishChance;

    public CrimsonLotusFire(Properties properties, float damage, int spreadDelay, int extinguishChance) {
        super(properties, damage);
        this.damage = damage;
        this.spreadDelay = spreadDelay;
        this.extinguishChance = extinguishChance;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false));
    }

    public CrimsonLotusFire(Properties properties) {
        this(properties, 1.0f, 30, 5);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DOWN_AABB;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Simplified and more efficient entity damage logic
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return;
            }
            // Creative players can remove fire with empty hand
            if (player.isCreative() && player.getMainHandItem().isEmpty()) {
                level.removeBlock(pos, false);
                return;
            }
        }

        if (entity.isInvulnerable()) {
            return;
        }

        if (!level.isClientSide) {
            entity.hurt(level.damageSources().inFire(), this.damage);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, getFireTickDelay(level.random));

        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }

        int age = state.getValue(AGE);
        if (age < 15) {
            state = state.setValue(AGE, age + 1);
            level.setBlock(pos, state, 3);
        }

        this.trySpread(level, pos, random, age);
    }

    private void trySpread(ServerLevel level, BlockPos pos, RandomSource random, int age) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            if (random.nextInt(spreadDelay) == 0) {
                this.checkBurn(level, adjacentPos, random, age);
            }
        }
    }

    private void checkBurn(Level level, BlockPos pos, RandomSource random, int age) {
        if (!level.getBlockState(pos).isFlammable(level, pos, Direction.UP)) {
            return;
        }

        BlockState fireState = this.defaultBlockState().setValue(AGE, Math.min(15, age + random.nextInt(3) / 2));
        if (canSurvive(fireState, level, pos)) {
            level.setBlock(pos, fireState, 3);
            level.gameEvent(null, GameEvent.BLOCK_PLACE, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        boolean north = canBurn(level.getBlockState(pos.north()));
        boolean east = canBurn(level.getBlockState(pos.east()));
        boolean south = canBurn(level.getBlockState(pos.south()));
        boolean west = canBurn(level.getBlockState(pos.west()));
        boolean up = canBurn(level.getBlockState(pos.above()));

        return state.setValue(NORTH, north)
                .setValue(EAST, east)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(UP, up);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSurviveOnBlock(level.getBlockState(pos.below()));
    }

    public static boolean canSurviveOnBlock(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.NETHERRACK ||
                block == Blocks.MAGMA_BLOCK ||
                block == Blocks.SOUL_SAND ||
                block == Blocks.SOUL_SOIL ||
                CampfireBlock.isLitCampfire(state) ||
                block instanceof BaseFireBlock;
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return simpleCodec(properties -> new CrimsonLotusFire(properties, damage, spreadDelay, extinguishChance));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        boolean north = canBurn(level.getBlockState(pos.north()));
        boolean east = canBurn(level.getBlockState(pos.east()));
        boolean south = canBurn(level.getBlockState(pos.south()));
        boolean west = canBurn(level.getBlockState(pos.west()));
        boolean up = canBurn(level.getBlockState(pos.above()));

        return this.defaultBlockState()
                .setValue(NORTH, north)
                .setValue(EAST, east)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(UP, up);
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return state.isFlammable(null, null, null);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!player.isCreative()) {
            return;
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        Player player = context.getPlayer();
        return player != null && player.isCreative();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    public float getExplosionResistance() {
        return 3600000.0F;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return player.isCreative() ? super.getDestroyProgress(state, player, level, pos) : 0.0F;
    }

    public static void onBlockCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        if (igniter instanceof Player) {
        }
    }

    private static int getFireTickDelay(RandomSource random) {
        return 30 + random.nextInt(10);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.scheduleTick(pos, this, getFireTickDelay(level.random));
    }
}