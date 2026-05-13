package net.thejadeproject.ascension.common.items.artifacts;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.Config;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.EntityDataManager;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.util.ModTags;

public class RepairSlip extends Item {
    private static final int DEFAULT_REPAIR_INTERVAL = 100;
    private static final int DEFAULT_REPAIR_AMOUNT = 2;

    public RepairSlip(Properties properties) {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (!isFirstRepairSlip(player, stack)) {
                return;
            }

            int repairInterval = Config.COMMON.REPAIR_INTERVAL != null ?
                    Config.COMMON.REPAIR_INTERVAL.get() : DEFAULT_REPAIR_INTERVAL;

            if (player.tickCount % repairInterval == 0) {
                repairItems(player);
            }
        }
    }

    private boolean isFirstRepairSlip(Player player, ItemStack thisStack) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof RepairSlip) {
                return stack == thisStack;
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof RepairSlip) {
                return stack == thisStack;
            }
        }
        ItemStack offhand = player.getInventory().offhand.get(0);
        if (offhand.getItem() instanceof RepairSlip) {
            return offhand == thisStack;
        }
        return false;
    }

    private void repairItems(Player player) {
        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (entityData == null) return;

        int repairAmount = Config.COMMON.REPAIR_AMOUNT != null ?
                Config.COMMON.REPAIR_AMOUNT.get() : DEFAULT_REPAIR_AMOUNT;

        for (ItemStack stack : player.getInventory().items) {
            if (repairItem(entityData, stack, repairAmount)) return;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (repairItem(entityData, stack, repairAmount)) return;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (repairItem(entityData, stack, repairAmount)) return;
        }
    }

    private boolean repairItem(IEntityData entityData, ItemStack stack, int repairAmount) {
        if (!stack.isEmpty() && stack.isDamaged() && !stack.is(ModTags.Items.REPAIR_BLACKLIST)) {
            if (!entityData.getQiContainer().tryConsumeQi(5)) return true;

            stack.setDamageValue(Math.max(stack.getDamageValue() - repairAmount, 0));
            return true;
        }
        return false;
    }

}