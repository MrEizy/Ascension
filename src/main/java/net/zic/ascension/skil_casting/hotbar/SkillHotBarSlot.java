package net.zic.ascension.skil_casting.hotbar;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.skill.castable.PreCastData;

public class SkillHotBarSlot {
    protected Identifier skill;
    protected PreCastData preCastData;




    public void setSkill(LivingEntity entity, Identifier skill){
        if(skill == null){
            setSkill(null,null);
            return;
        }

        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,entity.level().registryAccess()) instanceof CastableSkill castableSkill)) return;

        setSkill(entity,skill,castableSkill.newPreCastData());

    }
    public void unslotSkill(LivingEntity entity){
        if(skill == null) return;
        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,entity.level().registryAccess()) instanceof CastableSkill castableSkill)) return;
        castableSkill.onUnEquip(
            entity,
            preCastData
        );

        skill = null;
        preCastData = null;

    }

    public void setSkill(LivingEntity entity,Identifier skill,PreCastData preCastData){
        unslotSkill(entity);
        if(skill == null) return;
        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,entity.level().registryAccess()) instanceof CastableSkill castableSkill)) return;

        this.skill = skill;
        this.preCastData = preCastData;

        castableSkill.onEquip(entity,preCastData);
    }

    public Identifier getSkill(){
        return skill;
    }
    public PreCastData getPreCastData(){
        return preCastData;
    }
}
