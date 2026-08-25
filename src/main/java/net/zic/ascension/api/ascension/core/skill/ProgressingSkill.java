package net.zic.ascension.api.ascension.core.skill;

import net.zic.ascension.api.rpg_engine.source.OriginSource;

public interface ProgressingSkill extends Skill {
    int getMaximumProgression();

    int getInitialProgression();

    int getDefaultProgressionCap();

    default double getExperienceRequiredForNextProgression(int currentProgression) {
        return Double.POSITIVE_INFINITY;
    }

    default void onProgressionChanged(
            OriginSource source,
            ProgressingSkillData data,
            int previousProgression,
            int currentProgression
    ) {
    }
}
