package net.zic.ascension.impl.core.technique;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkill;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkillData;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.TechniqueSkillCap;
import net.zic.ascension.api.ascension.core.technique.TechniqueSkillDefinition;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TechniqueSkillService {
    private static final Set<OriginSource> RECONCILING = Collections.newSetFromMap(new IdentityHashMap<>());

    private TechniqueSkillService() {
    }

    public static void reconcileAll(OriginSource source) {
        if (source == null || !RECONCILING.add(source)) {
            return;
        }
        try {
            for (Identifier techniqueId : List.copyOf(AscensionOriginSourceHelper.getTechniques(source))) {
                var technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, techniqueId, source.getRegistryAccess());
                if (!(technique instanceof SimpleTechnique simple)) {
                    continue;
                }
                PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source, simple.getPath());
                if (pathInstance != null) {
                    reconcile(source, techniqueId, pathInstance, simple.getSkills());
                }
            }
        } finally {
            RECONCILING.remove(source);
        }
    }

    public static void reconcile(
            OriginSource source,
            Identifier techniqueId,
            PathInstance pathInstance,
            Map<Identifier, TechniqueSkillDefinition> skills
    ) {
        if (source == null || techniqueId == null || pathInstance == null || skills == null || skills.isEmpty()) {
            return;
        }

        int majorRealm = pathInstance.getCurrentMajorRealm();
        for (Map.Entry<Identifier, TechniqueSkillDefinition> entry : skills.entrySet()) {
            Identifier skillId = entry.getKey();
            TechniqueSkillDefinition definition = entry.getValue();
            if (skillId == null || definition == null) {
                continue;
            }

            if (majorRealm < definition.unlock() || !definition.requirements().test(source)) {
                SkillProgressionService.removeCap(source, skillId, techniqueId);
                AscensionOriginSourceHelper.removeSkill(source, skillId, techniqueId);
                continue;
            }

            if (!AscensionOriginSourceHelper.addSkill(source, skillId, techniqueId)) {
                continue;
            }

            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, source.getRegistryAccess());
            if (!(skill instanceof ProgressingSkill)) {
                continue;
            }

            TechniqueSkillCap cap = definition.capAt(majorRealm).orElse(null);
            if (cap == null) {
                SkillProgressionService.removeCap(source, skillId, techniqueId);
                continue;
            }

            SkillProgressionService.setCap(source, skillId, techniqueId, cap.progression());
            if (!(skill instanceof ActiveSkill)) {
                SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
                if (data instanceof ProgressingSkillData progressingData
                        && progressingData.getSkillProgression().getTrainedProgression() < cap.progression()) {
                    SkillProgressionService.setTrainedProgression(source, skillId, cap.progression());
                }
            }
        }
    }

    public static void remove(
            OriginSource source,
            Identifier techniqueId,
            Map<Identifier, TechniqueSkillDefinition> skills
    ) {
        if (source == null || techniqueId == null || skills == null || skills.isEmpty()) {
            return;
        }

        for (Identifier skillId : skills.keySet()) {
            SkillProgressionService.removeCap(source, skillId, techniqueId);
            AscensionOriginSourceHelper.removeSkill(source, skillId, techniqueId);
        }
    }
}
