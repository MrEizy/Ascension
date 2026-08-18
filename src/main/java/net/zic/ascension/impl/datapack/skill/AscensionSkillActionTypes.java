package net.zic.ascension.impl.datapack.skill;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.SkillActions;
import net.zic.ascension.impl.core.skill.passive.PassiveModules;
import net.zic.ascension.impl.core.effect.AscensionBuildupChannels;

public final class AscensionSkillActionTypes {
    private static final ExtensionTypeRegistry<SkillAction> TYPES = new ExtensionTypeRegistry<>(
            TypeRegistries.SKILL_ACTION_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> MASTERY_GATE = TYPES.add("mastery_gate", SkillActions.MasteryGate.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> MESSAGE = TYPES.add("message", SkillActions.Message.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> SOUND = TYPES.add("sound", SkillActions.Sound.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> PARTICLES = TYPES.add("particles", SkillActions.Particles.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> RESOURCE = TYPES.add("resource", SkillActions.Resource.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> CULTIVATE = TYPES.add("cultivate", SkillActions.Cultivate.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> DAMAGE = TYPES.add("damage", SkillActions.Damage.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> EFFECT = TYPES.add("effect", SkillActions.Effect.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> BUILDUP = TYPES.add("buildup", SkillActions.Buildup.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> STAGGER = TYPES.add("stagger", SkillActions.Stagger.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> BARRIER = TYPES.add("barrier", SkillActions.Barrier.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> PROJECTILE = TYPES.add("projectile", SkillActions.Projectile.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> FIELD = TYPES.add("field", SkillActions.Field.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> NETWORK = TYPES.add("network", SkillActions.Network.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> CONSTRUCT = TYPES.add("construct", SkillActions.Construct.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> MOVE = TYPES.add("move", SkillActions.Move.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> ANCHOR = TYPES.add("anchor", SkillActions.Anchor.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> VISUAL = TYPES.add("visual", SkillActions.Visual.CODEC);
    public static final DeferredHolder<CodecType<SkillAction>, CodecType<SkillAction>> WEAPON_SWING = TYPES.add("weapon_swing", SkillActions.WeaponSwing.CODEC);

    private AscensionSkillActionTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
        PassiveModules.register(eventBus);
        AscensionBuildupChannels.register(eventBus);
    }
}
