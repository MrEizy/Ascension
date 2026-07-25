package net.zic.ascension.impl.datapack.skill.castable.held;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeatureType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.held.feature.MessageReleaseFeature;
import net.zic.ascension.impl.core.skill.castable.held.feature.ParticleBurstReleaseFeature;
import net.zic.ascension.impl.core.skill.castable.held.feature.ResourceTransactionReleaseFeature;
import net.zic.ascension.impl.core.skill.castable.held.feature.SoundReleaseFeature;

public final class AscensionHeldCastReleaseFeatureTypes {
    public static final DeferredRegister<HeldCastReleaseFeatureType> TYPES = DeferredRegister.create(
            TypeRegistries.HELD_CAST_RELEASE_FEATURE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<HeldCastReleaseFeatureType, HeldCastReleaseFeatureType> MESSAGE = TYPES.register(
            "message",
            () -> new HeldCastReleaseFeatureType(MessageReleaseFeature.CODEC)
    );
    public static final DeferredHolder<HeldCastReleaseFeatureType, HeldCastReleaseFeatureType> SOUND = TYPES.register(
            "sound",
            () -> new HeldCastReleaseFeatureType(SoundReleaseFeature.CODEC)
    );
    public static final DeferredHolder<HeldCastReleaseFeatureType, HeldCastReleaseFeatureType> PARTICLE_BURST = TYPES.register(
            "particle_burst",
            () -> new HeldCastReleaseFeatureType(ParticleBurstReleaseFeature.CODEC)
    );
    public static final DeferredHolder<HeldCastReleaseFeatureType, HeldCastReleaseFeatureType> RESOURCE_TRANSACTION = TYPES.register(
            "resource_transaction",
            () -> new HeldCastReleaseFeatureType(ResourceTransactionReleaseFeature.CODEC)
    );

    private AscensionHeldCastReleaseFeatureTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
