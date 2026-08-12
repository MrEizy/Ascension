package net.zic.ascension.impl.datapack.progression;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.impl.core.path.foundation.foundation_change.condition.EveryFoundationRealmCondition;
import net.zic.ascension.impl.core.technique.realm_change.condition.EveryMajorRealmCondition;
import net.zic.ascension.impl.core.technique.realm_change.condition.EveryMinorRealmCondition;
import net.zic.ascension.impl.core.technique.realm_change.condition.EveryRealmCondition;
import net.zic.ascension.impl.datapack.bloodline.purity.condition.OnPurityInRangeConditionType;
import net.zic.ascension.impl.datapack.progression.condition.UnitConditionType;
import net.zic.ascension.impl.core.technique.realm_change.condition.RealmChangeConditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
            ()->new UnitConditionType<>(RealmChangeConditions.EveryRealm::new)
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MINOR_REALM = PROGRESS_ACTION_CONDITION_TYPES.register(
            "every_minor_realm",
            ()->new UnitConditionType<>(RealmChangeConditions.EveryMinorRealm::new)
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MAJOR_REALM = PROGRESS_ACTION_CONDITION_TYPES.register(
            "every_major_realm",
            ()->new UnitConditionType<>(RealmChangeConditions.EveryMajorRealm::new)
    );

    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MAJOR_REALM_IN = PROGRESS_ACTION_CONDITION_TYPES.register(
            "major_realms_in",
            () -> new ProgressActionConditionType() {
                @Override
                public com.mojang.serialization.MapCodec<? extends net.zic.ascension.api.ascension.core.progression.ProgressActionCondition> codec() {
                    return RecordCodecBuilder.<RealmChangeConditions.MajorRealmsIn>mapCodec(instance -> instance.group(
                            Codec.INT.listOf().fieldOf("realms").forGetter(RealmChangeConditions.MajorRealmsIn::majorRealms)
                    ).apply(instance, RealmChangeConditions.MajorRealmsIn::new));
                }
            }
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_MINOR_REALM_IN = PROGRESS_ACTION_CONDITION_TYPES.register(
            "minor_realms_in",
            () -> new ProgressActionConditionType() {
                @Override
                public com.mojang.serialization.MapCodec<? extends net.zic.ascension.api.ascension.core.progression.ProgressActionCondition> codec() {
                    return RecordCodecBuilder.<RealmChangeConditions.MinorRealmsIn>mapCodec(instance -> instance.group(
                            Codec.INT.listOf().fieldOf("realms").forGetter(RealmChangeConditions.MinorRealmsIn::minorRealms)
                    ).apply(instance, RealmChangeConditions.MinorRealmsIn::new));
                }
            }
    );
    public static final DeferredHolder<ProgressActionConditionType,ProgressActionConditionType> EVERY_REALM_IN = PROGRESS_ACTION_CONDITION_TYPES.register(
            "realms_in",
            () -> new ProgressActionConditionType() {
                @Override
                public com.mojang.serialization.MapCodec<? extends net.zic.ascension.api.ascension.core.progression.ProgressActionCondition> codec() {
                    return RecordCodecBuilder.<RealmChangeConditions.RealmsIn>mapCodec(instance -> instance.group(
                            Codec.unboundedMap(Codec.INT, Codec.INT.listOf()).fieldOf("realms").forGetter(RealmChangeConditions.RealmsIn::realms)
                    ).apply(instance, RealmChangeConditions.RealmsIn::new));
                }
            }
    );
    public static void register(IEventBus eventBus){

        PROGRESS_ACTION_CONDITION_TYPES.register(eventBus);
    }
}
