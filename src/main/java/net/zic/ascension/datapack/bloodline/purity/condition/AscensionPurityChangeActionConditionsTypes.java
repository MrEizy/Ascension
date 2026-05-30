package net.zic.ascension.datapack.bloodline.purity.condition;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionConditionType;

public class AscensionPurityChangeActionConditionsTypes {
    public static final DeferredRegister<PurityChangeActionConditionType> PURITY_CHANGE_ACTION_CONDITION_TYPES =
            DeferredRegister.create(TypeRegistries.PURITY_CHANGE_ACTION_CONDITION_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<PurityChangeActionConditionType,PurityChangeActionConditionType> ON_PURITY_CONDITION = PURITY_CHANGE_ACTION_CONDITION_TYPES.register(
            "on_purity_in_range",
            OnPurityInRangeConditionType::new
    );

    public static void register(IEventBus eventBus){

        PURITY_CHANGE_ACTION_CONDITION_TYPES.register(eventBus);
    }
}
