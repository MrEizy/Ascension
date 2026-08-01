package net.zic.ascension.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AscensionCraft.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ASCENSION_ITEMS_TAB = CREATIVE_MODE_TABS.register("ascension_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.TABLET_OF_DESTRUCTION_EARTH.get()))
                    .title(Component.translatable("creativetab.ascension.items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_HUMAN);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_EARTH);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_HEAVEN);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_ASCENDANT);


                    }).build());






































    public static final Supplier<CreativeModeTab> PHYSIQUE_TRANSFERS_TAB = CREATIVE_MODE_TABS.register("physique_transfers_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.PHYSIQUE_ESSENCE.get()))
                    .title(Component.translatable("creativetab.ascension.physique_transfers"))
                    .displayItems((itemDisplayParameters, output) ->
                            itemDisplayParameters.holders()
                                    .lookupOrThrow(CoreRegistries.PHYSIQUE_REGISTRY.key())
                                    .listElements()
                                    .forEach(holder -> {
                                        Identifier physiqueId = holder.key().identifier();
                                        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
                                        stack.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), physiqueId);
                                        output.accept(stack);
                                    }))
                    .build());

    public static final Supplier<CreativeModeTab> BLOODLINE_TRANSFERS_TAB = CREATIVE_MODE_TABS.register("bloodline_transfers_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BLOODLINE_ESSENCE.get()))
                    .title(Component.translatable("creativetab.ascension.bloodline_transfers"))
                    .displayItems((itemDisplayParameters, output) ->
                            itemDisplayParameters.holders()
                                    .lookupOrThrow(CoreRegistries.BLOODLINE_REGISTRY.key())
                                    .listElements()
                                    .forEach(holder -> {
                                        Identifier bloodlineId = holder.key().identifier();
                                        ItemStack stack = new ItemStack(ModItems.BLOODLINE_ESSENCE.get());
                                        stack.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), bloodlineId);
                                        stack.set(AscensionComponents.PURITY.get(), 100);
                                        output.accept(stack);
                                    }))
                    .build());

    public static final Supplier<CreativeModeTab> TECHNIQUE_TRANSFERS_TAB = CREATIVE_MODE_TABS.register("technique_transfers_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.TECHNIQUE_MANUAL.get()))
                    .title(Component.translatable("creativetab.ascension.technique_transfers"))
                    .displayItems((itemDisplayParameters, output) ->
                            itemDisplayParameters.holders()
                                    .lookupOrThrow(CoreRegistries.TECHNIQUE_REGISTRY.key())
                                    .listElements()
                                    .forEach(holder -> {
                                        Identifier techniqueId = holder.key().identifier();
                                        ItemStack stack = new ItemStack(ModItems.TECHNIQUE_MANUAL.get());
                                        stack.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), techniqueId);
                                        output.accept(stack);
                                    }))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
