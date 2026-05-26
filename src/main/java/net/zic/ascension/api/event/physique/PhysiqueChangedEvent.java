package net.zic.ascension.api.event.physique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.event.EventReason;

/**
 * An event that is called when the physique on an origin source is changed
 *
 * is split into a pre event (can be cancelled)
 * and a post event (cannot be cancelled)
 *
 */
public abstract class PhysiqueChangedEvent extends PhysiqueEvent{
    private final Identifier newPhysique;
    private final PhysiqueData newPhysiqueData;
    private final EventReason reason;
    protected PhysiqueChangedEvent(Identifier physique, PhysiqueData data, Identifier newPhysique, PhysiqueData newPhysiqueData, OriginSource source, EventReason reason) {
        super(physique, data, source);
        this.newPhysique = newPhysique;
        this.newPhysiqueData = newPhysiqueData;
        this.reason = reason;
    }

    public Identifier getNewPhysiqueIdentifier(){
        return newPhysique;
    }
    public Physique getNewPhysique(RegistryAccess access){
        return CoreRegistries.PHYSIQUE_REGISTRY.get(access).getValue(newPhysique);
    }

    public PhysiqueData getNewPhysiqueData(){
        return newPhysiqueData;
    }
    public EventReason getReason(){
        return reason;
    }
    public static class Pre extends PhysiqueChangedEvent implements ICancellableEvent {


        public Pre(Identifier physique, PhysiqueData data, Identifier newPhysique, PhysiqueData newPhysiqueData, OriginSource source, EventReason reason) {
            super(physique, data, newPhysique, newPhysiqueData, source, reason);
        }
    }
    public static class Post extends PhysiqueChangedEvent {


        public Post(Identifier physique, PhysiqueData data, Identifier newPhysique, PhysiqueData newPhysiqueData, OriginSource source, EventReason reason) {
            super(physique, data, newPhysique, newPhysiqueData, source, reason);
        }
    }
}