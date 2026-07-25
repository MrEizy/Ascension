package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.event.EventReason;

public abstract class SkillAddedEvent extends SkillEvent{
    private final EventReason reason;

    protected SkillAddedEvent(AscensionOriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
        super(source, skill, skillData);
        this.reason = reason;
    }

    public EventReason getReason(){
        return reason;
    }


    public static class Pre extends SkillAddedEvent implements ICancellableEvent{

        public Pre(AscensionOriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
            super(source, skill, skillData, reason);
        }
    }
    public static class Post extends SkillAddedEvent {

        public Post(AscensionOriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
            super(source, skill, skillData, reason);
        }
    }
}
