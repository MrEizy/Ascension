package net.zic.ascension.impl.core.tribulation;

import net.zic.ascension.api.core.tribulation.TribulationData;
import net.zic.ascension.api.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;

import java.util.UUID;

public class LightingTribulationData implements TribulationData {

    private int survived;

    public LightingTribulationData(int number){
        setLightningSurvived(number);
    }

    public void setLightningSurvived(int number){
        this.survived = number;

    }
    public int getLightningSurvived(){
        return survived;
    }
    @Override
    public UUID getUUID() {
        return UUID.randomUUID();
    }

    @Override
    public TribulationType getType() {
        return AscensionTribulationTypes.LIGHTNING_TRIBULATION.get();
    }
}
