package net.zic.ascension.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AscensionCraft.MOD_ID);

    public static final Supplier<CreativeModeTab> ARTIFACT_ITEMS_TAB = CREATIVE_MODE_TABS.register("artifact_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.TABLET_OF_DESTRUCTION_EARTH.get()))
                    .title(Component.translatable("creativetab.ascension.artifact_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_HUMAN);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_EARTH);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_HEAVEN);


                    }).build());

    public static void register(IEventBus eventBus) {

    }
}
