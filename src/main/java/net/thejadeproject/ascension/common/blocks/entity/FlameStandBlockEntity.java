package net.thejadeproject.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thejadeproject.ascension.common.blocks.custom.FlameStandBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Flame Stand.
 *
 * ── Temperature model ─────────────────────────────────────────────────────
 * Temperature (0–MAX_TEMP) rises when fanned and decays naturally each tick.
 * The decay rate increases the closer temperature is to zero (flames die quickly
 * when cold) and decreases at high heat (hot embers hold longer).
 *
 *   Decay per tick = BASE_DECAY + (MAX_TEMP - temp) * DECAY_SCALE
 *
 * When temperature reaches 0 the flame extinguishes.
 *
 * ── Fluctuation ───────────────────────────────────────────────────────────
 * A small noise value (±FLICKER_RANGE) is recalculated every FLICKER_INTERVAL
 * ticks and smoothly interpolated toward. This gives the HUD bar a living,
 * breathing appearance without affecting actual crafting logic.
 *
 * ── Purity / Realm bonuses ────────────────────────────────────────────────
 * Set by the fire item used to light the stand (from config).
 * Used by the cauldron entity when computing output quality.
 */
public class FlameStandBlockEntity extends BlockEntity {

    // ── Temperature constants ────────────────────────────────────
    public static final int   MAX_TEMP       = 1000;
    public static final int   LIGHT_TEMP     = 200;   // temp set when first lit
    /** Base decay per tick at full temp. Lower = stays hot longer. */
    private static final float BASE_DECAY    = 0.4f;
    /** Extra decay added proportionally as temp falls (colder = faster decay). */
    private static final float DECAY_SCALE   = 0.0008f;

    // ── Warning thresholds ───────────────────────────────────────
    /** Fan warning shown when temp drops below 30% of max. */
    public static final int WARN_THRESH     = (int)(MAX_TEMP * 0.30f);
    /** Critical warning when temp drops below 10% of max. */
    public static final int CRIT_THRESH     = (int)(MAX_TEMP * 0.10f);

    // ── Flicker constants ────────────────────────────────────────
    private static final int   FLICKER_INTERVAL = 12; // ticks between flicker updates
    private static final int   FLICKER_RANGE    = 25; // ± max flicker amount

    // ── State ────────────────────────────────────────────────────
    private boolean   isLit        = false;
    private float     temperature  = 0f;
    private int       purityBonus  = 0;
    private int       realmBonus   = 0;
    /** The flame item currently burning in the stand (shown by renderer). */
    private ItemStack litFlameItem = ItemStack.EMPTY;

    // Flicker state (used client-side for smooth HUD)
    private float   flickerTarget   = 0f;
    private float   flickerCurrent  = 0f;
    private int     flickerTimer    = 0;

