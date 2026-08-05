package net.zic.ascension.mob_cultivation.skill;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.Config;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;

import java.util.ArrayList;
import java.util.List;

public final class MobCultivationCastingController {
    private MobCultivationCastingController() {
    }

    public static void tick(Mob mob, long gameTime) {
        if (!Config.MOB_CULTIVATION.SKILLS_ENABLED.get() || mob.isNoAi()) {
            return;
        }

        int interval = Config.MOB_CULTIVATION.SKILL_DECISION_INTERVAL.get();
        int offset = Math.floorMod(mob.getId(), interval);
        if (Math.floorMod(gameTime + offset, interval) != 0L) {
            return;
        }

        LivingEntity target = mob.getTarget();
        boolean hasTarget = target != null && target.isAlive() && !target.isRemoved();
        double targetDistance = hasTarget ? mob.distanceTo(target) : 0.0D;
        double maximumHealth = Math.max(1.0D, mob.getMaxHealth());
        double healthFraction = Math.clamp(mob.getHealth() / maximumHealth, 0.0D, 1.0D);

        List<Candidate> candidates = new ArrayList<>();
        for (MobCultivationSkillPool.Entry entry : MobCultivationSkillService.ownedEntries(mob)) {
            if (!entry.canUse(healthFraction, targetDistance, hasTarget)) {
                continue;
            }
            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    entry.skill(),
                    mob.registryAccess()
            );
            if (skill instanceof ActiveSkill activeSkill) {
                candidates.add(new Candidate(entry, activeSkill));
            }
        }

        if (hasTarget) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        while (!candidates.isEmpty()) {
            int highestPriority = candidates.stream()
                    .mapToInt(candidate -> candidate.entry().priority())
                    .max()
                    .orElse(Integer.MIN_VALUE);
            List<Candidate> priorityGroup = candidates.stream()
                    .filter(candidate -> candidate.entry().priority() == highestPriority)
                    .toList();
            Candidate selected = chooseWeighted(mob, priorityGroup);
            if (selected == null) {
                return;
            }
            candidates.remove(selected);

            CastResult result = selected.skill().tryCast(mob);
            if (!result.isSuccess()) {
                continue;
            }
            selected.skill().initialCast(mob, selected.skill().newPreCastData());
            return;
        }
    }

    private static Candidate chooseWeighted(Mob mob, List<Candidate> candidates) {
        double total = 0.0D;
        for (Candidate candidate : candidates) {
            total += candidate.entry().weight();
        }
        if (!Double.isFinite(total) || total <= 0.0D) {
            return null;
        }

        double roll = mob.getRandom().nextDouble() * total;
        Candidate fallback = null;
        for (Candidate candidate : candidates) {
            fallback = candidate;
            roll -= candidate.entry().weight();
            if (roll <= 0.0D) {
                return candidate;
            }
        }
        return fallback;
    }

    private record Candidate(MobCultivationSkillPool.Entry entry, ActiveSkill skill) {
    }
}
