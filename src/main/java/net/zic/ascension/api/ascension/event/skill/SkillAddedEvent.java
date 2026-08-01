package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class SkillAddedEvent extends SkillEvent{

    protected SkillAddedEvent(OriginSource source, Identifier skill, SkillData skillData) {
        super(source, skill, skillData);

    }


    public static class Pre extends SkillAddedEvent implements ICancellableEvent{

        public Pre(OriginSource source, Identifier skill, SkillData skillData) {
            super(source, skill, skillData);
        }
    }
    public static class Post extends SkillAddedEvent {

        public Post(OriginSource source, Identifier skill, SkillData skillData) {
            super(source, skill, skillData);
        }
    }
}
