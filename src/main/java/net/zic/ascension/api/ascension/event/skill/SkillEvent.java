package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkillData;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionSnapshot;
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

    public static final class ProgressionChanged extends SkillEvent {
        private final SkillProgressionSnapshot previous;
        private final SkillProgressionSnapshot current;
        private final Reason reason;

        public ProgressionChanged(
                OriginSource source,
                Identifier skill,
                ProgressingSkillData skillData,
                SkillProgressionSnapshot previous,
                SkillProgressionSnapshot current,
                Reason reason
        ) {
            super(source, skill, skillData);
            this.previous = previous;
            this.current = current;
            this.reason = reason;
        }

        public SkillProgressionSnapshot getPrevious() {
            return previous;
        }

        public SkillProgressionSnapshot getCurrent() {
            return current;
        }

        public Reason getReason() {
            return reason;
        }

        public enum Reason {
            TRAINED_PROGRESSION,
            EXPERIENCE,
            CAP,
            CAP_REMOVED
        }
    }

    public static final class ProgressionResolve extends SkillEvent {
        private final int permanentProgression;
        private final int absoluteMaximumProgression;
        private int additiveModifier;
        private int minimumProgression;
        private int maximumProgression;

        public ProgressionResolve(
                OriginSource source,
                Identifier skill,
                ProgressingSkillData skillData,
                int permanentProgression,
                int maximumProgression
        ) {
            super(source, skill, skillData);
            this.permanentProgression = permanentProgression;
            this.absoluteMaximumProgression = Math.max(1, maximumProgression);
            this.minimumProgression = 1;
            this.maximumProgression = this.absoluteMaximumProgression;
        }

        public int getPermanentProgression() {
            return permanentProgression;
        }

        public int getAdditiveModifier() {
            return additiveModifier;
        }

        public void addProgression(int progression) {
            additiveModifier += progression;
        }

        public int getMinimumProgression() {
            return minimumProgression;
        }

        public void raiseMinimumProgression(int progression) {
            int resolved = Math.max(1, Math.min(maximumProgression, progression));
            minimumProgression = Math.max(minimumProgression, resolved);
        }

        public int getMaximumProgression() {
            return maximumProgression;
        }

        public void lowerMaximumProgression(int progression) {
            int resolved = Math.max(1, Math.min(absoluteMaximumProgression, progression));
            maximumProgression = Math.min(maximumProgression, resolved);
            minimumProgression = Math.min(minimumProgression, maximumProgression);
        }

        public int resolve() {
            int modified = permanentProgression + additiveModifier;
            return Math.max(minimumProgression, Math.min(maximumProgression, modified));
        }
    }
}
