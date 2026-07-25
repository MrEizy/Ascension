package net.zic.ascension.api.ascension.event.skill;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;

public abstract class SkillEvent extends Event {
    private final AscensionOriginSource source;
    private final Identifier skill;
    private final SkillData skillData;

    protected SkillEvent(AscensionOriginSource source, Identifier skill, SkillData skillData) {
        this.source = source;
        this.skill = skill;
        this.skillData = skillData;
    }


    public AscensionOriginSource getSource(){
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
