package net.zic.ascension.impl.resource;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.resource.ResourceRegistries;
import net.zic.ascension.api.core.resource.ResourceType;
import net.zic.ascension.impl.resource.type.PlayerExhaustionResourceType;
import net.zic.ascension.impl.resource.type.PlayerHungerResourceType;
import net.zic.ascension.impl.resource.type.PlayerSaturationResourceType;
import net.zic.ascension.impl.resource.type.QiResourceType;
import net.zic.ascension.impl.resource.type.StaminaResourceType;

public final class AscensionResourceTypes {
    public static final DeferredRegister<ResourceType> RESOURCE_TYPES = DeferredRegister.create(
            ResourceRegistries.RESOURCE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<ResourceType, ResourceType> QI = RESOURCE_TYPES.register(
            "qi",
            QiResourceType::new
    );
    public static final DeferredHolder<ResourceType, ResourceType> STAMINA = RESOURCE_TYPES.register(
            "stamina",
            StaminaResourceType::new
    );
    public static final DeferredHolder<ResourceType, ResourceType> EXHAUSTION = RESOURCE_TYPES.register(
            "exhaustion",
            PlayerExhaustionResourceType::new
    );
    public static final DeferredHolder<ResourceType, ResourceType> HUNGER = RESOURCE_TYPES.register(
            "hunger",
            PlayerHungerResourceType::new
    );
    public static final DeferredHolder<ResourceType, ResourceType> SATURATION = RESOURCE_TYPES.register(
            "saturation",
            PlayerSaturationResourceType::new
    );

    private AscensionResourceTypes() {
    }

    public static void register(IEventBus eventBus) {
        RESOURCE_TYPES.register(eventBus);
    }
}
