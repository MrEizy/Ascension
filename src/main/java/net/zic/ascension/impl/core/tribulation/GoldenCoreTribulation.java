package net.zic.ascension.impl.core.tribulation;

import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import net.zic.ascension.impl.datapack.tribulation.GoldenCoreTribulationType;

/**
 * a lightning tribulation that forms a golden core.
 * the core to form is set on the data before it starts, once completed the path holds the data and this tribulation
 * adds/removes the core as the realm is gained/lost, owning it through {@link GoldenCoreTribulationData#owner()}
 */
public class GoldenCoreTribulation extends AbstractLightningTribulation<GoldenCoreTribulationData> {

    public GoldenCoreTribulation(int strikes, double damage, int tickDelay) {
        super(strikes, damage, tickDelay);
    }

    @Override
    public GoldenCoreTribulationType getType() {
        return AscensionTribulationTypes.GOLDEN_CORE_TRIBULATION.get();
    }

    @Override
    public GoldenCoreTribulationData newData() {
        return GoldenCoreTribulationData.fresh();
    }

    @Override
    public void onAdded(OriginSource source, GoldenCoreTribulationData data) {
        //fresh data (a realm simulated without running the tribulation) has no core
        if (!data.hasGoldenCore()) return;
        AscensionOriginSourceHelper.addGoldenCore(source, data.getGoldenCoreId(), data.getGoldenCore(), data.getPath(),
                data.getAcquisitionRealm(), data.owner());
    }

    @Override
    public void onRemoved(OriginSource source, GoldenCoreTribulationData data) {
        if (!data.hasGoldenCore()) return;
        AscensionOriginSourceHelper.removeGoldenCore(source, data.getGoldenCoreId(), data.owner());
    }
}
