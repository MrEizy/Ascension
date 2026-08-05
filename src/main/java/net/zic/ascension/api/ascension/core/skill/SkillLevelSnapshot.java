package net.zic.ascension.api.ascension.core.skill;

public record SkillLevelSnapshot(
        int trainedLevel,
        double experience,
        int levelFloor,
        int accessibleLevelCap,
        int permanentLevel,
        int effectiveLevel
) {
}
