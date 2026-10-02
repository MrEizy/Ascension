package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.value.ScaledValue;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public final class AscensionScaledValueSourceTypes {
    private static final ExtensionTypeRegistry<ScaledValue.Source> TYPES = new ExtensionTypeRegistry<>(
            TypeRegistries.SCALED_VALUE_SOURCE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> CONSTANT =
            TYPES.add("constant", ScaledValueSources.Constant.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> MASTERY =
            TYPES.add("mastery", ScaledValueSources.Mastery.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> LEVEL =
            TYPES.add("level", ScaledValueSources.Level.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> PATH_REALM_MULTIPLIER =
            TYPES.add("path_realm_multiplier", ScaledValueSources.PathRealmMultiplier.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> CHARGE =
            TYPES.add("charge", ScaledValueSources.Charge.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> STAT =
            TYPES.add("stat", ScaledValueSources.StatValue.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> AFFINITY =
            TYPES.add("affinity", ScaledValueSources.Affinity.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> CONTEXT =
            TYPES.add("context", ScaledValueSources.ContextValue.CODEC);
    public static final DeferredHolder<CodecType<ScaledValue.Source>, CodecType<ScaledValue.Source>> SKILL_EFFECT =
            TYPES.add("skill_effect", ScaledValueSources.EffectValue.CODEC);

    private AscensionScaledValueSourceTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
