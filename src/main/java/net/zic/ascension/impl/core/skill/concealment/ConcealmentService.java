package net.zic.ascension.impl.core.skill.concealment;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.passive.PassiveSkill;

public final class ConcealmentService {
    private ConcealmentService() {
    }

    public static Component concealDeathMessage(Component message, ServerPlayer victim, DamageSource damageSource) {
        LivingEntity killer = damageSource.getEntity() instanceof LivingEntity source
                ? source
                : victim.getKillCredit();
        if (killer == null || killer == victim) {
            return message;
        }
        Component disguise = deathMessageIdentity(killer);
        if (disguise == null) {
            return message;
        }
        return replaceKiller(message, killer, disguise, 0);
    }

    public static Component deathMessageIdentity(LivingEntity entity) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(entity);
        if (source == null) {
            return null;
        }
        Component best = null;
        int bestPriority = Integer.MIN_VALUE;
        String bestId = null;
        for (SkillInstance<?> instance : AscensionOriginSourceHelper.getSkillInstances(source)) {
            if (!(instance.getSkill() instanceof ConcealmentSkill skill)
                    || !(instance.getData() instanceof PassiveSkill.Data data)
                    || !skill.isActive(data)
                    || skill.identity().deathMessage().isEmpty()) {
                continue;
            }
            String id = instance.getSkillId().toString();
            if (best == null || skill.priority() > bestPriority
                    || skill.priority() == bestPriority && id.compareTo(bestId) < 0) {
                best = skill.identity().deathMessage().get();
                bestPriority = skill.priority();
                bestId = id;
            }
        }
        return best;
    }

    private static Component replaceKiller(Component message, LivingEntity killer, Component disguise, int depth) {
        if (depth > 16) {
            return message;
        }
        if (depth > 0 && (message.getString().equals(killer.getDisplayName().getString())
                || message.getString().equals(killer.getName().getString()))) {
            return disguise;
        }
        MutableComponent rebuilt;
        if (message.getContents() instanceof TranslatableContents translated) {
            Object[] original = translated.getArgs();
            Object[] args = original.clone();
            boolean changed = false;
            for (int i = 1; i < args.length; i++) {
                if (args[i] instanceof Component component) {
                    Component replacement = replaceKiller(component, killer, disguise, depth + 1);
                    if (replacement != component) {
                        args[i] = replacement;
                        changed = true;
                    }
                } else if (args[i] instanceof String text && (text.equals(killer.getName().getString())
                        || text.equals(killer.getDisplayName().getString()))) {
                    args[i] = disguise;
                    changed = true;
                }
            }
            rebuilt = changed
                    ? Component.translatableWithFallback(translated.getKey(), translated.getFallback(), args)
                    : message.plainCopy();
        } else {
            rebuilt = message.plainCopy();
        }
        rebuilt.setStyle(message.getStyle());
        for (Component sibling : message.getSiblings()) {
            rebuilt.append(replaceKiller(sibling, killer, disguise, depth + 1));
        }
        return rebuilt;
    }
}
