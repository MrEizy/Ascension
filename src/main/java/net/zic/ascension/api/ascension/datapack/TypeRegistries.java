package net.zic.ascension.api.ascension.datapack;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.ascension.datapack.requirement.RequirementType;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.api.ascension.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.core.projectile.ProjectileBehavior;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.zenithlib.registry.RegistryHelper;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class TypeRegistries {
    public static final Registry<PhysiqueType> PHYSIQUE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "physique_type");
    public static final Registry<BloodlineType> BLOODLINE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "bloodline_type");
    public static final Registry<SkillType> SKILL_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "skill_type");
    public static final Registry<TechniqueType> TECHNIQUE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "technique_type");
    public static final Registry<PathType> PATH_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "path_type");
    public static final Registry<TribulationType> TRIBULATION_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "tribulation_type");
    public static final Registry<ProgressActionType> PROGRESS_ACTION_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "progression_action_type");
    public static final Registry<ProgressActionConditionType> PROGRESS_ACTION_CONDITION_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "progression_action_condition_type");
    public static final Registry<RequirementType> REQUIREMENT_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "requirement_type");
    public static final Registry<CodecType<ScaledValue.Source>> SCALED_VALUE_SOURCE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "scaled_value_source_type");
    public static final Registry<CodecType<SkillEffectModule>> SKILL_EFFECT_MODULE_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "skill_effect_module_type");
    public static final Registry<CodecType<SkillAction>> SKILL_ACTION_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "skill_action_type");
    public static final Registry<CodecType<TargetingDefinition>> TARGETING_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "targeting_type");
    public static final Registry<CodecType<ProjectileBehavior>> PROJECTILE_BEHAVIOR_TYPE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "projectile_behavior_type");


    private TypeRegistries() {
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        AscensionCraft.LOGGER.info("Creating type registries");
        event.register(PHYSIQUE_TYPE_REGISTRY);
        event.register(BLOODLINE_TYPE_REGISTRY);
        event.register(SKILL_TYPE_REGISTRY);
        event.register(TECHNIQUE_TYPE_REGISTRY);
        event.register(PATH_TYPE_REGISTRY);
        event.register(TRIBULATION_TYPE_REGISTRY);
        event.register(PROGRESS_ACTION_TYPE_REGISTRY);
        event.register(PROGRESS_ACTION_CONDITION_TYPE_REGISTRY);
        event.register(REQUIREMENT_TYPE_REGISTRY);
        event.register(SCALED_VALUE_SOURCE_TYPE_REGISTRY);
        event.register(SKILL_EFFECT_MODULE_TYPE_REGISTRY);
        event.register(SKILL_ACTION_TYPE_REGISTRY);
        event.register(TARGETING_TYPE_REGISTRY);
        event.register(PROJECTILE_BEHAVIOR_TYPE_REGISTRY);
        AscensionCraft.LOGGER.info("Finished loading type registries");
    }
}
