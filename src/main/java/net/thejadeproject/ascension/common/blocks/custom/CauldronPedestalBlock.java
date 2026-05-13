package net.thejadeproject.ascension.common.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thejadeproject.ascension.common.blocks.entity.CauldronPedestalBlockEntity;
import net.thejadeproject.ascension.common.blocks.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * Ingredient Pedestal for the Mortal-tier Pill Cauldron multiblock.
 *
 * Layout around the cauldron (same Y level):
 *       [Pedestal North]
 *  [Pedestal West] [Cauldron] [Pedestal East]
 *
 * Interaction:
 *   Right-click with held items → places / adds to pedestal (up to 64, same item only)
 *   Right-click with empty hand → retrieves the whole stack back
 *   Items shown floating above the pedestal via BESR.
 *   Cannot insert/remove from the cauldron GUI (mirror slots are locked).
 */
public class CauldronPedestalBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 12, 13);
    public static final MapCodec<CauldronPedestalBlock> CODEC = simpleCodec(CauldronPedestalBlock::new);

    public CauldronPedestalBlock(Properties properties) {
        super(properties.sound(SoundType.STONE).strength(2.5f).requiresCorrectToolForDrops());
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronPedestalBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CauldronPedestalBlockEntity pedestal)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack current = pedestal.getItem();

        // ── Retrieve with empty hand ──────────────────────────────
        if (stack.isEmpty()) {
            if (!current.isEmpty()) {
                player.setItemInHand(hand, current.copy());
                pedestal.setItem(ItemStack.EMPTY);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                return ItemInteractionResult.sidedSuccess(false);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // ── Place or add items ────────────────────────────────────
        // Check compatibility: pedestal must be empty or hold the same item
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
            // Different item type already on pedestal – do nothing
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Determine how many to insert (whole held stack, or as many as fit)
        int toInsert = player.getAbilities().instabuild ? stack.getCount() : stack.getCount();
        int inserted = pedestal.tryInsert(stack.copyWithCount(toInsert));

        if (inserted > 0 && !player.getAbilities().instabuild) {
            stack.shrink(inserted);
        }

        if (inserted > 0) {
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            return ItemInteractionResult.sidedSuccess(false);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CauldronPedestalBlockEntity pedestal) {
                pedestal.drops();
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}