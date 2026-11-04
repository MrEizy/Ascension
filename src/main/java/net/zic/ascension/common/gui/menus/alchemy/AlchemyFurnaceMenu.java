package net.zic.ascension.common.gui.menus.alchemy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyRefinementPlan;
import net.zic.ascension.impl.core.technique.AlchemyTechnique;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.minecraft.resources.Identifier;
import java.util.List;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;
import net.zic.ascension.common.gui.menus.AscMenuTypes;

public class AlchemyFurnaceMenu extends AbstractContainerMenu {
    private final AlchemyFurnaceBlockEntity furnace;
    private final Container container;
    private final ContainerData progress;

    public AlchemyFurnaceMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof AlchemyFurnaceBlockEntity entity ? entity : null);
    }

    public AlchemyFurnaceMenu(int id, Inventory playerInventory, AlchemyFurnaceBlockEntity furnace) {
        super(AscMenuTypes.ALCHEMY_FURNACE.get(), id);
        this.furnace = furnace;
        this.container = furnace == null ? new SimpleContainer(4) : furnace.inventory();
        this.progress = furnace == null ? new SimpleContainerData(7) : furnace.menuData();
        addSlot(new Slot(container, 0, 53, 20) {
            @Override public boolean mayPlace(ItemStack stack) { return editable() && AlchemyMaterials.resolve(stack, net.zic.ascension.api.ascension.core.alchemy.AlchemyContext.of(playerInventory.player)).isPresent(); }
            @Override public boolean mayPickup(Player player) { return editable(); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, 1, 80, 9) {
            @Override public boolean mayPlace(ItemStack stack) { return editable() && AlchemyMaterials.resolve(stack, net.zic.ascension.api.ascension.core.alchemy.AlchemyContext.of(playerInventory.player)).isPresent(); }
            @Override public boolean mayPickup(Player player) { return editable(); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, 2, 107, 20) {
            @Override public boolean mayPlace(ItemStack stack) { return editable() && AlchemyMaterials.resolve(stack, net.zic.ascension.api.ascension.core.alchemy.AlchemyContext.of(playerInventory.player)).isPresent(); }
            @Override public boolean mayPickup(Player player) { return editable(); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, 3, 98, 59) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        addDataSlots(progress);
    }

    private boolean editable() { return furnace == null || furnace.phase() == AlchemyFurnaceBlockEntity.Phase.IDLE; }
    public int phase() { return progress.get(0); }
    public int phaseTicks() { return progress.get(1); }
    public int preparationIndex(int index) { return index < 0 || index > 4 ? 0 : progress.get(index + 2); }

    public String selectionLabel(int index, Player player) {
        if (index < 0 || index > 4 || player == null) return "";
        if (index == 4) {
            List<Identifier> effects = AlchemyRefinementPlan.focusChoices(player.level().registryAccess());
            int selection = progress.get(6);
            return selection <= 0 || selection > effects.size() ? "Auto" : pretty(effects.get(selection - 1).getPath());
        }
        AlchemyTechnique.Stage stage = AlchemyTechnique.Stage.values()[index];
        List<Identifier> choices = AlchemyRefinementPlan.choices(player.level().registryAccess(), stage);
        int selection = progress.get(index + 2);
        if (choices.isEmpty()) return "Default";
        Identifier selected = choices.get(Math.max(0, Math.min(selection, choices.size() - 1)));
        Technique<?> technique = CoreRegistries.TECHNIQUE_REGISTRY.get(player.level().registryAccess()).getValue(selected);
        return technique == null ? pretty(selected.getPath()) : technique.getName(null).getString();
    }

    public String selectionDetails(int index, Player player) {
        if (player == null) return "";
        if (index == 4) return "Only steers toward effects this medicine can already express; steering may reduce potency.";
        if (index < 0 || index > 3) return "";
        AlchemyTechnique.Stage stage = AlchemyTechnique.Stage.values()[index];
        List<Identifier> options = AlchemyRefinementPlan.choices(player.level().registryAccess(), stage);
        if (options.isEmpty()) return "No techniques loaded";
        Identifier id = options.get(Math.max(0, Math.min(progress.get(index + 2), options.size() - 1)));
        Technique<?> knownTechnique = CoreRegistries.TECHNIQUE_REGISTRY.get(player.level().registryAccess()).getValue(id);
        if (!(knownTechnique instanceof AlchemyTechnique technique)) return "Missing alchemy technique";
        AlchemyTechnique.Refinement method = technique.refinement();
        String explanation = technique.getDescription(null).getString();
        String access = technique.basic() ? "Basic technique. "
                : AlchemyRefinementPlan.isLearnedOrBasic(player.level().registryAccess(), player, id)
                        ? "Learned technique. " : "Not yet learned. ";
        return access + explanation + " " + switch (stage) {
            case EXTRACTION -> String.format(java.util.Locale.ROOT,
                    "Essence yield %.0f%%, purity %+.0f%%, instability %+.0f%%",
                    method.yield() * 100, method.purityDelta() * 100, method.instabilityDelta() * 100);
            case HEATING -> String.format(java.util.Locale.ROOT,
                    "Heat %.2fx, control %.2fx, yield %.0f%%, instability %+.0f%%",
                    method.heat(), method.control(), method.yield() * 100, method.instabilityDelta() * 100);
            case STABILIZATION -> String.format(java.util.Locale.ROOT,
                    "Yield %.0f%%, purity %+.0f%%/herb, instability %+.0f%%/herb",
                    method.yield() * 100, method.purityDelta() * 100, method.instabilityDelta() * 100);
            case CONDENSATION -> String.format(java.util.Locale.ROOT,
                    "Ingredient quality %+.0f%%, focus precision %.0f%%",
                    method.qualityBonus() * 100, method.focusPrecision() * 100);
        };
    }

    private static String pretty(String path) {
        String value = path.replace('_', ' ');
        return value.isEmpty() ? "Default" : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (furnace == null || id < 0 || id >= 5 || !stillValid(player)) return false;
        return furnace.cyclePreparation(id, player);
    }
    @Override public boolean stillValid(Player player) {
        return furnace == null || (player.level().getBlockEntity(furnace.getBlockPos()) == furnace
                && player.distanceToSqr(furnace.getBlockPos().getX() + 0.5, furnace.getBlockPos().getY() + 0.5, furnace.getBlockPos().getZ() + 0.5) <= 64.0);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index == 3) {
            if (!moveItemStackTo(original, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else if (index < 3) {
            if (!moveItemStackTo(original, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!editable() || !moveItemStackTo(original, 0, 3, false)) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