    public FlameStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLAME_STAND.get(), pos, state);
    }

    // ── Public API ───────────────────────────────────────────────

    /** Lights the stand with the given flame item, starting temp and bonuses. */
    public void light(ItemStack flameItem, int purityBonus, int realmBonus) {
        this.isLit        = true;
        this.temperature  = LIGHT_TEMP;
        this.purityBonus  = purityBonus;
        this.realmBonus   = realmBonus;
        this.litFlameItem = flameItem.copyWithCount(1);
        sync();
    }

    /** Extinguishes the flame, resetting all state. */
    public void extinguish() {
        this.isLit        = false;
        this.temperature  = 0f;
        this.purityBonus  = 0;
        this.realmBonus   = 0;
        this.litFlameItem = ItemStack.EMPTY;
        sync();
    }

    public void onRemoved() { isLit = false; litFlameItem = ItemStack.EMPTY; }

    /** The flame item currently burning — shown by the BESR above the stand. */
    public ItemStack getLitFlameItem() { return litFlameItem; }

    private float rotation = 0f;
    /** Client-side spin for the flame item renderer. */
    public float getRenderingRotation() {
        rotation += 0.4f;
        if (rotation >= 360f) rotation = 0f;
        return rotation;
    }

    /**
     * Raises temperature by {@code amount}.
     * Called by FanItem (and the old empty-hand fan for backwards compat).
     */
    public void fan(int amount) {
        if (!isLit) return;
        temperature = Math.min(MAX_TEMP, temperature + amount);
        sync();
    }

    // ── Getters ──────────────────────────────────────────────────
    public boolean isLit()        { return isLit; }
    public int getPurityBonus()   { return isLit ? purityBonus : 0; }
    public int getRealmBonus()    { return isLit ? realmBonus  : 0; }
    /** Returns the current raw temperature (0–MAX_TEMP). */
    public int getTemperature()   { return (int) temperature; }

    /** Temperature as 0.0–1.0 fraction, with smooth flicker added. */
    public float getDisplayTemp() {
        if (!isLit) return 0f;
        float raw = temperature / MAX_TEMP;
        float flicker = flickerCurrent / MAX_TEMP;
        return Math.max(0f, Math.min(1f, raw + flicker));
    }

    public boolean needsFanning() { return isLit && temperature <= WARN_THRESH; }
    public boolean isCritical()   { return isLit && temperature <= CRIT_THRESH; }

    /**
     * Returns how well the current temperature sits within [minTemp, maxTemp].
     * 1.0 = perfectly centered, 0.0 = at the edge or outside.
     * Used by the cauldron to compute purity based on last-second temperature.
     */
    public float getTempPrecision(int minTemp, int maxTemp) {
        if (!isLit || minTemp >= maxTemp) return 0f;
        int temp = getTemperature();
        if (temp < minTemp || temp > maxTemp) return 0f;
        int midpoint = (minTemp + maxTemp) / 2;
        int halfRange = (maxTemp - minTemp) / 2;
        return halfRange == 0 ? 1f : 1f - (Math.abs(temp - midpoint) / (float) halfRange);
    }

    // ── Tick ─────────────────────────────────────────────────────
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!isLit) return;

        // ── Temperature decay ────────────────────────────────────
        float decay = BASE_DECAY + (MAX_TEMP - temperature) * DECAY_SCALE;
        temperature = Math.max(0f, temperature - decay);

        if (temperature <= 0f) {
            extinguish();
            level.setBlock(pos, state.setValue(FlameStandBlock.LIT, false), 3);
            return;
        }

        // ── Flicker update ───────────────────────────────────────
        flickerTimer++;
        if (flickerTimer >= FLICKER_INTERVAL) {
            flickerTimer = 0;
            flickerTarget = (float)(Math.random() * 2 - 1) * FLICKER_RANGE;
        }
        // Smooth interpolation toward flicker target
        flickerCurrent += (flickerTarget - flickerCurrent) * 0.25f;

        // ── Periodic sync ────────────────────────────────────────
        if ((int) temperature % 10 == 0 || needsFanning()) {
            sync();
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // ── NBT ──────────────────────────────────────────────────────
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putBoolean("isLit", isLit);
        tag.putFloat("temperature", temperature);
        tag.putInt("purityBonus", purityBonus);
        tag.putInt("realmBonus", realmBonus);
        if (!litFlameItem.isEmpty()) {
            tag.put("litFlameItem", litFlameItem.save(reg));
        } else {
            tag.put("litFlameItem", new CompoundTag());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        isLit       = tag.getBoolean("isLit");
        temperature = tag.getFloat("temperature");
        purityBonus = tag.getInt("purityBonus");
        realmBonus  = tag.getInt("realmBonus");
        if (tag.contains("litFlameItem")) {
            CompoundTag itemTag = tag.getCompound("litFlameItem");
            litFlameItem = itemTag.isEmpty()
                    ? ItemStack.EMPTY
                    : ItemStack.parse(reg, itemTag).orElse(ItemStack.EMPTY);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider reg) { return saveWithoutMetadata(reg); }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}