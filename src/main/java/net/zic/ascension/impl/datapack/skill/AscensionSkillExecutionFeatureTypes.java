package net.zic.ascension.impl.datapack.skill;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.SkillFeatures;
import net.zic.ascension.impl.core.skill.passive.PassiveModules;
import net.zic.ascension.impl.core.effect.AscensionBuildupChannels;

public final class AscensionSkillExecutionFeatureTypes {
    private static final ExtensionTypeRegistry<SkillExecutionFeature> TYPES = new ExtensionTypeRegistry<>(
            TypeRegistries.SKILL_EXECUTION_FEATURE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> MESSAGE = TYPES.add("message", SkillFeatures.Message.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> SOUND = TYPES.add("sound", SkillFeatures.Sound.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> PARTICLES = TYPES.add("particles", SkillFeatures.Particles.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> RESOURCE = TYPES.add("resource", SkillFeatures.Resource.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> DAMAGE = TYPES.add("damage", SkillFeatures.Damage.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> EFFECT = TYPES.add("effect", SkillFeatures.Effect.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> BUILDUP = TYPES.add("buildup", SkillFeatures.Buildup.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> STAGGER = TYPES.add("stagger", SkillFeatures.Stagger.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> BARRIER = TYPES.add("barrier", SkillFeatures.Barrier.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> PROJECTILE = TYPES.add("projectile", SkillFeatures.Projectile.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> FIELD = TYPES.add("field", SkillFeatures.Field.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> NETWORK = TYPES.add("network", SkillFeatures.Network.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> CONSTRUCT = TYPES.add("construct", SkillFeatures.Construct.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> MOVE = TYPES.add("move", SkillFeatures.Move.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> ANCHOR = TYPES.add("anchor", SkillFeatures.Anchor.CODEC);
    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> VISUAL = TYPES.add("visual", SkillFeatures.Visual.CODEC);

    private AscensionSkillExecutionFeatureTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
        PassiveModules.register(eventBus);
        AscensionBuildupChannels.register(eventBus);
    }
}
