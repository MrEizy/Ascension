package net.zic.ascension.datapack.progression;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;

import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.datapack.bloodline.SimpleBloodlineType;
import net.zic.ascension.datapack.bloodline.purity.condition.OnPurityInRangeConditionType;

public class AscensionProgressActionConditionTypes {
    public static final DeferredRegister<ProgressActionConditionType> PROGRESS_ACTION_CONDITION_TYPES =
            DeferredRegister.create(TypeRegistries.PROGRESS_ACTION_CONDITION_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<ProgressActionConditionType,OnPurityInRangeConditionType> ON_PURITY_CONDITION = PROGRESS_ACTION_CONDITION_TYPES.register(
            "on_purity_in_range",
            OnPurityInRangeConditionType::new
    );


    public static void register(IEventBus eventBus){

        PROGRESS_ACTION_CONDITION_TYPES.register(eventBus);
    }
}
