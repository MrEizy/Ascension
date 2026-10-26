package net.zic.ascension.api.ascension.core.alchemy;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

/**
 * Legacy fixed-formula alchemy retained temporarily for references in existing alpha content.
 * Procedural condensation now uses MedicinalPillFactory instead of registered recipe outputs.
 */
@Deprecated(forRemoval = true)
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AlchemyFormulas {
    public static final RegistryHelper.DataPackRegistry<AlchemyFormula> REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "alchemy/formulas",
            () -> AlchemyFormula.CODEC
    );

    private AlchemyFormulas() {
    }

    @SubscribeEvent
    public static void registerDatapackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY.key(), REGISTRY.codec().get(), REGISTRY.codec().get());
    }
}
