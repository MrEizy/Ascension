package net.zic.ascension.refactor_packages.skill_casting.persistent_casting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.zic.ascension.refactor_packages.skills.castable.IPersistentSkillInstance;
/*
    unlike casting instances where the player only has 1,
    this is dynamical so does not clean itself up
 */
public class PersistentCastingInstance {
    private final IPersistentSkillInstance castInstance;
    private final ResourceLocation skill;
    private int ticksElapsed; //how many ticks it has been since cast was started

    public PersistentCastingInstance(IPersistentSkillInstance castInstance, ResourceLocation skill){
        this.castInstance = castInstance;
        this.skill = skill;
    }

    public boolean tick(Entity entity) {
        ticksElapsed += 1;
        return castInstance.continueCasting(ticksElapsed,entity,skill);
    }
}
