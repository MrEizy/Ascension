package net.zic.ascension.api.core.skill.levelled;

import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.source.OriginSource;

public interface LevelledSkill extends Skill {
    int getMaximumLevel();

    default int getDefaultAccessibleLevel() {
        return getMaximumLevel();
    }

    default double getExperienceRequiredForNextLevel(int currentLevel) {
        return Double.POSITIVE_INFINITY;
    }

    default void onLevelChanged(
            OriginSource source,
            LevelledSkillData data,
            int previousLevel,
            int currentLevel
    ) {
    }
}
