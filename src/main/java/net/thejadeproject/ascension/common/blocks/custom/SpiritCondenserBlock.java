package net.thejadeproject.ascension.common.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thejadeproject.ascension.common.blocks.entity.ModBlockEntities;
import net.thejadeproject.ascension.common.blocks.entity.SpiritCondenserBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The Spirit Condenser.
 *
 * Place anywhere within 3 blocks of the Pill Cauldron.
 * When a player stands on it, Qi is slowly drained and the cauldron
 * receives a purity bonus (+@link SpiritCondenserBlockEntity#PURITY_BONUS) during crafting.
 *
 * Uses {@code entityInside()} which fires every tick a colliding entity overlaps
 * the block — more reliable than stepOn for continuous standing detection.
 */
public class SpiritCondenserBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 2, 14);
    public static final MapCodec<SpiritCondenserBlock> CODEC = simpleCodec(SpiritCondenserBlock::new);

    /** Qi drained per second while player stands on the condenser. */
    public static final double QI_DRAIN_PER_SECOND = 5.0;

    public SpiritCondenserBlock(Properties properties) {
        super(properties.sound(SoundType.AMETHYST).strength(3.0f).requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritCondenserBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Fires every tick the player's AABB overlaps this block.
     * Signals the block entity that a player is present this tick.
     * The entity resets 'active' to false each tick, so if no call
     * arrives the condenser automatically deactivates.
     */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;

        // Only count as "standing on top" — not clipping through the side
        double topY = pos.getY() + SHAPE.max(net.minecraft.core.Direction.Axis.Y);
        if (player.getY() < topY - 0.35) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SpiritCondenserBlockEntity condenser) {
            condenser.onPlayerStanding(player);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.SPIRIT_CONDENSER.get(),
                (l, pos, bs, be) -> be.tick(l, pos, bs));
    }
}