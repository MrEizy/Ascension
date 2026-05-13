package net.thejadeproject.ascension.common.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradeItem extends Item {
    private final String upgradeType;

    public UpgradeItem(String upgradeType, Rarity rarity) {
        super(new Properties()
                .stacksTo(1)
                .rarity(rarity));
        this.upgradeType = upgradeType;
    }

    public String getUpgradeType() {
        return upgradeType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        switch(upgradeType) {
            case "stack_upgrade_t1":
                tooltipComponents.add(Component.translatable("tooltip.ascension.stack_upgrade.t1")
                        .withStyle(ChatFormatting.GREEN));
                break;
            case "stack_upgrade_t2":
                tooltipComponents.add(Component.translatable("tooltip.ascension.stack_upgrade.t2")
                        .withStyle(ChatFormatting.GREEN));
                break;
            case "pickup_upgrade":
                tooltipComponents.add(Component.translatable("tooltip.ascension.pickup_upgrade")
                        .withStyle(ChatFormatting.BLUE));
                break;
        }
    }

}
