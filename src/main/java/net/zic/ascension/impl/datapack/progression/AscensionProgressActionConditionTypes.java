package net.zic.ascension.impl.datapack.progression;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;

import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.impl.core.technique.realm_change.condition.EveryMajorRealmCondition;
import net.zic.ascension.impl.core.technique.realm_change.condition.EveryMinorRealmCondition;
import net.zic.ascension.impl.core.technique.realm_change.condition.EveryRealmCondition;
import net.zic.ascension.impl.datapack.bloodline.purity.condition.OnPurityInRangeConditionType;
import net.zic.ascension.impl.datapack.progression.condition.UnitConditionType;
import net.zic.ascension.impl.datapack.technique.realm_change.condition.EveryMajorRealmInConditionType;
import net.zic.ascension.impl.datapack.technique.realm_change.condition.EveryMinorRealmInConditionType;
import net.zic.ascension.impl.datapack.technique.realm_change.condition.EveryRealmInConditionType;

public class AscensionProgressActionConditionTypes {
    public static final DeferredRegister<ProgressActionConditionType> PROGRESS_ACTION_CONDITION_TYPES =
            DeferredRegister.create(TypeRegistries.PROGRESS_ACTION_CONDITION_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    //──Purity────────────────────────────────────────────────────────

    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> ON_PURITY_CONDITION = PROGRESS_ACTION_CONDITION_TYPES.register(
            "on_purity_in_range",
            OnPurityInRangeConditionType::new
    );

    //──Realm Change────────────────────────────────────────────────────────
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_REALM = PROGRESS_ACTION_CONDITION_TYPES.register(
            "every_realm",
            ()->new UnitConditionType<>(EveryRealmCondition::new)
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MINOR_REALM = PROGRESS_ACTION_CONDITION_TYPES.register(
            "every_minor_realm",
            ()->new UnitConditionType<>(EveryMinorRealmCondition::new)
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MAJOR_REALM = PROGRESS_ACTION_CONDITION_TYPES.register(
            "every_major_realm",
            ()->new UnitConditionType<>(EveryMajorRealmCondition::new)
    );

    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MAJOR_REALM_IN = PROGRESS_ACTION_CONDITION_TYPES.register(
            "major_realms_in",
            EveryMajorRealmInConditionType::new
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MINOR_REALM_IN = PROGRESS_ACTION_CONDITION_TYPES.register(
            "minor_realms_in",
            EveryMinorRealmInConditionType::new
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_REALM_IN = PROGRESS_ACTION_CONDITION_TYPES.register(
            "realms_in",
            EveryRealmInConditionType::new
    );
    public static void register(IEventBus eventBus){

        PROGRESS_ACTION_CONDITION_TYPES.register(eventBus);
    }
}
