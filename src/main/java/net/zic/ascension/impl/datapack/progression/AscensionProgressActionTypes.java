package net.zic.ascension.impl.datapack.progression;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.datapack.progression.action.GiveBaseStatsActionType;
import net.zic.ascension.impl.datapack.progression.action.GivePathBonusesActionType;
import net.zic.ascension.impl.datapack.progression.action.GrantSkillsActionType;
import net.zic.ascension.impl.datapack.progression.action.RemoveSkillsActionType;

public final class AscensionProgressActionTypes {
    public static final DeferredRegister<ProgressActionType> PROGRESS_ACTION_TYPES =
            DeferredRegister.create(TypeRegistries.PROGRESS_ACTION_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<ProgressActionType, ProgressActionType> GIVE_BASE_STATS_TYPE = PROGRESS_ACTION_TYPES.register(
            "give_base_stats",
            GiveBaseStatsActionType::new
    );

    public static final DeferredHolder<ProgressActionType, ProgressActionType> GIVE_PATH_BONUSES_TYPE = PROGRESS_ACTION_TYPES.register(
            "give_path_bonuses",
            GivePathBonusesActionType::new
    );

    public static final DeferredHolder<ProgressActionType, ProgressActionType> GRANT_SKILLS_TYPE = PROGRESS_ACTION_TYPES.register(
            "grant_skills",
            GrantSkillsActionType::new
    );

    public static final DeferredHolder<ProgressActionType, ProgressActionType> REMOVE_SKILLS_TYPE = PROGRESS_ACTION_TYPES.register(
            "remove_skills",
            RemoveSkillsActionType::new
    );

    private AscensionProgressActionTypes() {
    }

    public static void register(IEventBus eventBus) {
        PROGRESS_ACTION_TYPES.register(eventBus);
    }
}
