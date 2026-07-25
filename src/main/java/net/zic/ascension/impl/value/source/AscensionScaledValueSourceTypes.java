package net.zic.ascension.impl.value.source;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.value.source.ScaledValueSourceType;

public final class AscensionScaledValueSourceTypes {
    public static final DeferredRegister<ScaledValueSourceType> SOURCE_TYPES =
            DeferredRegister.create(TypeRegistries.SCALED_VALUE_SOURCE_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<ScaledValueSourceType, ScaledValueSourceType> CONSTANT = SOURCE_TYPES.register(
            "constant",
            () -> new ScaledValueSourceType(ConstantScaledValueSource.CODEC)
    );
    public static final DeferredHolder<ScaledValueSourceType, ScaledValueSourceType> SKILL_LEVEL = SOURCE_TYPES.register(
            "skill_level",
            () -> new ScaledValueSourceType(SkillLevelScaledValueSource.CODEC)
    );
    public static final DeferredHolder<ScaledValueSourceType, ScaledValueSourceType> CHARGE = SOURCE_TYPES.register(
            "charge",
            () -> new ScaledValueSourceType(ChargeScaledValueSource.CODEC)
    );
    public static final DeferredHolder<ScaledValueSourceType, ScaledValueSourceType> STAT = SOURCE_TYPES.register(
            "stat",
            () -> new ScaledValueSourceType(StatScaledValueSource.CODEC)
    );
    public static final DeferredHolder<ScaledValueSourceType, ScaledValueSourceType> AFFINITY = SOURCE_TYPES.register(
            "affinity",
            () -> new ScaledValueSourceType(AffinityScaledValueSource.CODEC)
    );
    public static final DeferredHolder<ScaledValueSourceType, ScaledValueSourceType> CONTEXT = SOURCE_TYPES.register(
            "context",
            () -> new ScaledValueSourceType(ContextScaledValueSource.CODEC)
    );

    private AscensionScaledValueSourceTypes() {
    }

    public static void register(IEventBus eventBus) {
        SOURCE_TYPES.register(eventBus);
    }
}
