package net.zic.ascension.skill_casting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.core.skill.castable.CastData;
import net.zic.ascension.api.core.skill.castable.PreCastData;

public class SkillCastEvent extends Event {
    private final LivingEntity entity;
    private final Identifier skill;
    private final PreCastData preCastData;

    public SkillCastEvent(LivingEntity entity, Identifier skill, PreCastData preCastData) {
        this.entity = entity;
        this.skill = skill;
        this.preCastData = preCastData;
    }
    public LivingEntity getEntity(){return entity;}

    public Identifier getSkill() {
        return skill;
    }

    public PreCastData getPreCastData() {
        return preCastData;
    }
}
