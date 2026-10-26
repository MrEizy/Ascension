package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

/**
 * Registers datapack-defined medicinal reactions and composition-to-effect expressions.
 * These registries are separate from the legacy alchemy formula registry.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MedicinalRegistries {
    public static final RegistryHelper.DataPackRegistry<MedicinalReaction> REACTIONS = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "alchemy/reactions", () -> MedicinalReaction.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<MedicinalEffectExpression> EXPRESSIONS = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "alchemy/effect_expressions", () -> MedicinalEffectExpression.CODEC
    );

    private MedicinalRegistries() {
    }

    @SubscribeEvent
    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REACTIONS.key(), REACTIONS.codec().get(), REACTIONS.codec().get());
        event.dataPackRegistry(EXPRESSIONS.key(), EXPRESSIONS.codec().get(), EXPRESSIONS.codec().get());
    }
}
