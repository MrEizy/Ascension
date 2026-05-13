package net.thejadeproject.ascension.menus;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;

import net.thejadeproject.ascension.menus.custom.herb_pouch.HerbPouchMenu;
import net.thejadeproject.ascension.menus.custom.pill_cauldron.PillCauldronLowHumanMenu;
import net.thejadeproject.ascension.menus.custom.spirit_ring.SpatialRingInventoryMenu;
import net.thejadeproject.ascension.menus.custom.spirit_ring.SpatialRingModifierMenu;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AscensionCraft.MOD_ID);



    public static final DeferredHolder<MenuType<?>, MenuType<PillCauldronLowHumanMenu>> PILL_CAULDRON_LOW_HUMAN_MENU =
            registerMenuType("pill_cauldron_low_human_menu", PillCauldronLowHumanMenu::new);


    public static final DeferredHolder<MenuType<?>,MenuType<SpatialRingInventoryMenu>> SPATIAL_RING_INVENTORY_MENU =
            registerMenuType("spirit_ring_inventory_menu", SpatialRingInventoryMenu::new);

    public static final DeferredHolder<MenuType<?>,MenuType<SpatialRingModifierMenu>> SPATIAL_RING_MODIFIER_MENU =
            registerMenuType("spirit_ring_modifier_menu", SpatialRingModifierMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<HerbPouchMenu>> HERB_POUCH_MENU =
            registerMenuType("herb_pouch_menu", HerbPouchMenu::new);



    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }


    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
