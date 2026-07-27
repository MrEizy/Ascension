package net.zic.ascension.impl.datapack.projectile;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.projectile.ProjectileBehaviorType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.core.projectile.HomingProjectileBehavior;

public final class AscensionProjectileBehaviorTypes {
    public static final DeferredRegister<ProjectileBehaviorType> TYPES = DeferredRegister.create(
            TypeRegistries.PROJECTILE_BEHAVIOR_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<ProjectileBehaviorType, ProjectileBehaviorType> HOMING = TYPES.register(
            "homing",
            () -> new ProjectileBehaviorType(HomingProjectileBehavior.CODEC)
    );

    private AscensionProjectileBehaviorTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
