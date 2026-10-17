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

//gathers a storm, strikes the target with lightning a set number of times, then lets the storm break up before resolving.
//waits while the target is not loaded, except while the storm is breaking up
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

        switch (data.getPhase()) {
            case FORMING -> {
                //no strikes until the storm has gathered
                if (data.tickPhase(clouds.forming().ticks())) {
                    changePhase(instance, LightningTribulationData.Phase.ACTIVE);
                }
            }
            case ACTIVE -> tickStrikes(instance, level, target);
            case DISSIPATING -> tickDissipating(instance);
        }
    }

    //the storm keeps breaking up even when the target has gone, so the tribulation still resolves
    @Override
    protected void tickWithoutTarget(TribulationInstance<LightningTribulationData> instance, ServerLevel level) {
        if (instance.getData().getPhase() == LightningTribulationData.Phase.DISSIPATING) {
            tickDissipating(instance);
        }
    }

    //the storm breaks up before the failure resolves, instead of vanishing
    @Override
    public void onTargetDeath(TribulationInstance<LightningTribulationData> instance) {
        LightningTribulationData data = instance.getData();
        data.setFailing(true);
        if (data.getPhase() != LightningTribulationData.Phase.DISSIPATING) {
            changePhase(instance, LightningTribulationData.Phase.DISSIPATING);
        }
    }

    private void tickDissipating(TribulationInstance<LightningTribulationData> instance) {
        LightningTribulationData data = instance.getData();
        if (!data.tickPhase(clouds.dissipating().ticks())) return;
        if (data.isFailing()) {
            instance.fail();
        } else {
            instance.succeed();
        }
    }

    //the client counts the phase ticks itself, so the data only has to be sent when the phase changes
    private static void changePhase(TribulationInstance<LightningTribulationData> instance, LightningTribulationData.Phase phase) {
        instance.getData().setPhase(phase);
        instance.markDataDirty();
    }

    private void tickStrikes(TribulationInstance<LightningTribulationData> instance, ServerLevel level, LivingEntity target) {
        LightningTribulationData data = instance.getData();

        if (data.getLightningSurvived() >= strikes) {
            changePhase(instance, LightningTribulationData.Phase.DISSIPATING);
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
        //the number of strikes or the length of a phase may have been lowered since it was saved
        data.setLightningSurvived(Math.min(data.getLightningSurvived(), strikes));
        data.clampPhaseTicks(switch (data.getPhase()) {
            case FORMING -> clouds.forming().ticks();
            case ACTIVE -> 0;
            case DISSIPATING -> clouds.dissipating().ticks();
        });
        return data;
    }

    @Override
    public TribulationRenderer<LightningTribulationData> getRenderer() {
        if (renderer == null) {
            renderer = createRenderer();
        }
        return renderer;
    }

    //the storm drawn above the tribulation, created once on first use
    protected TribulationRenderer<LightningTribulationData> createRenderer() {
        return new TribulationCloudRenderer<>(clouds, LightningTribulationData::getLightningSurvived, this::formation);
    }

    //how far the storm has gathered, worked out from the synced phase
    protected TribulationCloudRenderer.Formation formation(LightningTribulationData data, float ticksSinceSync) {
        float progress = data.transitionProgress(clouds.forming().ticks(), clouds.dissipating().ticks(), ticksSinceSync);
        return switch (data.getPhase()) {
            case FORMING -> new TribulationCloudRenderer.Formation(progress, clouds.forming(), false);
            case ACTIVE -> TribulationCloudRenderer.Formation.FORMED;
            //dissipating plays its style backwards, from formed to clear sky
            case DISSIPATING -> new TribulationCloudRenderer.Formation(1.0F - progress, clouds.dissipating(), true);
        };
    }
}
