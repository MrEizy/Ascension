package net.zic.ascension.handler;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.resource.ResourceDefinition;
import net.zic.ascension.api.ascension.core.resource.ResourceModifiers;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.event.bloodline.BloodlineEvent;
import net.zic.ascension.api.ascension.event.path.PathAddedEvent;
import net.zic.ascension.api.ascension.event.path.PathRemovedEvent;
import net.zic.ascension.api.ascension.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.api.ascension.event.resource.ResourceTransactionEvent;
import net.zic.ascension.api.ascension.event.skill.SkillEvent;
import net.zic.ascension.api.ascension.event.technique.TechniqueEvent;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.effect.FrozenStateService;
import net.zic.ascension.impl.core.effect.SkillEffectManager;
import net.zic.ascension.impl.core.effect.SkillEffectModules;
import net.zic.ascension.impl.core.skill.body.BodyCultivationSkillService;
import net.zic.ascension.impl.core.skill.passive.PassiveCombatService;
import net.zic.ascension.impl.core.skill.passive.PassiveModifiers;
import net.zic.ascension.impl.core.skill.passive.PassiveSkillService;
import net.zic.ascension.impl.core.skill.passive.PassiveTriggerService;
import net.zic.ascension.impl.core.technique.TechniqueSkillService;
import net.zic.ascension.impl.resource.AscensionResourceSources;

import java.util.Map;
import java.util.Set;

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
    public static void onResolvedDamage(RPGEngineEntityDamagedEvent.Post event) {
        if (event.getDamage() <= 0.0D) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            BodyCultivationSkillService.stimulate(player, AscensionResourceSources.DAMAGE, event.getDamage());
        }
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity entity ? entity : null;
        Map<Identifier, Double> variables = Map.of(PassiveTriggerService.DAMAGE, event.getDamage());
        if (attacker != null && attacker != event.getEntity()) {
            PassiveCombatService.applyLifesteal(event);
            PassiveTriggerService.trigger(attacker, PassiveTrigger.Event.DAMAGE_DEALT, event.getEntity(), variables);
        }
        PassiveTriggerService.trigger(event.getEntity(), PassiveTrigger.Event.DAMAGE_TAKEN, attacker, variables);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity killer = event.getEntity().getKillCredit();
        if (killer == null && event.getSource().getEntity() instanceof LivingEntity source) {
            killer = source;
        }
        if (killer != null && killer != event.getEntity()) {
            PassiveTriggerService.trigger(killer, PassiveTrigger.Event.KILL, event.getEntity(), Map.of());
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
        if (entity instanceof ServerPlayer player) {
            BodyCultivationSkillService.tick(player);
        }
        if (entity.hasData(CoreAttachments.DATAPACK_RESOURCES)) {
            for (var entry : CoreRegistries.RESOURCE_DEFINITION_REGISTRY.get(entity.registryAccess()).entrySet()) {
                ResourceDefinition definition = entry.getValue();
                if (definition.regenerationInterval() <= 0
                        || entity.level().getGameTime() % definition.regenerationInterval() != 0L) {
                    continue;
                }
                double amount = definition.regeneration(entity, entry.getKey().identifier());
                if (amount > 0.0D) {
                    ResourceTransactionService.generate(
                            entity,
                            entry.getKey().identifier(),
                            new ResourceSourceIdentity.Simple(AscensionCraft.prefix("resource_regeneration"), Set.of()),
                            amount
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onResourceChanged(ResourceTransactionEvent.Post event) {
        ResourceTransactionService.Result result = event.getResult();
        if (result == null || result.context() == null || !result.succeeded() || result.appliedAmount() == 0.0D) {
            return;
        }
        var request = result.context().request();
        if (request.hasFlag(ResourceTransactionRequest.Flag.SIMULATE)) {
            return;
        }
        Map<Identifier, Double> variables = Map.of(
                PassiveTriggerService.RESOURCE_REQUESTED, result.requestedAmount(),
                PassiveTriggerService.RESOURCE_APPLIED, result.appliedAmount(),
                PassiveTriggerService.RESOURCE_BEFORE, result.amountBefore(),
                PassiveTriggerService.RESOURCE_AFTER, result.amountAfter(),
                PassiveTriggerService.RESOURCE_MAXIMUM, result.context().resourceType().getMaximum(request.entity())
        );
        PassiveTriggerService.trigger(request.entity(), PassiveTrigger.Event.RESOURCE_CHANGED, request.target(), variables);
    }

    @SubscribeEvent
    public static void onSkillAdded(SkillEvent.Added.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onSkillRemoved(SkillEvent.Removed.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onSkillProgressionChanged(SkillEvent.ProgressionChanged event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onTechniqueAdded(TechniqueEvent.Added.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onTechniqueRemoved(TechniqueEvent.Removed.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onPathAdded(PathAddedEvent.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onPathRemoved(PathRemovedEvent.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onBloodlineAdded(BloodlineEvent.Added.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onBloodlineRemoved(BloodlineEvent.Removed.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
    }

    @SubscribeEvent
    public static void onPhysiqueChanged(PhysiqueChangedEvent.Post event) {
        TechniqueSkillService.reconcileAll(event.getSource());
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
        for (PassiveSkillService.Entry<PassiveModifiers.Resources> entry
                : PassiveSkillService.modifiers(source, source.getRegistryAccess(), PassiveModifiers.Resources.class)) {
            for (ResourceModifiers.Definition definition : entry.modifier().modifiers()) {
                if (definition.matches(event.getContext())) {
                    event.getCollector().add(definition.resolve(event.getContext(), source, entry.skillId()));
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
