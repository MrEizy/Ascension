package net.zic.ascension.impl.core.tribulation;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;

import java.util.UUID;

public class LightningTribulationData implements TribulationData {
    private final UUID id;
    private int survived;
    private int ticks;
    private Phase phase;
    //ticks spent in the current phase
    private int phaseTicks;
    //set when the target died, the tribulation fails once the storm has dissipated
    private boolean failing;

    public LightningTribulationData(int survived, int ticks, UUID id) {
        this(survived, ticks, id, Phase.FORMING, 0, false);
    }

    public LightningTribulationData(int survived, int ticks, UUID id, Phase phase, int phaseTicks, boolean failing) {
        this.survived = survived;
        this.ticks = ticks;
        this.id = id;
        this.phase = phase;
        this.phaseTicks = phaseTicks;
        this.failing = failing;
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

    public Phase getPhase() {return phase;}
    public int getPhaseTicks() {return phaseTicks;}
    public boolean isFailing() {return failing;}

    public void setPhase(Phase phase) {
        this.phase = phase;
        this.phaseTicks = 0;
    }

    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    //counts a tick of the current phase, true once it has lasted the given number of ticks
    public boolean tickPhase(int phaseLength) {
        phaseTicks++;
        return phaseTicks >= phaseLength;
    }

    //the configuration may have shortened a phase since this was saved
    public void clampPhaseTicks(int phaseLength) {
        phaseTicks = Math.min(phaseTicks, phaseLength);
    }

    /**
     * how far through the current transition the storm is, 0 to 1, before any easing. 1 while active.
     * @param ticksSinceSync ticks the client counted since this data arrived, the server only sends it when the phase changes
     */
    public float transitionProgress(int formTicks, int dissipateTicks, float ticksSinceSync) {
        float ticks = phaseTicks + ticksSinceSync;
        return switch (phase) {
            case FORMING -> formTicks <= 0 ? 1.0F : Mth.clamp(ticks / formTicks, 0.0F, 1.0F);
            case ACTIVE -> 1.0F;
            case DISSIPATING -> dissipateTicks <= 0 ? 1.0F : Mth.clamp(ticks / dissipateTicks, 0.0F, 1.0F);
        };
    }

    @Override
    public UUID getUUID() {
        return id;
    }

    @Override
    public TribulationType<?> getType() {
        return AscensionTribulationTypes.LIGHTNING_TRIBULATION.get();
    }

    //the storm gathers, strikes, then breaks up before the tribulation resolves
    public enum Phase implements StringRepresentable {
        FORMING("forming"),
        ACTIVE("active"),
        DISSIPATING("dissipating");

        public static final Codec<Phase> CODEC = StringRepresentable.fromEnum(Phase::values);
        private final String name;

        Phase(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
