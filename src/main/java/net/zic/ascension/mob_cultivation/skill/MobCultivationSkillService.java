package net.zic.ascension.mob_cultivation.skill;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MobCultivationSkillService {
    private MobCultivationSkillService() {
    }

    public static void synchronize(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        OriginSource source = MobCultivationManager.getEntityData(mob).getSource();
        int realmScore = Math.max(0, MobCultivationManager.getRealmScore(mob));
        int desiredCount = realmScore < 3 ? 0 : realmScore < 9 ? 1 : realmScore < 15 ? 2 : 3;
        if (data.getEliteTier().ordinal() > 0) {
            desiredCount++;
        }

        Map<Identifier, Double> weightedCandidates = collectCandidates(data, source, realmScore);
        Set<Identifier> desired = selectStableWeighted(
                mob,
                data.getAssignedSkills(),
                weightedCandidates,
                desiredCount
        );

        for (Identifier existing : data.getAssignedSkills()) {
            if (desired.contains(existing)) {
                continue;
            }
            SkillProgressionService.removeContribution(source, existing, MobCultivationManager.MOB_CULTIVATION_OWNER);
            AscensionOriginSourceHelper.removeSkill(source, existing, MobCultivationManager.MOB_CULTIVATION_OWNER);
        }

        for (Identifier skill : desired) {
            if (!AscensionOriginSourceHelper.hasSkill(source, skill)) {
                AscensionOriginSourceHelper.addSkill(source, skill, MobCultivationManager.MOB_CULTIVATION_OWNER);
            }
            SkillProgressionService.setLevelContribution(
                    source,
                    skill,
                    MobCultivationManager.MOB_CULTIVATION_OWNER,
                    1,
                    true,
                    true
            );
        }
        data.setAssignedSkills(desired);
    }

    public static void clear(MobCultivationData data, OriginSource source) {
        for (Identifier skill : data.getAssignedSkills()) {
            SkillProgressionService.removeContribution(source, skill, MobCultivationManager.MOB_CULTIVATION_OWNER);
            AscensionOriginSourceHelper.removeSkill(source, skill, MobCultivationManager.MOB_CULTIVATION_OWNER);
        }
        data.setAssignedSkills(Set.of());
    }

    public static List<MobCultivationSkillPool.Entry> ownedEntries(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        OriginSource source = MobCultivationManager.getEntityData(mob).getSource();
        int realmScore = Math.max(0, MobCultivationManager.getRealmScore(mob));
        Map<Identifier, MobCultivationSkillPool.Entry> entries = new LinkedHashMap<>();

        for (Identifier poolId : data.getSkillPools()) {
            MobCultivationSkillPool pool = MobCultivationSkillPoolManager.get(poolId);
            if (pool == null) {
                continue;
            }
            for (MobCultivationSkillPool.Entry entry : pool.entries()) {
                if (entry.minimumRealmScore() > realmScore
                        || !data.getAssignedSkills().contains(entry.skill())
                        || !AscensionOriginSourceHelper.hasSkill(source, entry.skill())) {
                    continue;
                }
                MobCultivationSkillPool.Entry previous = entries.get(entry.skill());
                if (previous == null || entry.priority() > previous.priority()) {
                    entries.put(entry.skill(), entry);
                }
            }
        }
        return List.copyOf(entries.values());
    }

    private static Map<Identifier, Double> collectCandidates(
            MobCultivationData data,
            OriginSource source,
            int realmScore
    ) {
        Map<Identifier, Double> candidates = new LinkedHashMap<>();
        for (Identifier poolId : data.getSkillPools()) {
            MobCultivationSkillPool pool = MobCultivationSkillPoolManager.get(poolId);
            if (pool == null) {
                continue;
            }
            for (MobCultivationSkillPool.Entry entry : pool.entries()) {
                if (entry.minimumRealmScore() > realmScore
                        || !CoreRegistries.SKILL_REGISTRY.get(source.getRegistryAccess()).containsKey(entry.skill())) {
                    continue;
                }
                candidates.merge(entry.skill(), entry.weight(), Double::sum);
            }
        }
        return candidates;
    }

    private static Set<Identifier> selectStableWeighted(
            Mob mob,
            Set<Identifier> existingSkills,
            Map<Identifier, Double> weightedCandidates,
            int count
    ) {
        if (count <= 0 || weightedCandidates.isEmpty()) {
            return Set.of();
        }

        Set<Identifier> selected = new LinkedHashSet<>();
        existingSkills.stream()
                .filter(weightedCandidates::containsKey)
                .sorted(java.util.Comparator.comparing(Identifier::toString))
                .limit(count)
                .forEach(selected::add);

        long seed = mob.getUUID().getMostSignificantBits()
                ^ Long.rotateLeft(mob.getUUID().getLeastSignificantBits(), 17);
        RandomSource random = RandomSource.create(seed);
        Map<Identifier, Double> remaining = new LinkedHashMap<>(weightedCandidates);
        selected.forEach(remaining::remove);

        for (int skipped = 0; skipped < selected.size(); skipped++) {
            random.nextLong();
        }
        while (selected.size() < count && !remaining.isEmpty()) {
            Identifier choice = chooseWeighted(random, remaining);
            if (choice == null) {
                break;
            }
            selected.add(choice);
            remaining.remove(choice);
        }
        return Set.copyOf(selected);
    }

    private static Identifier chooseWeighted(RandomSource random, Map<Identifier, Double> candidates) {
        double total = 0.0D;
        for (double weight : candidates.values()) {
            if (Double.isFinite(weight) && weight > 0.0D) {
                total += weight;
            }
        }
        if (total <= 0.0D) {
            return null;
        }

        double roll = random.nextDouble() * total;
        Identifier fallback = null;
        for (Map.Entry<Identifier, Double> entry : candidates.entrySet()) {
            if (!Double.isFinite(entry.getValue()) || entry.getValue() <= 0.0D) {
                continue;
            }
            fallback = entry.getKey();
            roll -= entry.getValue();
            if (roll <= 0.0D) {
                return entry.getKey();
            }
        }
        return fallback;
    }
}
