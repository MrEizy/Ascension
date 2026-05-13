package net.thejadeproject.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.thejadeproject.ascension.common.items.herbs.HerbBonusEffects;
import net.thejadeproject.ascension.menus.custom.pill_cauldron.PillCauldronLowHumanMenu;
import net.thejadeproject.ascension.recipe.LowHumanPillCauldronRecipe;
import net.thejadeproject.ascension.recipe.ModRecipes;
import net.thejadeproject.ascension.recipe.PillCauldronInput;
import net.thejadeproject.ascension.refactor_packages.util.PillEffectUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Block Entity for the Mortal-tier Pill Cauldron.
 *
 * ── Crafting model ───────────────────────────────────────────────────────
 * Progress advances by 1 tick only while the Flame Stand's temperature is
 * inside [recipe.minTemp, recipe.maxTemp].
 * Progress resets if the flame goes out OR temp leaves the range.
 * maxProgress = recipe.recipeTime * 20 ticks.
 *
 * The temperature at the LAST tick of crafting determines purity via
 * FlameStand.getTempPrecision(minTemp, maxTemp) → recipe.calcPurity().
 *
 * ── ContainerData layout ─────────────────────────────────────────────────
 *  0  progress (ticks)
 *  1  maxProgress (ticks)
 *  2  flame lit (0/1)
 *  3  current temperature (0–1000)
 *  4  recipe minTemp (0 when no active recipe)
 *  5  recipe maxTemp (0 when no active recipe)
 */
public class PillCauldronLowHumanEntity extends BlockEntity implements MenuProvider {

    private static final int MIRROR_LEFT    = 0;
    private static final int MIRROR_BACK    = 1;
    private static final int MIRROR_RIGHT   = 2;
    private static final int OUTPUT_SUCCESS = 3;
    private static final int OUTPUT_FAIL    = 4;

    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private int progress    = 0;
    private int maxProgress = 0;

    public final ContainerData data;

