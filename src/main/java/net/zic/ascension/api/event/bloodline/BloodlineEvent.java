package net.zic.ascension.api.event.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;

public abstract class BloodlineEvent extends Event {

    private final Identifier bloodline;
    private final BloodlineData data;
    private final OriginSource source;

    protected BloodlineEvent(Identifier bloodline, BloodlineData data, OriginSource source) {
        this.bloodline = bloodline;
        this.data = data;
        this.source = source;
    }

    public Identifier getBloodlineIdentifier(){
        return bloodline;
    }
    public Bloodline getBloodline(RegistryAccess access){
        return CoreRegistries.BLOODLINE_REGISTRY.get(access).getValue(bloodline);
    }
    public BloodlineData getBloodlineData(){
        return data;
    }

    public OriginSource getSource(){
        return source;
    }


}
