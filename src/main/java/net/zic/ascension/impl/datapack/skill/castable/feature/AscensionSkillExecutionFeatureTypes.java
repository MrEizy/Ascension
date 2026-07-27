package net.zic.ascension.impl.datapack.skill.castable.feature;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.feature.*;

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

    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> MOVEMENT = FEATURE_TYPES.register(
            "movement",
            () -> new SkillExecutionFeatureType(MovementFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> SET_MOVEMENT_ANCHOR = FEATURE_TYPES.register(
            "set_movement_anchor",
            () -> new SkillExecutionFeatureType(SetMovementAnchorFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> CLEAR_MOVEMENT_ANCHOR = FEATURE_TYPES.register(
            "clear_movement_anchor",
            () -> new SkillExecutionFeatureType(ClearMovementAnchorFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> VIRTUAL_PROJECTILE = FEATURE_TYPES.register(
            "virtual_projectile",
            () -> new SkillExecutionFeatureType(VirtualProjectileFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> SPAWN_AREA_FIELD = FEATURE_TYPES.register(
            "spawn_area_field",
            () -> new SkillExecutionFeatureType(SpawnAreaFieldFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> REMOVE_AREA_FIELD = FEATURE_TYPES.register(
            "remove_area_field",
            () -> new SkillExecutionFeatureType(RemoveAreaFieldFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> SPAWN_FORMATION = FEATURE_TYPES.register(
            "spawn_formation",
            () -> new SkillExecutionFeatureType(SpawnFormationFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> REMOVE_FORMATION = FEATURE_TYPES.register(
            "remove_formation",
            () -> new SkillExecutionFeatureType(RemoveFormationFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> SPAWN_CONSTRUCT = FEATURE_TYPES.register(
            "spawn_construct",
            () -> new SkillExecutionFeatureType(SpawnConstructFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> REMOVE_CONSTRUCT = FEATURE_TYPES.register(
            "remove_construct",
            () -> new SkillExecutionFeatureType(RemoveConstructFeature.CODEC)
    );
    public static final DeferredHolder<SkillExecutionFeatureType, SkillExecutionFeatureType> RESTORE_CONSTRUCT = FEATURE_TYPES.register(
            "restore_construct",
            () -> new SkillExecutionFeatureType(RestoreConstructFeature.CODEC)
    );

    private AscensionSkillExecutionFeatureTypes() {
    }

    public static void register(IEventBus eventBus) {
        FEATURE_TYPES.register(eventBus);
    }
}
