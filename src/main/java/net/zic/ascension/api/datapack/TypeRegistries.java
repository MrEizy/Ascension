package net.zic.ascension.api.datapack;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.datapack.data_source.DataSourceType;
import net.zic.ascension.api.datapack.path.PathType;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.ascension.api.datapack.technique.realm_change.ListenerActionType;
import net.zic.ascension.api.datapack.technique.realm_change.ListenerConditionType;
import net.zic.ascension.api.datapack.tribulation.TribulationType;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.registry.RegistryHelper;

/**
 * All the type registries used for decoding datapacks and sending datapack registries over the network
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class TypeRegistries {

    public static final Registry<PhysiqueType> PHYSIQUE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"physique_types");

    public static final Registry<BloodlineType> BLOODLINE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"bloodline_types");

    public static final Registry<SkillType> SKILL_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"skill_types");

    public static final Registry<TechniqueType> TECHNIQUE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"technique_types");

    public static final Registry<PathType> PATH_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"path_types");

    public static final Registry<DataSourceType> DATA_SOURCE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"data_source_types");

    public static final Registry<TribulationType> TRIBULATION_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"tribulation_type_registry");

    public static final Registry<ListenerActionType> LISTENER_ACTION_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"listener_action_types");

    public static final Registry<ListenerConditionType> LISTENER_CONDITIONN_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"listener_condition_types");


    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event){
        AscensionCraft.LOGGER.info("Creating type registries");
        event.register(PHYSIQUE_TYPE_REGISTRY);
        event.register(BLOODLINE_TYPE_REGISTRY);
        event.register(SKILL_TYPE_REGISTRY);
        event.register(TECHNIQUE_TYPE_REGISTRY);
        event.register(PATH_TYPE_REGISTRY);
        event.register(DATA_SOURCE_TYPE_REGISTRY);
        event.register(TRIBULATION_TYPE_REGISTRY);
        event.register(LISTENER_ACTION_TYPE_REGISTRY);
        event.register(LISTENER_CONDITIONN_TYPE_REGISTRY);
        AscensionCraft.LOGGER.info("Finished loading type registries");
    }
}
