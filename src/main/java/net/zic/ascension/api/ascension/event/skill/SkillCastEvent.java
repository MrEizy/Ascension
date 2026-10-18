package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import org.jspecify.annotations.Nullable;

/**
 * cast lifecycle events, only posted on the server
 */
public abstract class SkillCastEvent extends Event {
    private final CastingSkillInstance<?, ?, ?> instance;

    protected SkillCastEvent(CastingSkillInstance<?, ?, ?> instance) {
        this.instance = instance;
    }

    public CastingSkillInstance<?, ?, ?> getInstance() {
        return instance;
    }

    public LivingEntity getCaster() {
        return instance.getCaster();
    }

    public Identifier getSkill() {
        return instance.getSkillId();
    }

    public abstract static class Start extends SkillCastEvent {
        protected Start(CastingSkillInstance<?, ?, ?> instance) {
            super(instance);
        }

        //posted after the cast was accepted by tryCast but before it starts, cancel to stop the cast
        public static final class Pre extends Start implements ICancellableEvent {
            public Pre(CastingSkillInstance<?, ?, ?> instance) {
                super(instance);
            }
        }

        //posted after startCast, an instant skill has already ended at this point
        public static final class Post extends Start {
            public Post(CastingSkillInstance<?, ?, ?> instance) {
                super(instance);
            }
        }
    }

    public static final class PhaseChanged extends SkillCastEvent {
        private final @Nullable Identifier previousPhase;

        public PhaseChanged(CastingSkillInstance<?, ?, ?> instance, @Nullable Identifier previousPhase) {
            super(instance);
            this.previousPhase = previousPhase;
        }

        public @Nullable Identifier getPreviousPhase() {
            return previousPhase;
        }

        public @Nullable Identifier getPhase() {
            return getInstance().getPhase();
        }
    }

    //posted after endCast
    public static final class End extends SkillCastEvent {
        public End(CastingSkillInstance<?, ?, ?> instance) {
            super(instance);
        }

        public CastStatus.@Nullable Reason getReason() {
            return getInstance().getStatus().getReason();
        }
    }
}
