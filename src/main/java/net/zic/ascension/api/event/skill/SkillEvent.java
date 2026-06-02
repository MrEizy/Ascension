package net.zic.ascension.api.event.skill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.source.OriginSource;

public abstract class SkillEvent extends Event {
    private final OriginSource source;
    private final Identifier skill;
    private final SkillData skillData;

    protected SkillEvent(OriginSource source, Identifier skill, SkillData skillData) {
        this.source = source;
        this.skill = skill;
        this.skillData = skillData;
    }


    public OriginSource getSource(){
        return source;
    }
    public Identifier getSkill(){
        return skill;
    }
    public SkillData getSkillData(){
        return skillData;
    }
    public Skill getSkill(RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,getSkill(),access);
    }
}
