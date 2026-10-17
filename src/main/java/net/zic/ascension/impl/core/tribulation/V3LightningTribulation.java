package net.zic.ascension.impl.core.tribulation;

import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.core.tribulation.client.TribulationRenderer;
import net.zic.ascension.client.renderer.tribulation.MinecraftCloudTribulationRenderer;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import net.zic.ascension.impl.datapack.tribulation.V3TribulationType;

//a lightning tribulation that behaves the same, but its storm is made of minecraft clouds pulled in from around it
public class V3LightningTribulation extends LightningTribulation {

    public V3LightningTribulation(int strikes, double damage, int tickDelay, TribulationCloudSettings clouds) {
        super(strikes, damage, tickDelay, clouds);
    }

    @Override
    public V3TribulationType getType() {
        return AscensionTribulationTypes.V3_TRIBULATION.get();
    }

    @Override
    protected TribulationRenderer<LightningTribulationData> createRenderer() {
        return new MinecraftCloudTribulationRenderer<>(clouds(), LightningTribulationData::getLightningSurvived, this::formation);
    }
}
