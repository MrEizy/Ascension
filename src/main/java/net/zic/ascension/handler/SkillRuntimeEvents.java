package net.zic.ascension.handler;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.resource.ResourceModifiers;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.event.resource.ResourceTransactionEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.effect.FrozenStateService;
import net.zic.ascension.impl.core.effect.SkillEffectManager;
import net.zic.ascension.impl.core.effect.SkillEffectModules;
import net.zic.ascension.impl.core.skill.passive.ResourceModifierPassiveSkill;
import net.zic.ascension.impl.core.skill.passive.ResourceModifierPassiveSkill.Data;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class SkillRuntimeEvents {
    private SkillRuntimeEvents() {
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getNewDamage() > 0.0F) {
            player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER).recordDamage(event.getNewDamage());
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }
        if (FrozenStateService.isActive(entity)) {
            FrozenStateService.tick(entity);
        }
        if (SkillEffectManager.hasActiveEffects(entity)) {
            SkillEffectManager.tick(entity);
        }
    }

    @SubscribeEvent
    public static void collectResourceModifiers(ResourceTransactionEvent.Modify event) {
        collectPassiveModifiers(event);
        collectEffectModifiers(event);
    }

    private static void collectPassiveModifiers(ResourceTransactionEvent.Modify event) {
        AscensionEntityDataProvider provider = event.getContext().request().entity().getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
        );
        if (provider == null) {
            return;
        }
        AscensionEntityData entityData = provider.getData();
        if (entityData == null || entityData.getSource() == null) {
            return;
        }
        OriginSource source = entityData.getSource();
        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, source.getRegistryAccess());
            SkillData skillData = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof ResourceModifierPassiveSkill passive)
                    || !(skillData instanceof Data passiveData)
                    || passive instanceof net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill && !passiveData.isEnabled()) {
                continue;
            }
            int level = SkillProgressionResolver.resolve(source, skillId).effectiveProgression();
            for (ResourceModifiers.Definition definition : passive.getModifiers(level)) {
                if (definition.matches(event.getContext())) {
                    event.getCollector().add(definition.resolve(event.getContext(), source, skillId));
                }
            }
        }
    }

    private static void collectEffectModifiers(ResourceTransactionEvent.Modify event) {
        SkillEffectManager.Container container = event.getContext().request().entity().getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        if (container.isEmpty()) {
            return;
        }
        for (SkillEffectManager.Instance active : container.instances()) {
            SkillEffectDefinition definition = SkillDefinitions.resolveStored(
                    SkillEffectDefinition.class,
                    active.sourceSkill(),
                    active.definition(),
                    CoreRegistries.SKILL_EFFECT_REGISTRY,
                    event.getContext().request().entity().registryAccess()
            );
            if (definition == null) {
                continue;
            }
            for (var module : definition.modules()) {
                if (module instanceof SkillEffectModules.ResourceModifierModule resourceModifier
                        && resourceModifier.matches(event.getContext())) {
                    event.getCollector().add(resourceModifier.resolve(event.getContext(), active));
                }
            }
        }
    }
}
