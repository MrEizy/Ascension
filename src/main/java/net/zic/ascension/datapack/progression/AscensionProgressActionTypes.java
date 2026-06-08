package net.zic.ascension.datapack.progression;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.datapack.progression.action.GiveBaseStatsActionType;
import net.zic.ascension.datapack.progression.action.GiveSkillsActionType;

public class AscensionProgressActionTypes {
    public static final DeferredRegister<ProgressActionType> PROGRESS_ACTION_TYPES =
            DeferredRegister.create(TypeRegistries.PROGRESS_ACTION_TYPE_REGISTRY, AscensionCraft.MOD_ID);



    public static final DeferredHolder<ProgressActionType,ProgressActionType> GIVE_BASE_STATS_TYPE = PROGRESS_ACTION_TYPES.register(
            "give_base_stats",
            GiveBaseStatsActionType::new
    );
    public static final DeferredHolder<ProgressActionType,ProgressActionType> GIVE_SKILLS_TYPE = PROGRESS_ACTION_TYPES.register(
            "give_skills",
            GiveSkillsActionType::new
    );

    public static void register(IEventBus eventBus){

        PROGRESS_ACTION_TYPES.register(eventBus);
    }
}
