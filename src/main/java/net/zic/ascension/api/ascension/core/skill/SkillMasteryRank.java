package net.zic.ascension.api.ascension.core.skill;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum SkillMasteryRank implements StringRepresentable {
    INITIATE("initiate", "Initiate", 1, 0.0D),
    MINOR_MASTERY("minor_mastery", "Minor Mastery", 2, 100.0D),
    MAJOR_MASTERY("major_mastery", "Major Mastery", 3, 300.0D),
    PERFECTION("perfection", "Perfection", 4, 900.0D),
    TRANSCENDENCE("transcendence", "Transcendence", 5, 2500.0D);

    public static final Codec<SkillMasteryRank> CODEC = StringRepresentable.fromEnum(SkillMasteryRank::values);

    private final String serializedName;
    private final String displayName;
    private final int progression;
    private final double defaultExperienceToReach;

    SkillMasteryRank(String serializedName, String displayName, int progression, double defaultExperienceToReach) {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.progression = progression;
        this.defaultExperienceToReach = defaultExperienceToReach;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String displayName() {
        return displayName;
    }

    public int progression() {
        return progression;
    }

    public double defaultExperienceToReach() {
        return defaultExperienceToReach;
    }

    public static SkillMasteryRank fromProgression(int progression) {
        int resolved = Math.clamp(progression, INITIATE.progression, TRANSCENDENCE.progression);
        for (SkillMasteryRank rank : values()) {
            if (rank.progression == resolved) {
                return rank;
            }
        }
        return INITIATE;
    }

    public static SkillMasteryRank fromName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toLowerCase(Locale.ROOT).replace(' ', '_');
        for (SkillMasteryRank rank : values()) {
            if (rank.serializedName.equals(normalized) || rank.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return rank;
            }
        }
        return null;
    }
}
