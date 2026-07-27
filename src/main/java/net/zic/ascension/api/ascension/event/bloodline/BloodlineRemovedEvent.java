package net.zic.ascension.api.ascension.event.bloodline;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class BloodlineRemovedEvent extends BloodlineAddedEvent{
    protected BloodlineRemovedEvent(Identifier bloodline, BloodlineData data, OriginSource source) {
        super(bloodline, data, source);
    }

    public static class Pre extends BloodlineRemovedEvent implements ICancellableEvent {


        public Pre(Identifier bloodline, BloodlineData data, OriginSource source) {
            super(bloodline, data, source);
        }
    }
    public static class Post extends BloodlineRemovedEvent{


        public Post(Identifier bloodline, BloodlineData data, OriginSource source) {
            super(bloodline, data, source);
        }
    }
}
