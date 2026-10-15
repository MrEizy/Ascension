package net.zic.ascension.impl.core.tribulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.tribulation.EntityTargetingTribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.core.tribulation.client.TribulationRenderer;
import net.zic.ascension.client.renderer.tribulation.TribulationCloudRenderer;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import net.zic.ascension.impl.datapack.tribulation.LightningTribulationType;

import java.util.UUID;

//strikes the target with lightning a set number of times, waits while the target is not loaded
public class LightningTribulation extends EntityTargetingTribulation<LightningTribulationData> {
    private final int strikes;
    private final double damage;
    private final int tickDelay;
    private final TribulationCloudSettings clouds;
    private TribulationRenderer<LightningTribulationData> renderer;

    public LightningTribulation(int strikes, double damage, int tickDelay, TribulationCloudSettings clouds) {
        this.strikes = strikes;
        this.damage = damage;
        this.tickDelay = tickDelay;
        this.clouds = clouds;
    }

    public int strikes() {return strikes;}
    public double damage() {return damage;}
    public int tickDelay() {return tickDelay;}
    public TribulationCloudSettings clouds() {return clouds;}

    @Override
    public LightningTribulationType getType() {
        return AscensionTribulationTypes.LIGHTNING_TRIBULATION.get();
    }

    @Override
    public LightningTribulationData newData() {
        return new LightningTribulationData(0, 0, UUID.randomUUID());
    }

    @Override
    protected void tickWithTarget(TribulationInstance<LightningTribulationData> instance, ServerLevel level, LivingEntity target) {
        LightningTribulationData data = instance.getData();

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
    public LightningTribulationData validateLoadedData(LightningTribulationData data) {
        //the number of strikes may have been lowered since it was saved
        data.setLightningSurvived(Math.min(data.getLightningSurvived(), strikes));
        return data;
    }

    @Override
    public TribulationRenderer<LightningTribulationData> getRenderer() {
        if (renderer == null) renderer = new TribulationCloudRenderer<>(clouds, LightningTribulationData::getLightningSurvived);
        return renderer;
    }
}
