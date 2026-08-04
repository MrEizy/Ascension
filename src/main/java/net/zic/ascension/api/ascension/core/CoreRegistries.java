package net.zic.ascension.api.ascension.core;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.control.StaggerDefinition;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.api.ascension.core.runtime.AnchorNetworkDefinition;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.runtime.OwnerBoundConstructDefinition;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.runtime.AreaFieldDefinition;
import net.zic.ascension.api.ascension.core.projectile.VirtualProjectileDefinition;
import net.zic.zenithlib.registry.RegistryHelper;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class CoreRegistries {
    public static final RegistryHelper.DataPackRegistry<Physique> PHYSIQUE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "physiques", () -> PhysiqueType.PHYSIQUE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Bloodline> BLOODLINE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "bloodlines", () -> BloodlineType.BLOODLINE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Technique> TECHNIQUE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "techniques", () -> TechniqueType.TECHNIQUE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Path> PATH_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "paths", () -> PathType.PATH_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Skill> SKILL_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skills", () -> SkillType.SKILL_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<SkillEffectDefinition> SKILL_EFFECT_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skill_system/effects", () -> SkillEffectDefinition.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<VirtualProjectileDefinition> VIRTUAL_PROJECTILE_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skill_system/runtime/projectiles", () -> VirtualProjectileDefinition.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<AreaFieldDefinition> AREA_FIELD_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skill_system/runtime/fields", () -> AreaFieldDefinition.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<AnchorNetworkDefinition> ANCHOR_NETWORK_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skill_system/runtime/anchor_networks", () -> AnchorNetworkDefinition.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<OwnerBoundConstructDefinition> CONSTRUCT_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skill_system/runtime/constructs", () -> OwnerBoundConstructDefinition.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<BarrierDefinition> BARRIER_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skill_system/runtime/barriers", () -> BarrierDefinition.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<StaggerDefinition> STAGGER_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "skill_system/stagger", () -> StaggerDefinition.CODEC
    );
    public static final RegistryHelper.DataPackRegistry<ProgressAction> PROGRESS_ACTION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "progress_actions", () -> ProgressActionType.PROGRESS_ACTION_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<ProgressActionCondition> PROGRESS_ACTION_CONDITION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "progress_action_conditions", () -> ProgressActionConditionType.PROGRESS_ACTION_CONDITION_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<TribulationDefinition> TRIBULATION_DEFINITION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID, "tribulation_definitions", () -> TribulationType.TRIBULATION_CODEC
    );

    private CoreRegistries() {
    }

    public static <T> T safeAccess(RegistryHelper.DataPackRegistry<T> registry, Identifier id, RegistryAccess access) {
        if (registry == null || id == null || access == null) {
            return null;
        }
        return registry.get(access).containsKey(id) ? registry.get(access).getValue(id) : null;
    }

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        AscensionCraft.LOGGER.info("Creating core registries");
        register(event, PHYSIQUE_REGISTRY);
        register(event, TECHNIQUE_REGISTRY);
        register(event, BLOODLINE_REGISTRY);
        register(event, PATH_REGISTRY);
        register(event, SKILL_REGISTRY);
        register(event, SKILL_EFFECT_REGISTRY);
        register(event, VIRTUAL_PROJECTILE_REGISTRY);
        register(event, AREA_FIELD_REGISTRY);
        register(event, ANCHOR_NETWORK_REGISTRY);
        register(event, CONSTRUCT_REGISTRY);
        register(event, BARRIER_REGISTRY);
        register(event, STAGGER_REGISTRY);
        register(event, PROGRESS_ACTION_REGISTRY);
        register(event, PROGRESS_ACTION_CONDITION_REGISTRY);
        register(event, TRIBULATION_DEFINITION_REGISTRY);
    }

    private static <T> void register(DataPackRegistryEvent.NewRegistry event, RegistryHelper.DataPackRegistry<T> registry) {
        event.dataPackRegistry(registry.key(), registry.codec().get(), registry.codec().get());
    }
}
