package net.zic.ascension.api.ascension.event.bloodline;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.event.EventReason;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class BloodlineAddedEvent extends BloodlineEvent{

    protected BloodlineAddedEvent(Identifier bloodline, BloodlineData data, OriginSource source) {
        super(bloodline, data, source);

    }



    public static class Pre extends BloodlineAddedEvent implements ICancellableEvent {


        public Pre(Identifier bloodline, BloodlineData data, OriginSource source) {
            super(bloodline, data, source);
        }
    }
    public static class Post extends BloodlineAddedEvent{


        public Post(Identifier bloodline, BloodlineData data, OriginSource source) {
            super(bloodline, data, source);
        }
    }
}
