package net.zic.ascension.api.ascension.core.alchemy;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AlchemyRefinementRegistries {
    public static final RegistryHelper.DataPackRegistry<AlchemyRefinementTechnique> TECHNIQUES = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "alchemy/techniques", () -> AlchemyRefinementTechnique.CODEC);

    private AlchemyRefinementRegistries() {
    }

    @SubscribeEvent
    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(TECHNIQUES.key(), TECHNIQUES.codec().get(), TECHNIQUES.codec().get());
    }
}
