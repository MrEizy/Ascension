package net.zic.ascension.datapack.bloodline.purity.action;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionType;
import net.zic.ascension.core.bloodline.purity.action.GiveSkillsAction;

public class AscensionPurityChangeActionTypes {
    public static final DeferredRegister<PurityChangeActionType> PURITY_CHANGE_ACTION_TYPES =
            DeferredRegister.create(TypeRegistries.PURITY_CHANGE_ACTION_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<PurityChangeActionType,PurityChangeActionType> GIVE_BASE_STATS_TYPE = PURITY_CHANGE_ACTION_TYPES.register(
            "give_base_stats",
            GiveBaseStatsActionType::new
    );
    public static final DeferredHolder<PurityChangeActionType,PurityChangeActionType> GIVE_SKILLS_TYPE = PURITY_CHANGE_ACTION_TYPES.register(
            "give_skills",
            GiveSkillsActionType::new
    );
    public static void register(IEventBus eventBus){

        PURITY_CHANGE_ACTION_TYPES.register(eventBus);
    }
}
