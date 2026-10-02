package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
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

public final class PassiveTriggerService {
    public static final Identifier DAMAGE = AscensionCraft.prefix("trigger/damage");
    public static final Identifier RESOURCE_REQUESTED = AscensionCraft.prefix("trigger/resource_requested");
    public static final Identifier RESOURCE_APPLIED = AscensionCraft.prefix("trigger/resource_applied");
    public static final Identifier RESOURCE_BEFORE = AscensionCraft.prefix("trigger/resource_before");
    public static final Identifier RESOURCE_AFTER = AscensionCraft.prefix("trigger/resource_after");
    public static final Identifier RESOURCE_MAXIMUM = AscensionCraft.prefix("trigger/resource_maximum");

    private static final Map<TriggerKey, Long> LAST_TRIGGER = new HashMap<>();
    private static final ThreadLocal<Set<TriggerKey>> ACTIVE_TRIGGERS = ThreadLocal.withInitial(HashSet::new);
    private static final Comparator<Candidate> ORDER = Comparator
            .comparingInt((Candidate candidate) -> candidate.trigger().priority())
            .thenComparing(candidate -> candidate.skillId().toString())
            .thenComparingInt(Candidate::index);

    private PassiveTriggerService() {
    }

    public static void clearRuntimeState() {
        LAST_TRIGGER.clear();
        ACTIVE_TRIGGERS.remove();
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
        if (!valid(owner) || event == null || event == PassiveTrigger.Event.ATTACK) {
            return;
        }
        List<Candidate> candidates = new ArrayList<>(candidates(owner, event));
        candidates.sort(ORDER.reversed());
        for (Candidate candidate : candidates) {
            execute(owner, target, candidate, variables == null ? Map.of() : variables);
        }
    }

    private static List<Candidate> candidates(LivingEntity owner, PassiveTrigger.Event event) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(owner);
        if (source == null) {
            return List.of();
        }
        long gameTime = owner.level().getGameTime();
        List<Candidate> candidates = new ArrayList<>();
        for (Identifier skillId : List.copyOf(AscensionOriginSourceHelper.getSkills(source))) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, owner.registryAccess());
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof PassiveSkill passive) || !(data instanceof PassiveSkill.Data passiveData) || !passive.isActive(passiveData)) {
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
        if (!(owner.level() instanceof ServerLevel level)) {
            return false;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(owner);
        if (source == null) {
            return false;
        }
        PassiveTrigger trigger = candidate.trigger();
        SkillActionContext actionContext = new SkillActionContext(
                level,
                owner,
                candidate.skillId(),
                target,
                target == null ? null : target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                0.0D,
                variables
        );
        for (SkillCondition condition : trigger.conditions()) {
            if (condition == null || !condition.test(actionContext)) {
                return false;
            }
        }
        ScaledValue.Context valueContext = new ScaledValue.Context(source, candidate.skillId(), owner, target, 0.0D, variables);
        for (ActiveSkillCostDefinition cost : trigger.costs()) {
            if (!cost.canPay(owner, candidate.skillId(), target, valueContext, variables)) {
                return false;
            }
        }
        TriggerKey key = new TriggerKey(owner.getUUID(), candidate.skillId(), candidate.index());
        Set<TriggerKey> active = ACTIVE_TRIGGERS.get();
        active.add(key);
        try {
            for (ActiveSkillCostDefinition cost : trigger.costs()) {
                if (!cost.pay(owner, candidate.skillId(), target, valueContext, variables)) {
                    return false;
                }
            }
            SkillActionRuntime.execute(actionContext, trigger.actions());
            LAST_TRIGGER.put(key, owner.level().getGameTime());
            return true;
        } finally {
            active.remove(key);
            if (active.isEmpty()) {
                ACTIVE_TRIGGERS.remove();
            }
        }
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
