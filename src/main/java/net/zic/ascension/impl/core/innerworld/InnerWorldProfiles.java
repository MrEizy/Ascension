package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldProfiles {
    private InnerWorldProfiles() {
    }

    public static final ResourceKey<Registry<InnerWorldProfile>> REGISTRY =
            ResourceKey.createRegistryKey(AscensionCraft.prefix("inner_world_profile"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, InnerWorldProfile.CODEC);
    }
}