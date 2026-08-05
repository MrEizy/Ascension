package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.LevelledSkillData;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillLevelSnapshot;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class SkillEvent extends Event {
    private final OriginSource source;
    private final Identifier skill;
    private final SkillData skillData;

    protected SkillEvent(OriginSource source, Identifier skill, SkillData skillData) {
        this.source = source;
        this.skill = skill;
        this.skillData = skillData;
    }

    public OriginSource getSource() {
        return source;
    }

    public Identifier getSkill() {
        return skill;
    }

    public SkillData getSkillData() {
        return skillData;
    }

    public Skill getSkill(RegistryAccess access) {
        return CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, getSkill(), access);
    }

    public abstract static class Added extends SkillEvent {
        protected Added(OriginSource source, Identifier skill, SkillData skillData) {
            super(source, skill, skillData);
        }

        public static final class Pre extends Added implements ICancellableEvent {
            public Pre(OriginSource source, Identifier skill, SkillData skillData) {
                super(source, skill, skillData);
            }
        }

        public static final class Post extends Added {
            public Post(OriginSource source, Identifier skill, SkillData skillData) {
                super(source, skill, skillData);
            }
        }
    }

    public abstract static class Removed extends SkillEvent {
        protected Removed(OriginSource source, Identifier skill, SkillData skillData) {
            super(source, skill, skillData);
        }

        public static final class Pre extends Removed implements ICancellableEvent {
            public Pre(OriginSource source, Identifier skill, SkillData skillData) {
                super(source, skill, skillData);
            }
        }

        public static final class Post extends Removed {
            public Post(OriginSource source, Identifier skill, SkillData skillData) {
                super(source, skill, skillData);
            }
        }
    }

    public static final class LevelChanged extends SkillEvent {
        private final SkillLevelSnapshot previous;
        private final SkillLevelSnapshot current;
        private final Reason reason;

        public LevelChanged(OriginSource source, Identifier skill, LevelledSkillData skillData, SkillLevelSnapshot previous, SkillLevelSnapshot current, Reason reason) {
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

    public static final class LevelResolve extends SkillEvent {
        private final int permanentLevel;
        private final int absoluteMaximumLevel;
        private int additiveModifier;
        private int minimumLevel;
        private int maximumLevel;

        public LevelResolve(OriginSource source, Identifier skill, LevelledSkillData skillData, int permanentLevel, int maximumLevel) {
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
}
