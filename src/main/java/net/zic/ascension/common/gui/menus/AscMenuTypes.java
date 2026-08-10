package net.zic.ascension.common.gui.menus;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.menus.jade_bottle.JadeBottleMenu;

public class AscMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, AscensionCraft.MOD_ID);


    public static final DeferredHolder<MenuType<?>, MenuType<JadeBottleMenu>> JADE_BOTTLE =
            MENU_TYPES.register("jade_bottle",
                    () -> IMenuTypeExtension.create(JadeBottleMenu::fromNetwork));


    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
