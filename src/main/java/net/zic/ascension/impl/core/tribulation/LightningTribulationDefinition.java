package net.zic.ascension.impl.core.tribulation;

import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;

public record LightningTribulationDefinition(int lightingStrikes, double damage, int tickDelay) implements TribulationDefinition {
    @Override
    public TribulationType getType() {
        return AscensionTribulationTypes.LIGHTNING_TRIBULATION.get();
    }
}
