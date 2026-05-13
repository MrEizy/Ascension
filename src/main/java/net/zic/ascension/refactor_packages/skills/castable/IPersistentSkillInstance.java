package net.zic.ascension.refactor_packages.skills.castable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.zic.ascension.refactor_packages.util.IDataInstance;

public interface IPersistentSkillInstance extends IDataInstance {

    boolean continueCasting(int ticksElapsed, Entity entity, ResourceLocation skill);
}
