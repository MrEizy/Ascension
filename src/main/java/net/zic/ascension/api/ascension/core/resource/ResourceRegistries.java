package net.zic.ascension.api.ascension.core.resource;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class ResourceRegistries {
    public static final Registry<ResourceType> RESOURCE_TYPE_REGISTRY = RegistryHelper.registry(
            AscensionCraft.MOD_ID,
            "resource_type"
    );

    private ResourceRegistries() {
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(RESOURCE_TYPE_REGISTRY);
    }
}
