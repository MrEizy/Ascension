package net.zic.ascension.impl.datapack.targeting;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.targeting.TargetingType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.core.targeting.ConeTargeting;
import net.zic.ascension.impl.core.targeting.LookPositionTargeting;
import net.zic.ascension.impl.core.targeting.RadialTargeting;
import net.zic.ascension.impl.core.targeting.RayTargeting;
import net.zic.ascension.impl.core.targeting.SelfTargeting;

public final class AscensionTargetingTypes {
    public static final DeferredRegister<TargetingType> TARGETING_TYPES = DeferredRegister.create(
            TypeRegistries.TARGETING_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<TargetingType, TargetingType> SELF = TARGETING_TYPES.register(
            "self",
            () -> new TargetingType(SelfTargeting.CODEC)
    );
    public static final DeferredHolder<TargetingType, TargetingType> RADIAL = TARGETING_TYPES.register(
            "radial",
            () -> new TargetingType(RadialTargeting.CODEC)
    );
    public static final DeferredHolder<TargetingType, TargetingType> RAY = TARGETING_TYPES.register(
            "ray",
            () -> new TargetingType(RayTargeting.CODEC)
    );
    public static final DeferredHolder<TargetingType, TargetingType> CONE = TARGETING_TYPES.register(
            "cone",
            () -> new TargetingType(ConeTargeting.CODEC)
    );
    public static final DeferredHolder<TargetingType, TargetingType> LOOK_POSITION = TARGETING_TYPES.register(
            "look_position",
            () -> new TargetingType(LookPositionTargeting.CODEC)
    );

    private AscensionTargetingTypes() {
    }

    public static void register(IEventBus eventBus) {
        TARGETING_TYPES.register(eventBus);
    }
}
