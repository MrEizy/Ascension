package net.zic.ascension.impl.datapack.tribulation;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.tribulation.TribulationResolutionAction;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationResolutionActionType;
import net.zic.ascension.impl.core.tribulation.action.EndBreakthroughResolutionAction;
import net.zic.ascension.impl.core.tribulation.action.RealmChangeResolutionAction;

import java.util.function.Supplier;

public class AscensionTribulationResolutionActionTypes {
    public static final DeferredRegister<TribulationResolutionActionType> RESOLUTION_ACTION_TYPES =
            DeferredRegister.create(TypeRegistries.TRIBULATION_RESOLUTION_ACTION_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<TribulationResolutionActionType, TribulationResolutionActionType> REALM_CHANGE =
            register("realm_change", () -> RealmChangeResolutionAction.CODEC);
    public static final DeferredHolder<TribulationResolutionActionType, TribulationResolutionActionType> END_BREAKTHROUGH =
            register("end_breakthrough", () -> EndBreakthroughResolutionAction.CODEC);

    private static DeferredHolder<TribulationResolutionActionType, TribulationResolutionActionType> register(
            String name,
            Supplier<MapCodec<? extends TribulationResolutionAction>> codec
    ) {
        return RESOLUTION_ACTION_TYPES.register(name, () -> new TribulationResolutionActionType() {
            @Override
            public MapCodec<? extends TribulationResolutionAction> codec() {
                return codec.get();
            }
        });
    }

    public static void register(IEventBus eventBus) {
        RESOLUTION_ACTION_TYPES.register(eventBus);
    }
}
