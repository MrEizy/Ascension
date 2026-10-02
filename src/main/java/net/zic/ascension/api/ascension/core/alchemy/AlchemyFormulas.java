package net.zic.ascension.api.ascension.core.alchemy;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

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
