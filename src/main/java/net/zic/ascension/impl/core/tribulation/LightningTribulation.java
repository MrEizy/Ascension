package net.zic.ascension.impl.core.tribulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.tribulation.EntityTargetingTribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

//strikes the target with lightning a set number of times, waits while the target is not loaded
public class LightningTribulation extends EntityTargetingTribulation {
    private final int strikes;
    private final double damage;
    private final int tickDelay;

    public LightningTribulation(int strikes, double damage, int tickDelay) {
        this.strikes = strikes;
        this.damage = damage;
        this.tickDelay = tickDelay;
    }

    public int strikes() {return strikes;}
    public double damage() {return damage;}
    public int tickDelay() {return tickDelay;}

    @Override
    public TribulationType getType() {
        return AscensionTribulationTypes.LIGHTNING_TRIBULATION.get();
    }

    @Override
    public TribulationData newData() {
        return new LightningTribulationData(0, 0, UUID.randomUUID());
    }

    @Override
    protected void tickWithTarget(TribulationInstance instance, ServerLevel level, LivingEntity target) {
        if (!(instance.getData() instanceof LightningTribulationData data)) return;

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
    public @Nullable TribulationData validateLoadedData(TribulationData data) {
        if (!(data instanceof LightningTribulationData lightningData)) return null;
        //the number of strikes may have been lowered since it was saved
        lightningData.setLightningSurvived(Math.min(lightningData.getLightningSurvived(), strikes));
        return lightningData;
    }
}
