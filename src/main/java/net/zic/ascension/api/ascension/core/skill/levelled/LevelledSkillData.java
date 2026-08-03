package net.zic.ascension.api.ascension.core.skill.levelled;

import net.zic.ascension.api.ascension.core.skill.SkillData;

public interface LevelledSkillData extends SkillData {
    SkillProgressionData getSkillProgression();
}
