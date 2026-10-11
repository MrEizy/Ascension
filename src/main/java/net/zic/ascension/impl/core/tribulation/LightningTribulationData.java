package net.zic.ascension.impl.core.tribulation;

import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;

import java.util.UUID;

public class LightningTribulationData implements TribulationData {
    private final UUID id;
    private int survived;
    private int ticks;

    public LightningTribulationData(int survived, int ticks, UUID id) {
        this.survived = survived;
        this.ticks = ticks;
        this.id = id;
    }

    public boolean tick(int maxTicks){
        ticks++;
        if(ticks<maxTicks) return false;

        ticks = 0;
        return true;
    }

    public void setLightningSurvived(int number){
        this.survived = number;
    }
    public int getLightningSurvived(){
        return survived;
    }
    public int getTicks(){
        return ticks;
    }
    @Override
    public UUID getUUID() {
        return id;
    }

    @Override
    public TribulationType getType() {
        return AscensionTribulationTypes.LIGHTNING_TRIBULATION.get();
    }
}
