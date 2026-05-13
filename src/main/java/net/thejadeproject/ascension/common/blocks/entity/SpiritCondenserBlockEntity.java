package net.thejadeproject.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thejadeproject.ascension.common.blocks.custom.SpiritCondenserBlock;

import net.thejadeproject.ascension.data_attachments.ModAttachments;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Spirit Condenser.
 *
 * Flow each tick:
 *  1. SpiritCondenserBlock.entityInside() calls onPlayerStanding(player)
 *     if a player overlaps and is on the top surface.
 *  2. tick() reads pendingPlayer, activates, drains Qi once per second,
 *     then clears pendingPlayer for the next tick.
 *  3. If player steps off, no call arrives → pendingPlayer = null → active = false.
 */
public class SpiritCondenserBlockEntity extends BlockEntity {

    /** Purity bonus the cauldron gains while this condenser is active. */
    public static final int PURITY_BONUS = 10;

    private boolean active     = false;
    private int     drainTimer = 0;
    private static final int DRAIN_INTERVAL = 20; // 1 second = 20 ticks

    @Nullable
    private transient Player pendingPlayer = null;

    public SpiritCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPIRIT_CONDENSER.get(), pos, state);
    }

    /** Called by the block each tick a qualifying player is on top. */
    public void onPlayerStanding(Player player) {
        //todo
    }

    public boolean isActive() { return active; }

    public void tick(Level level, BlockPos pos, BlockState state) {
        boolean wasActive = active;

        if (pendingPlayer != null) {
            active = true;
            drainTimer++;
            if (drainTimer >= DRAIN_INTERVAL) {
                drainTimer = 0;
                //TODO drain qi
            }
        } else {
            // Player stepped off
            active     = false;
            drainTimer = 0;
        }

        if (active != wasActive) {
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        // Clear; entityInside() will repopulate next tick if player still present
        pendingPlayer = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putBoolean("active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        active = tag.getBoolean("active");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider reg) { return saveWithoutMetadata(reg); }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}