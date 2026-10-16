package net.zic.ascension.impl.core.tribulation;

import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.core.tribulation.client.TribulationRenderer;
import net.zic.ascension.client.renderer.tribulation.WispyTribulationCloudRenderer;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import net.zic.ascension.impl.datapack.tribulation.V2TribulationType;

//a lightning tribulation that behaves the same, but its storm is a vortex of wispy translucent streaks instead of the pixel hurricane
public class V2LightningTribulation extends LightningTribulation {

    public V2LightningTribulation(int strikes, double damage, int tickDelay, TribulationCloudSettings clouds) {
        super(strikes, damage, tickDelay, clouds);
    }

    @Override
    public V2TribulationType getType() {
        return AscensionTribulationTypes.V2_TRIBULATION.get();
    }

    @Override
    protected TribulationRenderer<LightningTribulationData> createRenderer() {
        return new WispyTribulationCloudRenderer<>(clouds(), LightningTribulationData::getLightningSurvived, this::formation);
    }
}
