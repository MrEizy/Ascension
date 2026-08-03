package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.value.ScaledValueSource;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public final class AscensionScaledValueSourceTypes {
    public static final DeferredRegister<CodecType<ScaledValueSource>> SOURCE_TYPES =
            DeferredRegister.create(TypeRegistries.SCALED_VALUE_SOURCE_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<CodecType<ScaledValueSource>, CodecType<ScaledValueSource>> CONSTANT = SOURCE_TYPES.register(
            "constant",
            () -> new CodecType<>(ConstantScaledValueSource.CODEC)
    );
    public static final DeferredHolder<CodecType<ScaledValueSource>, CodecType<ScaledValueSource>> SKILL_LEVEL = SOURCE_TYPES.register(
            "skill_level",
            () -> new CodecType<>(SkillLevelScaledValueSource.CODEC)
    );
    public static final DeferredHolder<CodecType<ScaledValueSource>, CodecType<ScaledValueSource>> CHARGE = SOURCE_TYPES.register(
            "charge",
            () -> new CodecType<>(ChargeScaledValueSource.CODEC)
    );
    public static final DeferredHolder<CodecType<ScaledValueSource>, CodecType<ScaledValueSource>> STAT = SOURCE_TYPES.register(
            "stat",
            () -> new CodecType<>(StatScaledValueSource.CODEC)
    );
    public static final DeferredHolder<CodecType<ScaledValueSource>, CodecType<ScaledValueSource>> AFFINITY = SOURCE_TYPES.register(
            "affinity",
            () -> new CodecType<>(AffinityScaledValueSource.CODEC)
    );
    public static final DeferredHolder<CodecType<ScaledValueSource>, CodecType<ScaledValueSource>> CONTEXT = SOURCE_TYPES.register(
            "context",
            () -> new CodecType<>(ContextScaledValueSource.CODEC)
    );

    private AscensionScaledValueSourceTypes() {
    }

    public static void register(IEventBus eventBus) {
        SOURCE_TYPES.register(eventBus);
    }
}
