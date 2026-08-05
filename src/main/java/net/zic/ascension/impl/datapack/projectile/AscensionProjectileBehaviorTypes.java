package net.zic.ascension.impl.datapack.projectile;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.projectile.ProjectileBehavior;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.runtime.projectile.ProjectileBehaviors;

public final class AscensionProjectileBehaviorTypes {
    private static final ExtensionTypeRegistry<ProjectileBehavior> TYPES = new ExtensionTypeRegistry<>(
            TypeRegistries.PROJECTILE_BEHAVIOR_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<ProjectileBehavior>, CodecType<ProjectileBehavior>> HOMING =
            TYPES.add("homing", ProjectileBehaviors.Homing.CODEC);

    private AscensionProjectileBehaviorTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
