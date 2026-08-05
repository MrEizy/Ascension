package net.zic.ascension.impl.datapack.targeting;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.targeting.TargetingDefinitions;

public final class AscensionTargetingTypes {
    private static final ExtensionTypeRegistry<TargetingDefinition> TYPES = new ExtensionTypeRegistry<>(
            TypeRegistries.TARGETING_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> SELF = TYPES.add("self", TargetingDefinitions.Self.CODEC);
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> RADIAL = TYPES.add("radial", TargetingDefinitions.Radial.CODEC);
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> RAY = TYPES.add("ray", TargetingDefinitions.Ray.CODEC);
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> CONE = TYPES.add("cone", TargetingDefinitions.Cone.CODEC);
    public static final DeferredHolder<CodecType<TargetingDefinition>, CodecType<TargetingDefinition>> LOOK_POSITION = TYPES.add("look_position", TargetingDefinitions.LookPosition.CODEC);

    private AscensionTargetingTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
