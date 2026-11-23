package net.zic.ascension.impl.core.skill.passive;

import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.runtime.weapon.WeaponVfxUtils;
import net.zic.ascension.impl.core.skill.castable.SkillActionRuntime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class PassiveTriggerService {
    public static final Identifier DAMAGE = AscensionCraft.prefix("trigger/damage");
    public static final Identifier RESOURCE_REQUESTED = AscensionCraft.prefix("trigger/resource_requested");
    public static final Identifier RESOURCE_APPLIED = AscensionCraft.prefix("trigger/resource_applied");
    public static final Identifier RESOURCE_BEFORE = AscensionCraft.prefix("trigger/resource_before");
    public static final Identifier RESOURCE_AFTER = AscensionCraft.prefix("trigger/resource_after");
    public static final Identifier RESOURCE_MAXIMUM = AscensionCraft.prefix("trigger/resource_maximum");

    private static final Map<TriggerKey, Long> LAST_TRIGGER = new HashMap<>();
    private static long lastCleanupTick = Long.MIN_VALUE;
    private static final ThreadLocal<Set<TriggerKey>> ACTIVE_TRIGGERS = ThreadLocal.withInitial(HashSet::new);
    private static final Comparator<Candidate> ORDER = Comparator
            .comparingInt((Candidate candidate) -> candidate.trigger().priority())
            .thenComparing(candidate -> candidate.skillId().toString())
            .thenComparingInt(Candidate::index);

    private PassiveTriggerService() {
    }

    public static void clearRuntimeState() {
        LAST_TRIGGER.clear();
        lastCleanupTick = Long.MIN_VALUE;
        ACTIVE_TRIGGERS.remove();
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        LAST_TRIGGER.keySet().removeIf(key -> key.owner().equals(playerId));
    }

    public static boolean tryAttack(ServerPlayer player) {
        if (!valid(player)) {
            return false;
        }
        List<Candidate> candidates = candidates(player, PassiveTrigger.Event.ATTACK);
        Candidate selected = candidates.stream().max(ORDER).orElse(null);
        return selected != null && execute(player, null, selected, Map.of());
    }

    public static void trigger(LivingEntity owner, PassiveTrigger.Event event, LivingEntity target, Map<Identifier, Double> variables) {
        trigger(owner, event, target, variables, null);
    }

    public static void trigger(LivingEntity owner, PassiveTrigger.Event event, LivingEntity target,
                               Map<Identifier, Double> variables, DamageSource damageSource) {
        if (!valid(owner) || event == null || event == PassiveTrigger.Event.ATTACK) {
            return;
        }
        List<Candidate> candidates = new ArrayList<>(candidates(owner, event, damageSource));
        candidates.sort(ORDER.reversed());
        for (Candidate candidate : candidates) {
            execute(owner, target, candidate, variables == null ? Map.of() : variables);
        }
    }

    private static List<Candidate> candidates(LivingEntity owner, PassiveTrigger.Event event) {
        return candidates(owner, event, null);
    }

    private static List<Candidate> candidates(LivingEntity owner, PassiveTrigger.Event event, DamageSource damageSource) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(owner);
        if (source == null) {
            return List.of();
        }
        long gameTime = owner.level().getGameTime();
        if (lastCleanupTick == Long.MIN_VALUE || gameTime < lastCleanupTick || gameTime - lastCleanupTick >= 12000L) {
            LAST_TRIGGER.entrySet().removeIf(entry -> gameTime - entry.getValue() > 72000L || gameTime < entry.getValue());
            lastCleanupTick = gameTime;
        }
        List<Candidate> candidates = new ArrayList<>();
        for (Identifier skillId : List.copyOf(AscensionOriginSourceHelper.getSkills(source))) {
            SkillInstance<PassiveSkill.Data> instance = PassiveSkillService.passiveInstance(source, skillId);
            if (instance == null || !(instance.getSkill() instanceof PassiveSkill passive) || !passive.isActive(instance.getData())) {
                continue;
            }
            for (int index = 0; index < passive.triggers().size(); index++) {
                PassiveTrigger trigger = passive.triggers().get(index);
                if (trigger.event() != event) {
                    continue;
                }
                if (event == PassiveTrigger.Event.ATTACK
                        && !WeaponVfxUtils.matchesWeapon(owner, trigger.weaponTag(), trigger.allowEmptyHand())) {
                    continue;
                }
                if (event == PassiveTrigger.Event.DAMAGE_DEALT && trigger.weaponTag().isPresent()
                        && !matchesDamageWeapon(owner, damageSource, trigger)) {
                    continue;
                }
                TriggerKey key = new TriggerKey(owner.getUUID(), skillId, index);
                if (ACTIVE_TRIGGERS.get().contains(key)) {
                    continue;
                }
                long last = LAST_TRIGGER.getOrDefault(key, Long.MIN_VALUE / 2L);
                if (gameTime - last < trigger.cooldown()) {
                    continue;
                }
                candidates.add(new Candidate(skillId, index, trigger));
            }
        }
        return candidates;
    }

    private static boolean execute(LivingEntity owner, LivingEntity target, Candidate candidate, Map<Identifier, Double> variables) {
        PassiveTrigger trigger = candidate.trigger();
        TriggerKey key = new TriggerKey(owner.getUUID(), candidate.skillId(), candidate.index());
        Set<TriggerKey> active = ACTIVE_TRIGGERS.get();
        active.add(key);
        try {
            if (!run(owner, target, candidate.skillId(), trigger.conditions(), trigger.costs(), trigger.actions(), variables)) {
                return false;
            }
            LAST_TRIGGER.put(key, owner.level().getGameTime());
            awardExperience(owner, candidate.skillId(), trigger.experience());
            return true;
        } finally {
            active.remove(key);
            if (active.isEmpty()) {
                ACTIVE_TRIGGERS.remove();
            }
        }
    }

    /**
     * Runs a trigger's body for an owner, as the skill skillId: checks the conditions and that every cost can be paid, pays them,
     * then runs the actions with target as the action target. False (with nothing paid or run) if a condition fails or a cost
     * can't be paid. Doesn't check or start a cooldown. Also used by Qi Coating element hooks (QiCoatingService).
     */
    public static boolean run(LivingEntity owner, LivingEntity target, Identifier skillId, List<SkillCondition> conditions,
                              List<ActiveSkillCostDefinition> costs, List<SkillAction> actions, Map<Identifier, Double> variables) {
        if (!(owner.level() instanceof ServerLevel level)) {
            return false;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(owner);
        if (source == null) {
            return false;
        }
        SkillActionContext actionContext = new SkillActionContext(
                level,
                owner,
                skillId,
                target,
                target == null ? null : target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                0.0D,
                variables
        );
        for (SkillCondition condition : conditions) {
            if (condition == null || !condition.test(actionContext)) {
                return false;
            }
        }
        ScaledValue.Context valueContext = new ScaledValue.Context(source, skillId, owner, target, 0.0D, variables);
        for (ActiveSkillCostDefinition cost : costs) {
            if (!cost.canPay(owner, skillId, target, valueContext, variables)) {
                return false;
            }
        }
        for (ActiveSkillCostDefinition cost : costs) {
            if (!cost.pay(owner, skillId, target, valueContext, variables)) {
                return false;
            }
        }
        SkillActionRuntime.execute(actionContext, actions);
        return true;
    }

    private static void awardExperience(LivingEntity owner, Identifier skillId, double amount) {
        if (amount <= 0.0D) {
            return;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(owner);
        if (source == null) {
            return;
        }
        SkillInstance<PassiveSkill.Data> instance = PassiveSkillService.passiveInstance(source, skillId);
        if (instance == null) {
            return;
        }
        PassiveSkill.Data data = instance.getData();
        long gameTime = owner.level().getGameTime();
        if (data.canAwardExperienceAt(gameTime)
                && SkillProgressionService.addExperience(source, skillId, amount)) {
            data.markExperienceAwardedAt(gameTime);
        }
    }

    private static boolean matchesDamageWeapon(LivingEntity owner, DamageSource damageSource, PassiveTrigger trigger) {
        if (damageSource == null || damageSource.getEntity() != owner || trigger.weaponTag().isEmpty()) {
            return false;
        }
        Entity direct = damageSource.getDirectEntity();
        ItemStack weapon;
        if (direct == owner) {
            weapon = owner.getMainHandItem();
        } else if (direct instanceof AbstractArrow arrow && arrow.getOwner() == owner) {
            weapon = arrow.getWeaponItem();
        } else {
            return false;
        }
        if (weapon == null || weapon.isEmpty()) {
            return direct == owner && trigger.allowEmptyHand();
        }
        return weapon.is(TagKey.<Item>create(Registries.ITEM, trigger.weaponTag().get()));
    }

    private static boolean valid(LivingEntity entity) {
        return entity != null && !entity.level().isClientSide() && !entity.isRemoved() && entity.isAlive()
                && (!(entity instanceof ServerPlayer player) || !player.isSpectator());
    }

    private record TriggerKey(UUID owner, Identifier skill, int index) {
    }

    private record Candidate(Identifier skillId, int index, PassiveTrigger trigger) {
    }
}
