package net.thejadeproject.ascension.events;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.physiques.PhysiqueTransferItem;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AnvilEvents {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() == ModItems.PHYSIQUE_ESSENCE.get() &&
                right.getItem() == ModItems.PHYSIQUE_ESSENCE.get()) {

            if (PhysiqueTransferItem.canCombine(left, right)) {
                int combinedPurity = PhysiqueTransferItem.getCombinedPurity(left, right);
                String physiqueId = left.get(ModDataComponents.PHYSIQUE_ID.get());
                ItemStack result = PhysiqueTransferItem.createWithPhysique(physiqueId, combinedPurity);

                event.setOutput(result);

                int baseCost = 1;
                int purityBonus = Math.min(combinedPurity / 10, 5);
                event.setCost(baseCost + purityBonus);

                event.setMaterialCost(1);

                event.setOutput(result);
            }
        }
    }
}
