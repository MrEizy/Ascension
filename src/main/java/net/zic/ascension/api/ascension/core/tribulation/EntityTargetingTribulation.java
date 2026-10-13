package net.zic.ascension.api.ascension.core.tribulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Helper for tribulations that track an entity.
 * Splits ticking into ticks where the target is loaded, and ticks where it is not.
 * The tribulation still ticks while its target is unloaded (as long as the chunk it is in is loaded)
 */
public abstract class EntityTargetingTribulation<T extends TribulationData> implements Tribulation<T> {

    @Override
    public final void tick(TribulationInstance<T> instance, ServerLevel level) {
        LivingEntity target = instance.getTargetEntity();
        if (target == null || target.isRemoved()) {
            tickWithoutTarget(instance, level);
            return;
        }
        tickWithTarget(instance, level, target);
    }

    protected abstract void tickWithTarget(TribulationInstance<T> instance, ServerLevel level, LivingEntity target);

    //by default nothing happens while the target is missing
    protected void tickWithoutTarget(TribulationInstance<T> instance, ServerLevel level) {}
}
