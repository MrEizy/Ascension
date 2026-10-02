package net.zic.ascension.api.ascension.core.skill;

public record SkillProgressionSnapshot(
        int trainedProgression,
        double experience,
        int accessibleCap,
        int permanentProgression,
        int effectiveProgression
) {
}
