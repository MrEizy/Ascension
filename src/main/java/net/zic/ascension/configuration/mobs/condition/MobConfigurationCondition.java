package net.zic.ascension.configuration.mobs.condition;

import net.minecraft.world.entity.Mob;

public interface MobConfigurationCondition {
    MobConfigurationConditionType getType();


    boolean test(Mob mob);
}
