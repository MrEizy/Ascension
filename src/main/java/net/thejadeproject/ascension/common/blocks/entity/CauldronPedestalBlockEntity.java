package net.thejadeproject.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stores one ItemStack (up to 64) displayed floating above the pedestal.
 *
 * KEY FIX: saveAdditional always writes the "item" key — even when the stack is
 * empty (written as an empty CompoundTag). This ensures that when the server
 * calls setItem(EMPTY) and sends a ClientboundBlockEntityDataPacket, the client's
 * loadAdditional actually receives the empty tag and clears its cached stack.
 * Without this, loadAdditional would see no "item" key, leave the old stale
 * ItemStack in place, and the renderer would keep showing the ghost item.
 */
public class CauldronPedestalBlockEntity extends BlockEntity {

    public static final int MAX_STACK = 64;

    private ItemStack item    = ItemStack.EMPTY;
    private float    rotation = 0f;

    public CauldronPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAULDRON_PEDESTAL.get(), pos, state);
    }

    public ItemStack getItem() { return item; }

    public void setItem(ItemStack stack) {
        this.item = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public int tryInsert(ItemStack toAdd) {
        if (toAdd.isEmpty()) return 0;

        if (item.isEmpty()) {
            int take = Math.min(toAdd.getCount(), MAX_STACK);
            ItemStack placed = toAdd.copy();
            placed.setCount(take);
            setItem(placed);
            return take;
        }

        if (!ItemStack.isSameItemSameComponents(item, toAdd)) return 0;

        int space = MAX_STACK - item.getCount();
        if (space <= 0) return 0;

        int take = Math.min(toAdd.getCount(), space);
        item.grow(take);
        // Use setItem to trigger sync properly
        setItem(item);
        return take;
    }

    public void drops() {
        if (level == null || item.isEmpty()) return;
        SimpleContainer inv = new SimpleContainer(1);
        inv.setItem(0, item.copy());
        Containers.dropContents(level, worldPosition, inv);
        item = ItemStack.EMPTY;
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360f) rotation = 0f;
        return rotation;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        // Always write the item tag — even when empty.
        // An empty CompoundTag means "no item" and loadAdditional will clear the
        // client's stale stack. Skipping the write would leave the old item visible.
        if (!item.isEmpty()) {
            tag.put("item", item.save(reg));
        } else {
            tag.put("item", new CompoundTag());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        if (tag.contains("item")) {
            CompoundTag itemTag = tag.getCompound("item");
            if (itemTag.isEmpty()) {
                // Empty compound = explicitly cleared
                item = ItemStack.EMPTY;
            } else {
                item = ItemStack.parse(reg, itemTag).orElse(ItemStack.EMPTY);
            }
        }
        // If "item" key is absent entirely (e.g. fresh load before any item placed),
        // leave item as EMPTY — that's correct for a new pedestal.
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider reg) {
        return saveWithoutMetadata(reg);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}