package net.zic.ascension.api.event.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.event.EventReason;

public abstract class SkillAddedEvent extends SkillEvent{
    private final EventReason reason;

    protected SkillAddedEvent(OriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
        super(source, skill, skillData);
        this.reason = reason;
    }

    public EventReason getReason(){
        return reason;
    }


    public static class Pre extends SkillAddedEvent implements ICancellableEvent{

        public Pre(OriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
            super(source, skill, skillData, reason);
        }
    }
    public static class Post extends SkillAddedEvent {

        public Post(OriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
            super(source, skill, skillData, reason);
        }
    }
}
