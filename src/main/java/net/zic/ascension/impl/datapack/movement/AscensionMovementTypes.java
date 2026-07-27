package net.zic.ascension.impl.datapack.movement;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.movement.MovementType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.core.movement.AnchorMovement;
import net.zic.ascension.impl.core.movement.DirectionalMovement;
import net.zic.ascension.impl.core.movement.PositionMovement;

public final class AscensionMovementTypes {
    public static final DeferredRegister<MovementType> TYPES = DeferredRegister.create(
            TypeRegistries.MOVEMENT_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<MovementType, MovementType> DIRECTIONAL = TYPES.register(
            "directional",
            () -> new MovementType(DirectionalMovement.CODEC)
    );
    public static final DeferredHolder<MovementType, MovementType> POSITION = TYPES.register(
            "position",
            () -> new MovementType(PositionMovement.CODEC)
    );
    public static final DeferredHolder<MovementType, MovementType> ANCHOR = TYPES.register(
            "anchor",
            () -> new MovementType(AnchorMovement.CODEC)
    );

    private AscensionMovementTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
