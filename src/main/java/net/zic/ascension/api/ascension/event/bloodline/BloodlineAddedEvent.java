package net.zic.ascension.api.ascension.event.bloodline;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.event.EventReason;

public abstract class BloodlineAddedEvent extends BloodlineEvent{

    private final EventReason reason;

    protected BloodlineAddedEvent(Identifier bloodline, BloodlineData data, OriginSource source, EventReason reason) {
        super(bloodline, data, source);
        this.reason = reason;
    }

    public EventReason getReason(){
        return reason;
    }

    public static class Pre extends BloodlineAddedEvent implements ICancellableEvent {


        public Pre(Identifier bloodline, BloodlineData data, OriginSource source, EventReason reason) {
            super(bloodline, data, source, reason);
        }
    }
    public static class Post extends BloodlineAddedEvent{


        public Post(Identifier bloodline, BloodlineData data, OriginSource source, EventReason reason) {
            super(bloodline, data, source, reason);
        }
    }
}
