package net.zic.ascension.impl.core.tribulation;

import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import net.zic.ascension.impl.datapack.tribulation.LightningTribulationType;

import java.util.UUID;

//strikes the target with lightning a set number of times, waits while the target is not loaded
public class LightningTribulation extends AbstractLightningTribulation<LightningTribulationData> {

    public LightningTribulation(int strikes, double damage, int tickDelay) {
        super(strikes, damage, tickDelay);
    }

    @Override
    public LightningTribulationType getType() {
        return AscensionTribulationTypes.LIGHTNING_TRIBULATION.get();
    }

    @Override
    public LightningTribulationData newData() {
        return new LightningTribulationData(0, 0, UUID.randomUUID());
    }
}
