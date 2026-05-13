package net.thejadeproject.ascension.common.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thejadeproject.ascension.common.blocks.entity.TechniqueStandBlockEntity;
import net.thejadeproject.ascension.common.items.techniques.TechniqueBinderItem;
import net.thejadeproject.ascension.common.items.techniques.TechniquePageItem;
import net.thejadeproject.ascension.common.items.techniques.TechniqueTransferItem;
import org.jetbrains.annotations.Nullable;

public class TechniqueStandBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public TechniqueStandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TechniqueStandBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TechniqueStandBlockEntity stand)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (heldStack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                if (!stand.getStoredItem().isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack retrieved = stand.retrieveItem();
                        player.addItem(retrieved);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (heldStack.getItem() instanceof TechniqueTransferItem
                || heldStack.getItem() instanceof TechniqueBinderItem) {
            if (stand.getStoredItem().isEmpty()) {
                if (!level.isClientSide()) {
                    stand.setStoredItem(heldStack.copyWithCount(1));
                    if (!player.getAbilities().instabuild) {
                        heldStack.shrink(1);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (heldStack.getItem() instanceof TechniquePageItem) {
            if (!level.isClientSide()) {
                ItemStack stored = stand.getStoredItem();
                if (stored.isEmpty()) {
                    stand.setStoredItem(heldStack.copyWithCount(1));
                    if (!player.getAbilities().instabuild) {
                        heldStack.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
                if (stored.getItem() instanceof TechniqueBinderItem) {
                    boolean consumed = stand.tryInsertPage(heldStack, player);
                    if (consumed && !player.getAbilities().instabuild) {
                        heldStack.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            } else {
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!(level.getBlockEntity(pos) instanceof TechniqueStandBlockEntity stand)) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            ItemStack stored = stand.getStoredItem();
            if (!stored.isEmpty()) {
                if (!level.isClientSide()) {
                    player.addItem(stored.copy());
                    stand.setStoredItem(ItemStack.EMPTY);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return net.minecraft.world.InteractionResult.PASS;
    }
}