    public PillCauldronLowHumanEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PILL_CAULDRON_LOW_HUMAN.get(), pos, state);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                FlameStandBlockEntity fs = getFlameStand();
                Optional<RecipeHolder<LowHumanPillCauldronRecipe>> recipe = getCurrentRecipe();
                return switch (i) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> (fs != null && fs.isLit()) ? 1 : 0;
                    case 3 -> (fs != null) ? fs.getTemperature() : 0;
                    case 4 -> recipe.map(r -> r.value().getMinTemp()).orElse(0);
                    case 5 -> recipe.map(r -> r.value().getMaxTemp()).orElse(0);
                    case 6 -> recipe.map(r -> r.value().getIdealTemp()).orElse(0);
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {
                if (i == 0) progress = v;
                if (i == 1) maxProgress = v;
            }
            @Override public int getCount() { return 7; }
        };
    }

    // ── Facing / relative position helpers ───────────────────────

    public Direction getFacing() {
        if (level == null) return Direction.NORTH;
        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.getValue(HorizontalDirectionalBlock.FACING);
        }
        return Direction.NORTH;
    }

    public BlockPos relativeOffset(int right, int up, int forward) {
        Direction facing   = getFacing();
        Direction rightDir = facing.getClockWise();
        return worldPosition
                .relative(rightDir, right)
                .relative(Direction.UP, up)
                .relative(facing, forward);
    }

    public BlockPos pedestalLeftPos()  { return relativeOffset(-2, 0, 0); }
    public BlockPos pedestalBackPos()  { return relativeOffset(0, 0, 2); }
    public BlockPos pedestalRightPos() { return relativeOffset(2, 0, 0); }

    // ── Multiblock helpers ────────────────────────────────────────

    @Nullable
    public FlameStandBlockEntity getFlameStand() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        return (be instanceof FlameStandBlockEntity fs) ? fs : null;
    }

    @Nullable
    private SpiritCondenserBlockEntity getActiveSpiritCondenser() {
        if (level == null) return null;
        for (int dx = -3; dx <= 3; dx++) for (int dy = -3; dy <= 3; dy++) for (int dz = -3; dz <= 3; dz++) {
            if (dx == 0 && dy == 0 && dz == 0) continue;
            BlockEntity be = level.getBlockEntity(worldPosition.offset(dx, dy, dz));
            if (be instanceof SpiritCondenserBlockEntity sc && sc.isActive()) return sc;
        }
        return null;
    }

    @Nullable
    private CauldronPedestalBlockEntity getPedestalEntity(BlockPos pos) {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(pos);
        return (be instanceof CauldronPedestalBlockEntity p) ? p : null;
    }

    private ItemStack getPedestalItem(BlockPos pos) {
        CauldronPedestalBlockEntity p = getPedestalEntity(pos);
        return (p != null) ? p.getItem() : ItemStack.EMPTY;
    }

    // ── Mirror sync ───────────────────────────────────────────────

    private void syncPedestalMirrors() {
        updateMirrorIfChanged(MIRROR_LEFT,  getPedestalItem(pedestalLeftPos()));
        updateMirrorIfChanged(MIRROR_BACK,  getPedestalItem(pedestalBackPos()));
        updateMirrorIfChanged(MIRROR_RIGHT, getPedestalItem(pedestalRightPos()));
    }

    private void updateMirrorIfChanged(int slot, ItemStack incoming) {
        ItemStack current = itemHandler.getStackInSlot(slot);
        boolean same = ItemStack.isSameItemSameComponents(current, incoming)
                && current.getCount() == incoming.getCount();
        if (!same) {
            itemHandler.setStackInSlot(slot, incoming.isEmpty() ? ItemStack.EMPTY : incoming.copy());
        }
    }

    // ── Consume ───────────────────────────────────────────────────

    private void consumePedestalIngredients(NonNullList<SizedIngredient> ingredients) {
        BlockPos[] positions = { pedestalLeftPos(), pedestalBackPos(), pedestalRightPos() };
        for (int i = 0; i < ingredients.size() && i < 3; i++) {
            CauldronPedestalBlockEntity pedestal = getPedestalEntity(positions[i]);
            if (pedestal == null) continue;
            int needed = ingredients.get(i).count();
            ItemStack held = pedestal.getItem();
            if (held.isEmpty()) continue;
            if (held.getCount() <= needed) {
                pedestal.setItem(ItemStack.EMPTY);
            } else {
                ItemStack remaining = held.copy();
                remaining.shrink(needed);
                pedestal.setItem(remaining);
            }
        }
    }

    // ── MenuProvider ──────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ascension.pill_cauldron");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new PillCauldronLowHumanMenu(id, inv, this, this.data);
    }

    // ── Tick ──────────────────────────────────────────────────────

    public void tick(Level level, BlockPos pos, BlockState state) {
        syncPedestalMirrors();

        FlameStandBlockEntity flameStand = getFlameStand();
        boolean flameLit = flameStand != null && flameStand.isLit();

        if (!flameLit) {
            resetProgress();
            return;
        }

        Optional<RecipeHolder<LowHumanPillCauldronRecipe>> recipeOpt = getMatchingRecipe(flameStand);

        if (recipeOpt.isPresent()) {
            LowHumanPillCauldronRecipe recipe = recipeOpt.get().value();

            // Set maxProgress from recipe on first tick of this recipe
            if (maxProgress != recipe.getRecipeTimeTicks()) {
                maxProgress = recipe.getRecipeTimeTicks();
                progress = 0;
            }

            if (canOutput(recipe)) {
                progress++;
                setChanged(level, pos, state);

                if (progress >= maxProgress) {
                    craftItem(flameStand, recipe);
                    resetProgress();
                }
            }
            // If can't output, pause but don't reset — let player clear output first
        } else {
            // Temp out of range or ingredients missing — reset
            resetProgress();
        }
    }

    // ── Crafting ──────────────────────────────────────────────────

    /**
     * Returns the matching recipe only if the flame stand temperature is currently
     * within [minTemp, maxTemp] AND the ingredients are present.
     * This drives both progress advancement and recipe-display for the HUD.
     */
    private Optional<RecipeHolder<LowHumanPillCauldronRecipe>> getMatchingRecipe(FlameStandBlockEntity fs) {
        if (level == null) return Optional.empty();
        int temp = fs.getTemperature();
        return level.getRecipeManager().getRecipeFor(
                ModRecipes.CAULDRON_LOW_HUMAN_TYPE.get(),
                new PillCauldronInput(3, List.of(
                        getPedestalItem(pedestalLeftPos()),
                        getPedestalItem(pedestalBackPos()),
                        getPedestalItem(pedestalRightPos())
                ), temp),
                level
        );
    }

    private Optional<RecipeHolder<LowHumanPillCauldronRecipe>> getCurrentRecipe() {
        FlameStandBlockEntity fs = getFlameStand();
        if (fs == null || !fs.isLit()) {
            // Still provide ingredients for HUD display, just pass 0 heat
            if (level == null) return Optional.empty();
            return level.getRecipeManager().getRecipeFor(
                    ModRecipes.CAULDRON_LOW_HUMAN_TYPE.get(),
                    new PillCauldronInput(3, List.of(
                            getPedestalItem(pedestalLeftPos()),
                            getPedestalItem(pedestalBackPos()),
                            getPedestalItem(pedestalRightPos())
                    ), 0),
                    level
            );
        }
        return getMatchingRecipe(fs);
    }

    private boolean canOutput(LowHumanPillCauldronRecipe r) {
        ItemStack successSlot = itemHandler.getStackInSlot(OUTPUT_SUCCESS);
        ItemStack failSlot    = itemHandler.getStackInSlot(OUTPUT_FAIL);
        boolean canSuccess = successSlot.isEmpty() ||
                (ItemStack.isSameItemSameComponents(successSlot, r.getSuccess()) &&
                        successSlot.getCount() + r.getSuccess().getCount() <= successSlot.getMaxStackSize());
        boolean canFail = failSlot.isEmpty() ||
                (ItemStack.isSameItemSameComponents(failSlot, r.getFail()) &&
                        failSlot.getCount() + r.getFail().getCount() <= failSlot.getMaxStackSize());
        return canSuccess && canFail;
    }

    private void craftItem(FlameStandBlockEntity flameStand, LowHumanPillCauldronRecipe recipe) {
        int finalTemp = flameStand.getTemperature();

        // ── Purity calculation ────────────────────────────────────
        int condenserBonus = (getActiveSpiritCondenser() != null)
                ? SpiritCondenserBlockEntity.PURITY_BONUS : 0;
        int tempPurity = recipe.calcPurity(finalTemp, condenserBonus + flameStand.getPurityBonus());

        // Herb quality/age adds a flat bonus on top
        List<ItemStack> inputs = List.of(
                getPedestalItem(pedestalLeftPos()),
                getPedestalItem(pedestalBackPos()),
                getPedestalItem(pedestalRightPos())
        );
        int herbPurityBonus = HerbBonusEffects.calcHerbPurityBonus(inputs);
        int rawPurity       = Math.min(100, tempPurity + herbPurityBonus);

        // ── Realm calculation ─────────────────────────────────────
        int baseRealm = Math.min(9, Math.max(1,
                recipe.getPillRealmMajor() + flameStand.getRealmBonus()));

        int finalMajorRealm = resolveRealm(baseRealm, rawPurity, inputs);
        int finalPurity     = rawPurity;

        // If realm was downgraded (degraded craft), re-roll purity to < 90
        if (finalMajorRealm < baseRealm) {
            finalPurity = ThreadLocalRandom.current().nextInt(1, 90);
        }

        // ── Bonus effect ──────────────────────────────────────────
        String bonusEffect = HerbBonusEffects.rollBonusEffect(inputs, recipe.getBonusChance());

        // ── Output ────────────────────────────────────────────────
        boolean isSuccess = ThreadLocalRandom.current().nextDouble() < recipe.getChance();
        ItemStack output  = (isSuccess ? recipe.getSuccess() : recipe.getFail()).copy();
        int targetSlot    = isSuccess ? OUTPUT_SUCCESS : OUTPUT_FAIL;

        // New signature — no minor realm parameter
        PillEffectUtil.applyPillData(output, finalMajorRealm, finalPurity, bonusEffect);

        ItemStack current = itemHandler.getStackInSlot(targetSlot);
        if (current.isEmpty()) {
            itemHandler.setStackInSlot(targetSlot, output);
        } else if (ItemStack.isSameItemSameComponents(current, output) &&
                current.getCount() + output.getCount() <= current.getMaxStackSize()) {
            current.grow(output.getCount());
            itemHandler.setStackInSlot(targetSlot, current);
        }

        consumePedestalIngredients(recipe.getSizedIngredients());
    }

    /**
     * Resolves the final major realm from the base realm, raw purity, and input herbs.
     *
     * ── Upgrade (purity == 100) ───────────────────────────────────────────
     * When purity reaches exactly 100 (Peak), there is a chance to upgrade the
     * pill's major realm by 1. Chance sources stack:
     *   - Base chance: 15%
     *   - Herb quality bonus (best Peak herb in inputs adds up to 20% more)
     *
     * ── Downgrade (purity < recipe purity_min) ───────────────────────────
     * When the raw purity is below the recipe's purity_min the craft degraded.
     * The major realm is reduced by 1 (floor 1). The purity of the downgraded
     * pill is re-rolled to a random value below 90 by the caller.
     */
    private int resolveRealm(int baseRealm, int purity,
                             List<ItemStack> inputs) {
        // ── Downgrade check ───────────────────────────────────────
        // A purity of 0 would mean the craft failed entirely (handled by fail output).
        // Downgrade only if purity rolled below 1 (shouldn't happen in practice, but defensive).
        // The real downgrade trigger: purity fell below Basic threshold (1) is impossible.
        // Instead: if baseRealm > 1 and purity < 10 (extremely bad craft), downgrade.
        if (baseRealm > 1 && purity < 10) {
            return baseRealm - 1;
        }

        // ── Upgrade check ────────────────────────────────────────
        if (purity < 100) return baseRealm; // must be exactly 100 (Peak) to upgrade
        if (baseRealm >= 9) return baseRealm; // already at max

        double upgradeChance = 0.15; // base 15%
        double herbBonus     = HerbBonusEffects.calcHerbRealmUpgradeChance(inputs);
        double totalChance   = Math.min(0.85, upgradeChance + herbBonus);

        return (ThreadLocalRandom.current().nextDouble() < totalChance)
                ? baseRealm + 1
                : baseRealm;
    }

    private void resetProgress() {
        if (progress != 0 || maxProgress != 0) {
            progress = 0;
            maxProgress = 0;
            setChanged();
        }
    }

    // ── Menu / HUD helpers ────────────────────────────────────────

    public boolean isCrafting()       { return progress > 0 && maxProgress > 0; }
    public boolean isFlameStandLit()  { return data.get(2) == 1; }
    public int getCurrentTemp()       { return data.get(3); }
    public int getRecipeMinTemp()     { return data.get(4); }
    public int getRecipeMaxTemp()     { return data.get(5); }
    public int getRecipeIdealTemp()   { return data.get(6); }

    public int getScaledArrowProgress() {
        int prog = data.get(0), max = data.get(1);
        return max != 0 && prog != 0 ? prog * 21 / max : 0;
    }

    // ── Drops ─────────────────────────────────────────────────────

    public void drops() {
        SimpleContainer inv = new SimpleContainer(2);
        inv.setItem(0, itemHandler.getStackInSlot(OUTPUT_SUCCESS));
        inv.setItem(1, itemHandler.getStackInSlot(OUTPUT_FAIL));
        Containers.dropContents(level, worldPosition, inv);
    }

    // ── NBT ───────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        tag.put("inventory", itemHandler.serializeNBT(reg));
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        super.saveAdditional(tag, reg);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        itemHandler.deserializeNBT(reg, tag.getCompound("inventory"));
        progress    = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider reg) { return saveWithoutMetadata(reg); }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}