package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.levelled.LevelledSkillData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public final class SkillLevelResolveEvent extends SkillEvent {
    private final int permanentLevel;
    private final int absoluteMaximumLevel;
    private int additiveModifier;
    private int minimumLevel;
    private int maximumLevel;

    public SkillLevelResolveEvent(
            OriginSource source,
            Identifier skill,
            LevelledSkillData skillData,
            int permanentLevel,
            int maximumLevel
    ) {
        super(source, skill, skillData);
        this.permanentLevel = permanentLevel;
        this.absoluteMaximumLevel = Math.max(0, maximumLevel);
        this.maximumLevel = this.absoluteMaximumLevel;
    }

    public int getPermanentLevel() {
        return permanentLevel;
    }

    public int getAdditiveModifier() {
        return additiveModifier;
    }

    public void addLevels(int levels) {
        additiveModifier += levels;
    }

    public int getMinimumLevel() {
        return minimumLevel;
    }

    public void raiseMinimumLevel(int minimumLevel) {
        int clampedLevel = Math.max(0, Math.min(maximumLevel, minimumLevel));
        this.minimumLevel = Math.max(this.minimumLevel, clampedLevel);
    }

    public int getMaximumLevel() {
        return maximumLevel;
    }

    public void lowerMaximumLevel(int maximumLevel) {
        int clampedLevel = Math.max(0, Math.min(absoluteMaximumLevel, maximumLevel));
        this.maximumLevel = Math.min(this.maximumLevel, clampedLevel);
        minimumLevel = Math.min(minimumLevel, this.maximumLevel);
    }

    public int resolve() {
        int modifiedLevel = permanentLevel + additiveModifier;
        return Math.max(minimumLevel, Math.min(maximumLevel, modifiedLevel));
    }
}
