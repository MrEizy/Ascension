package net.zic.ascension.api.ascension.event.physique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class PhysiqueEvent extends Event {

    private final Identifier physique;
    private final PhysiqueData data;
    private final OriginSource source;


    protected PhysiqueEvent(Identifier physique, PhysiqueData data, OriginSource source) {
        this.physique = physique;
        this.data = data;
        this.source = source;
    }
    public Identifier getPhysiqueIdentifier(){
        return physique;
    }
    public Physique getPhysique(RegistryAccess access){
        return CoreRegistries.PHYSIQUE_REGISTRY.get(access).getValue(physique);
    }
    public PhysiqueData getPhysiqueData(){
        return data;
    }
    public OriginSource getSource(){
        return source;
    }

}
