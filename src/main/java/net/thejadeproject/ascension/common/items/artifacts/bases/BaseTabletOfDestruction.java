package net.thejadeproject.ascension.common.items.artifacts.bases;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.thejadeproject.ascension.util.ModTags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTabletOfDestruction extends Item {

    private static final String DROP_BLOCKS_TAG    = "DropBlocks";
    protected static final String LINKED_POS_TAG   = "LinkedPos";
    protected static final String LINKED_DIM_TAG   = "LinkedDimension";

    public BaseTabletOfDestruction(Properties properties) {
        super(properties);
    }

    // -------------------------------------------------------------------------
    // Abstract configuration — implemented by each tier
    // -------------------------------------------------------------------------

    protected abstract int getCooldownTicks();
    protected abstract int getWidth();
    protected abstract int getHeight();
    protected abstract int getDepth();
    protected abstract boolean supportsDropBlocks();
    protected abstract boolean supportsContainerLinking();

    // -------------------------------------------------------------------------
    // Item interaction entry point
    // -------------------------------------------------------------------------

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level    = context.getLevel();
        Player player  = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack      = context.getItemInHand();
        BlockPos  clickedPos = context.getClickedPos();

        // Shift-click → container linking (Heaven tablet only)
        if (supportsContainerLinking() && player.isShiftKeyDown()) {
            return handleContainerLinking(level, player, stack, clickedPos);
        }

        // Cooldown check — works on both sides; message only on server
        if (player.getCooldowns().isOnCooldown(this)) {
            if (!level.isClientSide) {
                player.displayClientMessage(getCooldownMessage(), true);
            }
            return InteractionResult.FAIL;
        }

        // All real work happens server-side
        if (!level.isClientSide) {
            player.getCooldowns().addCooldown(this, getCooldownTicks());

            Direction direction = player.getDirection();
            boolean   dropBlocks = supportsDropBlocks() && isDropBlocksEnabled(stack);

            BlockPos linkedPos = null;
            String   linkedDim = null;
            if (supportsContainerLinking()) {
                LinkedContainerData link = getLinkedContainer(stack);
                linkedPos = link.pos();
                linkedDim = link.dimension();
            }

            clearArea((ServerLevel) level, clickedPos, direction,
                    player.position(), dropBlocks, linkedPos, linkedDim);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            level.playSound(null, clickedPos,
                    SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Container linking (Heaven tablet)
    // -------------------------------------------------------------------------

    private InteractionResult handleContainerLinking(Level level, Player player,
                                                     ItemStack stack, BlockPos pos) {
        // Return SUCCESS on client so the animation plays; actual work is server-only
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockState state = level.getBlockState(pos);

        if (!state.is(ModTags.Blocks.LINKABLE_CONTAINERS)) {
            player.displayClientMessage(
                    Component.translatable("item.ascension.tablet.link_invalid"), true);
            return InteractionResult.FAIL;
        }

        IItemHandler handler =
                level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler == null) {
            player.displayClientMessage(
                    Component.translatable("item.ascension.tablet.link_invalid"), true);
            return InteractionResult.FAIL;
        }

        LinkedContainerData current = getLinkedContainer(stack);
        String currentDim = level.dimension().location().toString();

        if (current.pos() != null
                && current.pos().equals(pos)
                && currentDim.equals(current.dimension())) {
            clearLinkedContainer(stack);
            player.displayClientMessage(
                    Component.translatable("item.ascension.tablet.unlink_success"), true);
        } else {
            setLinkedContainer(stack, pos, currentDim);
            player.displayClientMessage(
                    Component.translatable("item.ascension.tablet.link_success",
                            state.getBlock().getName().getString(),
                            pos.getX(), pos.getY(), pos.getZ()),
                    true);
        }

        return InteractionResult.SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Area clearing
    // -------------------------------------------------------------------------

    private void clearArea(ServerLevel level, BlockPos startPos, Direction direction,
                           Vec3 playerPos, boolean dropBlocks,
                           @Nullable BlockPos linkedContainerPos,
                           @Nullable String linkedDimension) {
        int width  = getWidth();
        int height = getHeight();
        int depth  = getDepth();
        int dx     = direction.getStepX();
        int dz     = direction.getStepZ();

        if (!isAreaInWorldBounds(level, startPos, width, height, depth, dx, dz)) {
            return;
        }

        List<BlockStatePos> blocksToDrop = new ArrayList<>();

        /*
         * Iterate TOP-DOWN within each column so that gravity-affected blocks
         * (sand, gravel, concrete powder) are captured before they can fall
         * into positions that have already been cleared. Without this, gravel
         * above an air gap created by an earlier iteration step would fall and
         * either be missed for collection or land in the wrong place.
         */
        for (int x = -width; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos columnBase = startPos.offset(
                        dx * z + dz * x, 0,
                        dz * z + dx * x);

                for (int y = height; y >= -1; y--) {   // <-- top-down
                    BlockPos targetPos = columnBase.above(y);

                    if (!shouldRemoveBlock(level, targetPos)) continue;

                    BlockState state = level.getBlockState(targetPos);

                    if (dropBlocks) {
                        blocksToDrop.add(new BlockStatePos(targetPos, state));
                    }

                    level.removeBlock(targetPos, false);
                }
            }
        }

        placeTorches(level, startPos, direction, depth);

        if (dropBlocks && !blocksToDrop.isEmpty()) {
            handleBlockDrops(level, blocksToDrop, playerPos, direction,
                    linkedContainerPos, linkedDimension);
        }
    }

    // -------------------------------------------------------------------------
    // Bounds check
    // -------------------------------------------------------------------------

    private boolean isAreaInWorldBounds(ServerLevel level, BlockPos startPos,
                                        int width, int height, int depth,
                                        int dx, int dz) {
        for (int x = -width; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                for (int y = -1; y <= height; y++) {
                    BlockPos p = startPos.offset(
                            dx * z + dz * x, y,
                            dz * z + dx * x);
                    if (!level.isInWorldBounds(p)) return false;
                }
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Torch placement
    // -------------------------------------------------------------------------

    private void placeTorches(ServerLevel level, BlockPos startPos,
                              Direction direction, int depth) {
        int dx = direction.getStepX();
        int dz = direction.getStepZ();

        for (int z = 0; z <= depth; z += 6) {
            BlockPos torchPos = startPos.offset(dx * z, -1, dz * z);
            BlockPos floorPos = torchPos.below();

            /*
             * Both checks are required:
             *  1. torchPos must be empty — a block that resisted removal
             *     (e.g. in DESTRUCTIBLE_BLOCKS tag exclusion) would otherwise
             *     be overwritten silently.
             *  2. floorPos must have a solid upward face to support the torch.
             */
            if (level.getBlockState(torchPos).isAir()
                    && level.getBlockState(floorPos)
                    .isFaceSturdy(level, floorPos, Direction.UP)) {
                level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Block drop handling
    // -------------------------------------------------------------------------

    private void handleBlockDrops(ServerLevel level, List<BlockStatePos> blocks,
                                  Vec3 playerPos, Direction direction,
                                  @Nullable BlockPos linkedContainerPos,
                                  @Nullable String linkedDimension) {
        // Drop items one block in front of the player so they don't fall into
        // the freshly cleared tunnel immediately.
        Vec3 dropPos = playerPos.add(direction.getStepX(), 0, direction.getStepZ());

        IItemHandler linkedHandler = null;
        if (linkedContainerPos != null && linkedDimension != null
                && linkedDimension.equals(level.dimension().location().toString())) {
            linkedHandler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, linkedContainerPos, null);
        }

        for (BlockStatePos blockData : blocks) {
            // Block entity must be fetched BEFORE removeBlock was called.
            // We stored the state at removal time; block entity is already gone,
            // so getBlockEntity returns null here — that's correct, vanilla
            // getDrops handles a null block entity gracefully.
            List<ItemStack> drops = Block.getDrops(
                    blockData.state, level, blockData.pos,
                    level.getBlockEntity(blockData.pos), null, ItemStack.EMPTY);

            for (ItemStack drop : drops) {
                if (linkedHandler != null) {
                    drop = insertIntoHandler(linkedHandler, drop);
                }

                if (!drop.isEmpty()) {
                    level.addFreshEntity(new ItemEntity(
                            level, dropPos.x, dropPos.y, dropPos.z, drop));
                }
            }
        }
    }

    /**
     * Attempts to insert {@code stack} into {@code handler}, returning whatever
     * could not be placed (may be empty if everything fit).
     *
     * <p>We work on a copy so the caller's reference is not mutated; the return
     * value is what matters.
     */
    private ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack) {
        if (stack.isEmpty()) return stack;

        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    // -------------------------------------------------------------------------
    // Linked-container data  (stored in CUSTOM_DATA on the ItemStack)
    // -------------------------------------------------------------------------

    protected record LinkedContainerData(@Nullable BlockPos pos,
                                         @Nullable String dimension) {}

    protected LinkedContainerData getLinkedContainer(ItemStack stack) {
        var customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        // getUnsafe() is intentional here: we only read, never write through
        // this reference, so we avoid the cost of a full tag copy.
        var tag = customData.getUnsafe();

        if (tag.contains(LINKED_POS_TAG)) {
            return new LinkedContainerData(
                    BlockPos.of(tag.getLong(LINKED_POS_TAG)),
                    tag.getString(LINKED_DIM_TAG));
        }
        return new LinkedContainerData(null, null);
    }

    protected void setLinkedContainer(ItemStack stack, BlockPos pos, String dimension) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();
        tag.putLong(LINKED_POS_TAG, pos.asLong());
        tag.putString(LINKED_DIM_TAG, dimension);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    protected void clearLinkedContainer(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();
        tag.remove(LINKED_POS_TAG);
        tag.remove(LINKED_DIM_TAG);
        // If the tag is now empty, remove the component entirely to keep stacks clean
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    // -------------------------------------------------------------------------
    // Drop-blocks toggle
    // -------------------------------------------------------------------------

    protected boolean isDropBlocksEnabled(ItemStack stack) {
        if (!supportsDropBlocks()) return false;
        // getUnsafe() — read-only, see note in getLinkedContainer
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .getUnsafe()
                .getBoolean(DROP_BLOCKS_TAG);
    }

    /**
     * Toggle drop-blocks mode.  Must be called server-side (e.g. from a key
     * binding packet handler).
     */
    public static void toggleDropModeServer(ItemStack stack, ServerPlayer player) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();
        boolean next = !tag.getBoolean(DROP_BLOCKS_TAG);
        tag.putBoolean(DROP_BLOCKS_TAG, next);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        Component statusText = Component.literal(String.valueOf(next))
                .withStyle(next ? ChatFormatting.GREEN : ChatFormatting.RED);
        player.displayClientMessage(
                Component.translatable("ascension.tablet.drop_blocks").append(statusText),
                true);
    }

    // -------------------------------------------------------------------------
    // Overrideable helpers
    // -------------------------------------------------------------------------

    protected Component getCooldownMessage() {
        return Component.translatable("ascension.tablet.cooldown");
    }

    protected boolean shouldRemoveBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return supportsDropBlocks() && isDropBlocksEnabled(stack);
    }

    // -------------------------------------------------------------------------
    // Internal value type
    // -------------------------------------------------------------------------

    protected record BlockStatePos(BlockPos pos, BlockState state) {}
}