package net.zic.ascension.impl.datapack.skill.castable.feature;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.feature.FrozenBuildupFeature;
import net.zic.ascension.impl.core.skill.castable.feature.MessageFeature;
import net.zic.ascension.impl.core.skill.castable.feature.ParticleBurstFeature;
import net.zic.ascension.impl.core.skill.castable.feature.ResourceTransactionFeature;
import net.zic.ascension.impl.core.skill.castable.feature.SkillEffectFeature;
import net.zic.ascension.impl.core.skill.castable.feature.SoundFeature;

public final class AscensionSkillExecutionFeatureTypes {
    public static final DeferredRegister<SkillExecutionFeatureType> FEATURE_TYPES = DeferredRegister.create(
            TypeRegistries.SKILL_EXECUTION_FEATURE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> MESSAGE = FEATURE_TYPES.register(
            "message",
            () -> new SkillExecutionFeatureType(MessageFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> SOUND = FEATURE_TYPES.register(
            "sound",
            () -> new SkillExecutionFeatureType(SoundFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> PARTICLE_BURST = FEATURE_TYPES.register(
            "particle_burst",
            () -> new SkillExecutionFeatureType(ParticleBurstFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> RESOURCE_TRANSACTION = FEATURE_TYPES.register(
            "resource_transaction",
            () -> new SkillExecutionFeatureType(ResourceTransactionFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> FROZEN_BUILDUP = FEATURE_TYPES.register(
            "frozen_buildup",
            () -> new SkillExecutionFeatureType(FrozenBuildupFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> SKILL_EFFECT = FEATURE_TYPES.register(
            "skill_effect",
            () -> new SkillExecutionFeatureType(SkillEffectFeature.CODEC)
    );

    private AscensionSkillExecutionFeatureTypes() {
    }

    public static void register(IEventBus eventBus) {
        FEATURE_TYPES.register(eventBus);
    }
}
