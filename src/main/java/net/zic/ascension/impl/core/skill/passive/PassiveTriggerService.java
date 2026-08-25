package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.runtime.weapon.WeaponVfxUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PassiveTriggerService {
    private static final Map<TriggerKey, Long> LAST_TRIGGER = new HashMap<>();
    private static final Comparator<Candidate> ORDER = Comparator
            .comparingInt((Candidate candidate) -> candidate.trigger().priority())
            .thenComparing(candidate -> candidate.skillId().toString())
            .thenComparingInt(Candidate::index);

    private PassiveTriggerService() {
    }

    public static void clearRuntimeState() {
        LAST_TRIGGER.clear();
    }

    public static boolean tryAttack(ServerPlayer player) {
        if (player == null || player.isRemoved() || !player.isAlive() || player.isSpectator()) {
            return false;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        Candidate selected = null;
        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, player.registryAccess());
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof PassiveSkill passive) || !(data instanceof PassiveSkill.Data passiveData) || !passive.isActive(passiveData)) {
                continue;
            }
            for (int index = 0; index < passive.triggers().size(); index++) {
                PassiveTrigger trigger = passive.triggers().get(index);
                if (trigger.event() != PassiveTrigger.Event.ATTACK
                        || !WeaponVfxUtils.matchesWeapon(player, trigger.weaponTag(), trigger.allowEmptyHand())) {
                    continue;
                }
                TriggerKey key = new TriggerKey(player.getUUID(), skillId, index);
                long last = LAST_TRIGGER.getOrDefault(key, Long.MIN_VALUE / 2L);
                if (gameTime - last < trigger.cooldown()) {
                    continue;
                }
                Candidate candidate = new Candidate(skillId, index, trigger);
                if (selected == null || ORDER.compare(candidate, selected) > 0) {
                    selected = candidate;
                }
            }
        }

        if (selected == null || !execute(player, source, selected)) {
            return false;
        }
        LAST_TRIGGER.put(new TriggerKey(player.getUUID(), selected.skillId(), selected.index()), gameTime);
        return true;
    }

    private static boolean execute(ServerPlayer player, OriginSource source, Candidate candidate) {
        PassiveTrigger trigger = candidate.trigger();
        SkillActionContext actionContext = new SkillActionContext(
                player.level(),
                player,
                candidate.skillId(),
                null,
                player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D),
                0.0D,
                Map.of()
        );
        ScaledValue.Context valueContext = new ScaledValue.Context(
                source,
                candidate.skillId(),
                player,
                null,
                0.0D,
                Map.of()
        );

        for (ActiveSkillCostDefinition cost : trigger.costs()) {
            if (!cost.canPay(player, candidate.skillId(), null, valueContext, Map.of())) {
                return false;
            }
        }
        for (ActiveSkillCostDefinition cost : trigger.costs()) {
            if (!cost.pay(player, candidate.skillId(), null, valueContext, Map.of())) {
                return false;
            }
        }
        trigger.actions().forEach(action -> action.apply(actionContext));
        return true;
    }

    private record TriggerKey(UUID player, Identifier skill, int index) {
    }

    private record Candidate(Identifier skillId, int index, PassiveTrigger trigger) {
    }
}
