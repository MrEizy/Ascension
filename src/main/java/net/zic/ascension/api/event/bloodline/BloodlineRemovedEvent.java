package net.zic.ascension.api.event.bloodline;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.event.EventReason;

public abstract class BloodlineRemovedEvent extends BloodlineAddedEvent{
    protected BloodlineRemovedEvent(Identifier bloodline, BloodlineData data, OriginSource source, EventReason reason) {
        super(bloodline, data, source, reason);
    }

    public static class Pre extends BloodlineRemovedEvent implements ICancellableEvent {


        public Pre(Identifier bloodline, BloodlineData data, OriginSource source, EventReason reason) {
            super(bloodline, data, source, reason);
        }
    }
    public static class Post extends BloodlineRemovedEvent{


        public Post(Identifier bloodline, BloodlineData data, OriginSource source, EventReason reason) {
            super(bloodline, data, source, reason);
        }
    }
}
