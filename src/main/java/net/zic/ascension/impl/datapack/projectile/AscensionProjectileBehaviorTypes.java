package net.zic.ascension.impl.datapack.projectile;

import net.zic.ascension.api.core.projectile.ProjectileBehavior;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.projectile.HomingProjectileBehavior;

public final class AscensionProjectileBehaviorTypes {
    public static final DeferredRegister<CodecType<ProjectileBehavior>> TYPES = DeferredRegister.create(
            TypeRegistries.PROJECTILE_BEHAVIOR_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<ProjectileBehavior>, CodecType<ProjectileBehavior>> HOMING = TYPES.register(
            "homing",
            () -> new CodecType<>(HomingProjectileBehavior.CODEC)
    );

    private AscensionProjectileBehaviorTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
