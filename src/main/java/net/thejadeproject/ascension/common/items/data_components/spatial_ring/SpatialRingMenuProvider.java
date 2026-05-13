package net.thejadeproject.ascension.common.items.data_components.spatial_ring;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.thejadeproject.ascension.menus.custom.spirit_ring.SpatialRingInventoryMenu;
import net.thejadeproject.ascension.menus.custom.spirit_ring.SpatialRingModifierMenu;
import org.jetbrains.annotations.Nullable;

public class SpatialRingMenuProvider implements MenuProvider {
    private final SpatialRingItemStackHandler.Type type;
    public SpatialRingMenuProvider(SpatialRingItemStackHandler.Type type){
        this.type = type;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if(type == SpatialRingItemStackHandler.Type.INVENTORY){
            return new SpatialRingInventoryMenu(i,inventory,null);
        }

        return new SpatialRingModifierMenu(i,inventory,null);
    }
}
