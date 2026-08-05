package net.zic.ascension.mob_cultivation.skill;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MobCultivationSkillService {
    private MobCultivationSkillService() {}

    public static void synchronize(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        OriginSource source = MobCultivationManager.getEntityData(mob).getSource();
        int realmScore = Math.max(0, MobCultivationManager.getRealmScore(mob));
        int desiredCount = realmScore < 3 ? 0 : realmScore < 9 ? 1 : realmScore < 15 ? 2 : 3;
        if (data.getEliteTier().ordinal() > 0) desiredCount++;

        List<MobCultivationSkillPool.Entry> candidates = new ArrayList<>();
        for (Identifier poolId : data.getSkillPools()) {
            MobCultivationSkillPool pool = MobCultivationSkillPoolManager.get(poolId);
            if (pool == null) continue;
            for (MobCultivationSkillPool.Entry entry : pool.entries()) {
                if (entry.minimumRealmScore() <= realmScore
                        && CoreRegistries.SKILL_REGISTRY.get(source.getRegistryAccess()).containsKey(entry.skill())) {
                    candidates.add(entry);
                }
            }
        }
        candidates.sort(Comparator
                .comparingDouble(MobCultivationSkillPool.Entry::weight).reversed()
                .thenComparing(entry -> entry.skill().toString()));

        Set<Identifier> desired = new LinkedHashSet<>();
        for (MobCultivationSkillPool.Entry entry : candidates) {
            desired.add(entry.skill());
            if (desired.size() >= desiredCount) break;
        }

        for (Identifier existing : List.copyOf(AscensionOriginSourceHelper.getSkills(source))) {
            if (!desired.contains(existing)) {
                AscensionOriginSourceHelper.removeSkill(source, existing, MobCultivationManager.MOB_CULTIVATION_OWNER);
            }
        }
        for (Identifier skill : desired) {
            if (!AscensionOriginSourceHelper.hasSkill(source, skill)) {
                AscensionOriginSourceHelper.addSkill(source, skill, MobCultivationManager.MOB_CULTIVATION_OWNER);
            }
        }
    }
}
