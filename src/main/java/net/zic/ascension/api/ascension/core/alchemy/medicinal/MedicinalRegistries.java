package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MedicinalRegistries {
    public static final RegistryHelper.DataPackRegistry<MedicinalReaction> REACTIONS = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "alchemy/reactions", () -> MedicinalReaction.CODEC
    );

    public static final RegistryHelper.DataPackRegistry<MedicinalEffectDefinition> EFFECTS = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "alchemy/effects", () -> MedicinalEffectDefinition.CODEC
    );

    private MedicinalRegistries() {
    }

    @SubscribeEvent
    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REACTIONS.key(), REACTIONS.codec().get(), REACTIONS.codec().get());
        event.dataPackRegistry(EFFECTS.key(), EFFECTS.codec().get(), EFFECTS.codec().get());
    }
}
