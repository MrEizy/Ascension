package net.zic.ascension.impl.datapack.targeting;

import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.targeting.ConeTargeting;
import net.zic.ascension.impl.core.targeting.LookPositionTargeting;
import net.zic.ascension.impl.core.targeting.RadialTargeting;
import net.zic.ascension.impl.core.targeting.RayTargeting;
import net.zic.ascension.impl.core.targeting.SelfTargeting;

public final class AscensionTargetingTypes {
    public static final DeferredRegister<CodecType<TargetingDefinition>> TARGETING_TYPES = DeferredRegister.create(
            TypeRegistries.TARGETING_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> SELF = TARGETING_TYPES.register(
            "self",
            () -> new CodecType<>(SelfTargeting.CODEC)
    );
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> RADIAL = TARGETING_TYPES.register(
            "radial",
            () -> new CodecType<>(RadialTargeting.CODEC)
    );
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> RAY = TARGETING_TYPES.register(
            "ray",
            () -> new CodecType<>(RayTargeting.CODEC)
    );
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> CONE = TARGETING_TYPES.register(
            "cone",
            () -> new CodecType<>(ConeTargeting.CODEC)
    );
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> LOOK_POSITION = TARGETING_TYPES.register(
            "look_position",
            () -> new CodecType<>(LookPositionTargeting.CODEC)
    );

    private AscensionTargetingTypes() {
    }

    public static void register(IEventBus eventBus) {
        TARGETING_TYPES.register(eventBus);
    }
}
