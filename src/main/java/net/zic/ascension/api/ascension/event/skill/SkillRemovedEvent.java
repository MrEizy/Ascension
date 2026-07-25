package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.event.EventReason;

public class SkillRemovedEvent extends SkillEvent{
    private final EventReason reason;

    protected SkillRemovedEvent(AscensionOriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
        super(source, skill, skillData);
        this.reason = reason;
    }

    public EventReason getReason(){
        return reason;
    }

    public static class Pre extends SkillRemovedEvent implements ICancellableEvent {

        public Pre(AscensionOriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
            super(source, skill, skillData, reason);
        }
    }
    public static class Post extends SkillRemovedEvent {

        public Post(AscensionOriginSource source, Identifier skill, SkillData skillData, EventReason reason) {
            super(source, skill, skillData, reason);
        }
    }
}


