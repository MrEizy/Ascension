package net.zic.ascension.api.core;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeActionCondition;
import net.zic.ascension.api.core.data_source.DataSource;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeAction;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionConditionType;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionType;
import net.zic.ascension.api.datapack.data_source.DataSourceType;
import net.zic.ascension.api.datapack.path.PathType;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.ascension.api.datapack.technique.realm_change.RealmChangeActionType;
import net.zic.ascension.api.datapack.technique.realm_change.RealmChangeActionConditionType;
import net.zic.zenithlib.registry.RegistryHelper;
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class CoreRegistries {

    public static final RegistryHelper.DataPackRegistry<Physique> PHYSIQUE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "physiques",
            ()->PhysiqueType.PHYSIQUE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Bloodline> BLOODLINE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "bloodlines",
            ()-> BloodlineType.BLOODLINE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Technique> TECHNIQUE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "technique",
            ()-> TechniqueType.TECHNIQUE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Path> PATH_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "paths",
            ()-> PathType.PATH_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Skill> SKILL_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "skills",
            ()-> SkillType.SKILL_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<DataSource> DATA_SOURCE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "data_sources",
            ()-> DataSourceType.DATA_SOURCE_CODEC
    );

    public static final RegistryHelper.DataPackRegistry<RealmChangeActionCondition> REALM_CHANGE_ACTION_CONDITION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "realm_change_action_conditions",
            ()-> RealmChangeActionConditionType.REALM_CHANGE_ACTION_CONDITION_CODEC
    );

    public static final RegistryHelper.DataPackRegistry<RealmChangeAction> REALM_CHANGE_ACTION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "realm_change_actions",
            ()-> RealmChangeActionType.REALM_CHANGE_ACTION_CODEC
    );

    public static final RegistryHelper.DataPackRegistry<PurityChangeAction> PURITY_CHANGE_ACTION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "purity_change_actions",
            ()-> PurityChangeActionType.PURITY_CHANGE_ACTION_CODEC
    );

    public static final RegistryHelper.DataPackRegistry<PurityChangeActionCondition> PURITY_CHANGE_ACTION_CONDITION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "purity_change_action_conditions",
            ()-> PurityChangeActionConditionType.PURITY_CHANGE_ACTION_CONDITION_CODEC
    );

    public static <T> T safeAccess(RegistryHelper.DataPackRegistry<T> registry, Identifier id, RegistryAccess access){
        try {
            return registry.get(access).getValue(id);
        }catch (Throwable throwable) {
            return null;
        }
    }

    //TODO think about if tribulations need their own registry or not. aka do we want them to make them in place
    //TODO like realm change, or do we want them to be able to define them elsewhere then reuse them using the registry (prob 2)


    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        AscensionCraft.LOGGER.info("creating core registries");
        event.dataPackRegistry(
                PHYSIQUE_REGISTRY.key(),
                PHYSIQUE_REGISTRY.codec().get(),
                PHYSIQUE_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                TECHNIQUE_REGISTRY.key(),
                TECHNIQUE_REGISTRY.codec().get(),
                TECHNIQUE_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                BLOODLINE_REGISTRY.key(),
                BLOODLINE_REGISTRY.codec().get(),
                BLOODLINE_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                PATH_REGISTRY.key(),
                PATH_REGISTRY.codec().get(),
                PATH_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                SKILL_REGISTRY.key(),
                SKILL_REGISTRY.codec().get(),
                SKILL_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                DATA_SOURCE_REGISTRY.key(),
                DATA_SOURCE_REGISTRY.codec().get(),
                DATA_SOURCE_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                REALM_CHANGE_ACTION_REGISTRY.key(),
                REALM_CHANGE_ACTION_REGISTRY.codec().get(),
                REALM_CHANGE_ACTION_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                REALM_CHANGE_ACTION_CONDITION_REGISTRY.key(),
                REALM_CHANGE_ACTION_CONDITION_REGISTRY.codec().get(),
                REALM_CHANGE_ACTION_CONDITION_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                PURITY_CHANGE_ACTION_REGISTRY.key(),
                PURITY_CHANGE_ACTION_REGISTRY.codec().get(),
                PURITY_CHANGE_ACTION_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                PURITY_CHANGE_ACTION_CONDITION_REGISTRY.key(),
                PURITY_CHANGE_ACTION_CONDITION_REGISTRY.codec().get(),
                PURITY_CHANGE_ACTION_CONDITION_REGISTRY.codec().get()
        );

    }
}
