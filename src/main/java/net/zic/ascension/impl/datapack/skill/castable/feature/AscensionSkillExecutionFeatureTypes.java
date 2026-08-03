package net.zic.ascension.impl.datapack.skill.castable.feature;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.feature.*;

public final class AscensionSkillExecutionFeatureTypes {
    public static final DeferredRegister<CodecType<SkillExecutionFeature>> FEATURE_TYPES = DeferredRegister.create(
            TypeRegistries.SKILL_EXECUTION_FEATURE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> MESSAGE = register("message", MessageFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> SOUND = register("sound", SoundFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> PARTICLE_BURST = register("particle_burst", ParticleBurstFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> RESOURCE_TRANSACTION = register("resource_transaction", ResourceTransactionFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> FROZEN_BUILDUP = register("frozen_buildup", FrozenBuildupFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> SKILL_EFFECT = register("skill_effect", SkillEffectFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> MOVEMENT = register("movement", MovementFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> MOVEMENT_ANCHOR = register("movement_anchor", MovementAnchorFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> SPAWN_PROJECTILE = register("spawn_projectile", SpawnProjectileFeature.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> RUNTIME_OBJECT = register("runtime_object", RuntimeObjectFeature.CODEC);

    private AscensionSkillExecutionFeatureTypes() {
    }

    private static <T extends SkillExecutionFeature> DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> register(
            String id,
            com.mojang.serialization.MapCodec<T> codec
    ) {
        return FEATURE_TYPES.register(id, () -> new CodecType<>(codec));
    }

    public static void register(IEventBus eventBus) {
        FEATURE_TYPES.register(eventBus);
    }
}
