package net.thejadeproject.ascension.util;

import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.ArrayList;
import java.util.List;

public class ItemHelper {

    /**
     * Creates a physique transfer item stack for a specific physique
     * @param physiqueId The ID of the physique (e.g., "ascension:heavenborn_stone_monkey_physique")
     * @return An ItemStack configured for that physique
     */
    public static ItemStack createPhysiqueTransferItem(String physiqueId) {
        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
        stack.set(ModDataComponents.PHYSIQUE_ID, physiqueId);
        return stack;
    }

    /**
     * Creates physique transfer items for all registered physiques
     * @return List of ItemStacks for all physiques
     */
    public static List<ItemStack> createAllPhysiqueTransferItems() {
        List<ItemStack> items = new ArrayList<>();

        // Add empty vessel first
        items.add(createPhysiqueTransferItem("ascension:empty_vessel"));

        // Add all other physiques
        AscensionRegistries.Physiques.PHSIQUES_REGISTRY.keySet().forEach(resourceLocation -> {
            String id = resourceLocation.toString();
            if (!id.equals("ascension:empty_vessel")) {
                items.add(createPhysiqueTransferItem(id));
            }
        });

        return items;
    }
}