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
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeAction;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;

import net.zic.ascension.api.datapack.data_source.DataSourceType;
import net.zic.ascension.api.datapack.path.PathType;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.datapack.technique.TechniqueType;

import net.zic.ascension.api.datapack.tribulation.TribulationType;
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
            "techniques",
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

    public static final RegistryHelper.DataPackRegistry<ProgressAction> PROGRESS_ACTION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "progress_actions",
            ()-> ProgressActionType.PROGRESS_ACTION_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<ProgressActionCondition> PROGRESS_ACTION_CONDITION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "progress_action_conditions",
            ()-> ProgressActionConditionType.PROGRESS_ACTION_CONDITION_CODEC
    );

    public static final RegistryHelper.DataPackRegistry<TribulationDefinition> TRIBULATION_DEFINITION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "tribulation_definitions",
            ()-> TribulationType.TRIBULATION_CODEC
    );

    public static <T> T safeAccess(RegistryHelper.DataPackRegistry<T> registry, Identifier id, RegistryAccess access){
        return registry.get(access).containsKey(id) ? registry.get(access).getValue(id) : null;
    }



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
                PROGRESS_ACTION_REGISTRY.key(),
                PROGRESS_ACTION_REGISTRY.codec().get(),
                PROGRESS_ACTION_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                PROGRESS_ACTION_CONDITION_REGISTRY.key(),
                PROGRESS_ACTION_CONDITION_REGISTRY.codec().get(),
                PROGRESS_ACTION_CONDITION_REGISTRY.codec().get()
        );

        event.dataPackRegistry(
                TRIBULATION_DEFINITION_REGISTRY.key(),
                TRIBULATION_DEFINITION_REGISTRY.codec().get(),
                TRIBULATION_DEFINITION_REGISTRY.codec().get()
        );
    }
}
