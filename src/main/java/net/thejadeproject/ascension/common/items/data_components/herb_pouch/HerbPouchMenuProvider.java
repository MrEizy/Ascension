package net.thejadeproject.ascension.common.items.data_components.herb_pouch;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.thejadeproject.ascension.menus.custom.herb_pouch.HerbPouchMenu;
import org.jetbrains.annotations.Nullable;

public class HerbPouchMenuProvider implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return Component.translatable("item.ascension.herb_pouch");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new HerbPouchMenu(containerId, inventory, null);
    }
}