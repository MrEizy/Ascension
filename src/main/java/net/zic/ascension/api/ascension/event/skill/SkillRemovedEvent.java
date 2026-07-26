package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.event.EventReason;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public class SkillRemovedEvent extends SkillEvent{

    protected SkillRemovedEvent(OriginSource source, Identifier skill, SkillData skillData) {
        super(source, skill, skillData);

    }


    public static class Pre extends SkillRemovedEvent implements ICancellableEvent {

        public Pre(OriginSource source, Identifier skill, SkillData skillData) {
            super(source, skill, skillData);
        }
    }
    public static class Post extends SkillRemovedEvent {

        public Post(OriginSource source, Identifier skill, SkillData skillData) {
            super(source, skill, skillData);
        }
    }
}


