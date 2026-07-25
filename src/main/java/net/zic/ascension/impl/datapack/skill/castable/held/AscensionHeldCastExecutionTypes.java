package net.zic.ascension.impl.datapack.skill.castable.held;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.held.execution.ProjectileReleaseExecution;
import net.zic.ascension.impl.core.skill.castable.held.execution.RadialReleaseExecution;
import net.zic.ascension.impl.core.skill.castable.held.execution.SelfReleaseExecution;

public final class AscensionHeldCastExecutionTypes {
    public static final DeferredRegister<HeldCastExecutionType> TYPES = DeferredRegister.create(
            TypeRegistries.HELD_CAST_EXECUTION_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<HeldCastExecutionType, HeldCastExecutionType> SELF_RELEASE = TYPES.register(
            "self_release",
            () -> new HeldCastExecutionType(SelfReleaseExecution.CODEC)
    );
    public static final DeferredHolder<HeldCastExecutionType, HeldCastExecutionType> RADIAL_RELEASE = TYPES.register(
            "radial_release",
            () -> new HeldCastExecutionType(RadialReleaseExecution.CODEC)
    );
    public static final DeferredHolder<HeldCastExecutionType, HeldCastExecutionType> PROJECTILE_RELEASE = TYPES.register(
            "projectile_release",
            () -> new HeldCastExecutionType(ProjectileReleaseExecution.CODEC)
    );

    private AscensionHeldCastExecutionTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
