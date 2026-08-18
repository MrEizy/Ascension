package net.zic.ascension.api.ascension.core.technique;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;

public record TechniqueSkillCap(int progression, boolean mastery) {
    public static final Codec<TechniqueSkillCap> CODEC = Codec.either(
            SkillMasteryRank.CODEC,
            Codec.intRange(1, Integer.MAX_VALUE)
    ).xmap(
            value -> value.map(
                    rank -> new TechniqueSkillCap(rank.progression(), true),
                    level -> new TechniqueSkillCap(level, false)
            ),
            cap -> cap.mastery
                    ? Either.left(SkillMasteryRank.fromProgression(cap.progression))
                    : Either.right(cap.progression)
    );

    public TechniqueSkillCap {
        progression = Math.max(1, progression);
    }

    public SkillMasteryRank masteryRank() {
        return SkillMasteryRank.fromProgression(progression);
    }
}
