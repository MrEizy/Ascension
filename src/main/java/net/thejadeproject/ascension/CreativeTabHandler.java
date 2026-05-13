package net.thejadeproject.ascension;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.techniques.TechniquePageItem;
import net.thejadeproject.ascension.common.items.techniques.TechniqueTransferItem;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueManualRegistry;

public class CreativeTabHandler {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AscensionCraft.MOD_ID);

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);

        CREATIVE_TABS.register("technique_transfers", () -> CreativeModeTab.builder()
                .title(Component.literal("Ascension - Technique Manuals"))
                .icon(() -> TechniqueTransferItem.createWithTechnique("ascension:none"))
                .displayItems(((itemDisplayParameters, output) -> {
                    generateTechniqueTransferItems(output);
                    generateTechniquePages(output);
                    generateTechniqueBinders(output);
                }))
                .build()
        );

        CREATIVE_TABS.register("physique_transfers", () -> CreativeModeTab.builder()
                .title(Component.literal("Ascension - Physique Essences"))
                .icon(() -> {
                    ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
                    String firstPhysiqueId = "ascension:empty_vessel";

                    for (var entry : AscensionRegistries.Physiques.PHSIQUES_REGISTRY.entrySet()) {
                        if (!entry.getKey().toString().equals("ascension:empty_vessel")) {
                            firstPhysiqueId = entry.getKey().toString();
                            break;
                        }
                    }

                    stack.set(ModDataComponents.PHYSIQUE_ID.get(), firstPhysiqueId);
                    stack.set(ModDataComponents.PURITY.get(), 100);
                    return stack;
                })
                .displayItems((parameters, output) -> {
                    generatePhysiqueTransferItems(output);
                })
                .build());
    }

    private static void generateTechniqueTransferItems(CreativeModeTab.Output output) {
        AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.keySet().forEach(resourceLocation -> {
            output.accept(TechniqueTransferItem.createWithTechnique(resourceLocation.toString()));
        });
    }

    private static void generateTechniquePages(CreativeModeTab.Output output) {
        TechniqueManualRegistry.getRegisteredTechniques().forEach(techniqueId -> {
            var manualData = TechniqueManualRegistry.get(techniqueId).orElseThrow();
            for (int i = 0; i < manualData.requiredPages(); i++) {
                output.accept(TechniquePageItem.createWithTechnique(techniqueId.toString(), i));
            }
        });
    }

    private static void generateTechniqueBinders(CreativeModeTab.Output output) {
        output.accept(new ItemStack(ModItems.TECHNIQUE_BINDER.get()));
    }

    private static void generatePhysiqueTransferItems(CreativeModeTab.Output output) {
        AscensionRegistries.Physiques.PHSIQUES_REGISTRY.keySet().forEach(resourceLocation -> {
            ItemStack fullStack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
            fullStack.set(ModDataComponents.PHYSIQUE_ID.get(), resourceLocation.toString());
            fullStack.set(ModDataComponents.PURITY.get(), 100);
            output.accept(fullStack);
        });
    }
}