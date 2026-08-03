package net.zic.ascension.api.ascension.event.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;
import net.zic.ascension.api.ascension.core.effect.SkillEffectRemovalReason;

import java.util.UUID;

public abstract class SkillEffectEvent extends Event {
    private final LivingEntity entity;
    private final Identifier definition;

    protected SkillEffectEvent(LivingEntity entity, Identifier definition) {
        this.entity = entity;
        this.definition = definition;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public Identifier getDefinition() {
        return definition;
    }

    public static final class Apply extends SkillEffectEvent implements ICancellableEvent {
        private final UUID sourceEntity;
        private final Identifier sourceSkill;
        private final int duration;
        private final double potency;

        public Apply(
                LivingEntity entity,
                Identifier definition,
                UUID sourceEntity,
                Identifier sourceSkill,
                int duration,
                double potency
        ) {
            super(entity, definition);
            this.sourceEntity = sourceEntity;
            this.sourceSkill = sourceSkill;
            this.duration = duration;
            this.potency = potency;
        }

        public UUID getSourceEntity() {
            return sourceEntity;
        }

        public Identifier getSourceSkill() {
            return sourceSkill;
        }

        public int getDuration() {
            return duration;
        }

        public double getPotency() {
            return potency;
        }
    }

    public static final class Updated extends SkillEffectEvent {
        private final SkillEffectContext context;

        public Updated(LivingEntity entity, SkillEffectContext context) {
            super(entity, context.definition());
            this.context = context;
        }

        public SkillEffectContext getContext() {
            return context;
        }
    }

    public static final class Removed extends SkillEffectEvent {
        private final SkillEffectContext context;
        private final SkillEffectRemovalReason reason;

        public Removed(LivingEntity entity, SkillEffectContext context, SkillEffectRemovalReason reason) {
            super(entity, context.definition());
            this.context = context;
            this.reason = reason;
        }

        public SkillEffectContext getContext() {
            return context;
        }

        public SkillEffectRemovalReason getReason() {
            return reason;
        }
    }
}
