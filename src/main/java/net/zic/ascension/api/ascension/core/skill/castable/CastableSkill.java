package net.zic.ascension.api.ascension.core.skill.castable;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastType;

public interface CastableSkill extends Skill {

    CastType getCastType();


    //called when added/removed from skill hot bar
    void onEquip(LivingEntity entity,PreCastData preCastData);
    void onUnEquip(LivingEntity entity, PreCastData preCastData);


    /*
        this is called when a skill is selected
        is called on both server and client so can be used for:
        buffs (if you want some special things to happen)
        initializing/cleaning up  HUD elements
        initializing/cleaning up keybinds (you might want them to only be available while skill is here
     */
    void selected(LivingEntity entity,PreCastData preCastData);
    void unselected(LivingEntity entity,PreCastData preCastData);


    /**
     * ran on both client and server
     * used to determine if a skill can be cast. main things you would do here
     * check cooldown
     * check and drain qi
     *
     * position checks + entity selected checks
     *
     * @param caster the entity casting this
     * @return if it should be cast or not
     */
    CastResult tryCast(LivingEntity caster);


    /**
     * called first after try cast.
     * if type == INSTANT this is the only thing called and should apply the cooldown
     * @param caster the entity casting the skill
     * @param preCastData the data before it was cast
     * @return the cast data to use
     */
    CastData initialCast(LivingEntity caster,PreCastData preCastData);

    /**
     *
     * @param caster the entity casting
     * @param castStatus the status of this casting instance
     * @param castData the data for this cast
     * @param ticksElapsed how many ticks since initialCast
     */
    void continueCasting(LivingEntity caster, CastStatus castStatus, CastData castData, int ticksElapsed);


    /**
     * called after continue casting is finished, for charge skills this would spawn the final entity
     * or other skills would use this to handle clean up
     * should apply cooldown here
     * @param caster the entity casting
     * @param status how/why casting has finished
     * @param castData the data for this cast
     */
    void finalCast(LivingEntity caster,CastStatus status,CastData castData,int ticksElapsed);


    CastData loadCastData(ByteBuf buf);
    PreCastData newPreCastData();
    PreCastData loadPreCastData(ValueInput input);
    PreCastData loadPreCastData(ByteBuf buf);

}
