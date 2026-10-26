package net.zic.ascension.impl.core.tribulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.tribulation.EntityTargetingTribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;

/**
 * strikes the target with lightning a set number of times, waits while the target is not loaded.
 * shared by every lightning style tribulation, subclasses provide the type and data
 * @param <D> the data, extending the lightning data so the strike logic can run on it
 */
public abstract class AbstractLightningTribulation<D extends LightningTribulationData> extends EntityTargetingTribulation<D> {
    private final int strikes;
    private final double damage;
    private final int tickDelay;

    protected AbstractLightningTribulation(int strikes, double damage, int tickDelay) {
        this.strikes = strikes;
        this.damage = damage;
        this.tickDelay = tickDelay;
    }

    public int strikes() {return strikes;}
    public double damage() {return damage;}
    public int tickDelay() {return tickDelay;}

    @Override
    protected void tickWithTarget(TribulationInstance<D> instance, ServerLevel level, LivingEntity target) {
        D data = instance.getData();

        if (data.getLightningSurvived() >= strikes) {
            instance.succeed();
            return;
        }

        if (data.tick(tickDelay)) {
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
            bolt.setDamage((float) damage);
            bolt.setPos(target.position());
            level.addFreshEntity(bolt);

            data.setLightningSurvived(data.getLightningSurvived() + 1);
            instance.markDataDirty();
        }
    }

    @Override
    public D validateLoadedData(D data) {
        //the number of strikes may have been lowered since it was saved
        data.setLightningSurvived(Math.min(data.getLightningSurvived(), strikes));
        return data;
    }
}
