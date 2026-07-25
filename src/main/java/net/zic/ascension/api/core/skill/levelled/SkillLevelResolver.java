package net.zic.ascension.api.core.skill.levelled;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.event.skill.SkillLevelResolveEvent;

public final class SkillLevelResolver {
    private SkillLevelResolver() {
    }

    public static SkillLevelSnapshot resolve(OriginSource source, Identifier skillId) {
        if (source == null || skillId == null || source.getRegistryAccess() == null) {
            return emptySnapshot();
        }

        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                source.getRegistryAccess()
        );
        SkillData data = source.getSkillData(skillId);
        if (!(skill instanceof LevelledSkill levelledSkill)
                || !(data instanceof LevelledSkillData levelledData)) {
            return emptySnapshot();
        }

        return resolve(source, skillId, levelledSkill, levelledData);
    }

    public static SkillLevelSnapshot resolve(
            OriginSource source,
            Identifier skillId,
            LevelledSkill skill,
            LevelledSkillData data
    ) {
        SkillProgressionData progression = data.getSkillProgression();
        int maximumLevel = Math.max(0, skill.getMaximumLevel());
        int trainedLevel = clamp(progression.getTrainedLevel(), 0, maximumLevel);
        int levelFloor = clamp(progression.getLevelFloor(), 0, maximumLevel);
        int accessibleLevelCap = progression.getAccessibleLevelCap(
                skill.getDefaultAccessibleLevel(),
                maximumLevel
        );
        int permanentLevel = Math.min(Math.max(trainedLevel, levelFloor), accessibleLevelCap);

        SkillLevelResolveEvent event = new SkillLevelResolveEvent(
                source,
                skillId,
                data,
                permanentLevel,
                maximumLevel
        );
        NeoForge.EVENT_BUS.post(event);

        return new SkillLevelSnapshot(
                trainedLevel,
                progression.getExperience(),
                levelFloor,
                accessibleLevelCap,
                permanentLevel,
                event.resolve()
        );
    }

    private static SkillLevelSnapshot emptySnapshot() {
        return new SkillLevelSnapshot(0, 0.0D, 0, 0, 0, 0);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
