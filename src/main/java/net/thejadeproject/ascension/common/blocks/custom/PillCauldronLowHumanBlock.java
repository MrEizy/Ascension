package net.thejadeproject.ascension.common.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thejadeproject.ascension.common.blocks.entity.ModBlockEntities;
import net.thejadeproject.ascension.common.blocks.entity.PillCauldronLowHumanEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Pill Cauldron block (Mortal Tier).
 *
 * Stores a FACING direction (the direction the front of the cauldron faces).
 * All multiblock positions (pedestals, flame stand) are resolved at runtime
 * relative to this direction by {@link PillCauldronLowHumanEntity#relativeOffset}.
 *
 * When placed, END_ROD particles appear at the expected positions of each
 * missing multiblock piece so the player knows where to put them.
 *
 * Layout relative to FACING (e.g. if placed facing SOUTH):
 *
 *       [Back Pedestal]          ← 2 blocks in the FACING direction
 *  [Left Pedestal] [Cauldron] [Right Pedestal]
 *       [Flame Stand]            ← directly below
 */
public class PillCauldronLowHumanBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 11, 14);
    public static final MapCodec<PillCauldronLowHumanBlock> CODEC = simpleCodec(PillCauldronLowHumanBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public PillCauldronLowHumanBlock(Properties properties) {
        super(properties.sound(SoundType.ANVIL).strength(3.5f, 4.5f).requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // FACING = the direction the player is looking when they place the block.
        // This means the cauldron's "front" (and the back pedestal) points AWAY from the player,
        // which matches relativeOffset(0, 0, +2) going away from the placer.
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PillCauldronLowHumanEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    // ── Ghost particle hints when placed ─────────────────────────

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            spawnMultiblockGhostParticles(serverLevel, pos, state.getValue(FACING));
        }
    }

    /**
     * Computes expected multiblock positions using the same relative-offset logic
     * as the entity, then spawns END_ROD particles there to guide placement.
     */
    private void spawnMultiblockGhostParticles(ServerLevel level, BlockPos origin, Direction facing) {
        Direction right = facing.getClockWise();

        // Flame Stand – directly below
        spawnOutlineAt(level, origin.below());

        // Back pedestal – 2 blocks in the facing direction
        spawnOutlineAt(level, origin.relative(facing, 2));

        // Left pedestal – 2 blocks counter-clockwise from facing
        spawnOutlineAt(level, origin.relative(right.getOpposite(), 2));

        // Right pedestal – 2 blocks clockwise from facing
        spawnOutlineAt(level, origin.relative(right, 2));
    }

    private void spawnOutlineAt(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            double ox = (Math.random() - 0.5) * 0.8;
            double oy = Math.random() * 1.0;
            double oz = (Math.random() - 0.5) * 0.8;
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5 + ox, pos.getY() + oy, pos.getZ() + 0.5 + oz,
                    1, 0, 0, 0, 0.01);
        }
    }

    // ── Interaction ───────────────────────────────────────────────

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PillCauldronLowHumanEntity cauldron) {
                ((ServerPlayer) player).openMenu(
                        new SimpleMenuProvider(cauldron, Component.literal("Pill Cauldron")), pos);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PillCauldronLowHumanEntity cauldron) {
                cauldron.drops();
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.PILL_CAULDRON_LOW_HUMAN.get(),
                (l, p, bs, be) -> be.tick(l, p, bs));
    }
}