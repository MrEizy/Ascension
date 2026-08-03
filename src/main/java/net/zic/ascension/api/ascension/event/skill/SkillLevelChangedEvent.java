package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.levelled.LevelledSkillData;
import net.zic.ascension.api.core.skill.levelled.SkillLevelSnapshot;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public final class SkillLevelChangedEvent extends SkillEvent {
    private final SkillLevelSnapshot previous;
    private final SkillLevelSnapshot current;
    private final Reason reason;

    public SkillLevelChangedEvent(
            OriginSource source,
            Identifier skill,
            LevelledSkillData skillData,
            SkillLevelSnapshot previous,
            SkillLevelSnapshot current,
            Reason reason
    ) {
        super(source, skill, skillData);
        this.previous = previous;
        this.current = current;
        this.reason = reason;
    }

    public SkillLevelSnapshot getPrevious() {
        return previous;
    }

    public SkillLevelSnapshot getCurrent() {
        return current;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        TRAINED_LEVEL,
        EXPERIENCE,
        LEVEL_FLOOR,
        LEVEL_CAP,
        LEVEL_CONTRIBUTION,
        CONTRIBUTION_REMOVED
    }
}
