package net.zic.ascension.api.ascension.event.bloodline;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class BloodlineEvent extends Event {
    private final OriginSource source;
    private final BloodlineInstance<?> instance;

    protected BloodlineEvent(OriginSource source, BloodlineInstance<?> instance) {
        this.source = source;
        this.instance = instance;
    }

    public OriginSource getSource() {
        return source;
    }

    public BloodlineInstance<?> getBloodlineInstance() {
        return instance;
    }

    public Identifier getBloodlineIdentifier() {
        return instance.getBloodlineId();
    }

    public Bloodline<?> getBloodline() {
        return instance.getBloodline();
    }

    public BloodlineData getBloodlineData() {
        return instance.getData();
    }

    public abstract static class Added extends BloodlineEvent {
        protected Added(OriginSource source, BloodlineInstance<?> instance) {
            super(source, instance);
        }

        public static final class Pre extends Added implements ICancellableEvent {
            public Pre(OriginSource source, BloodlineInstance<?> instance) {
                super(source, instance);
            }
        }

        public static final class Post extends Added {
            public Post(OriginSource source, BloodlineInstance<?> instance) {
                super(source, instance);
            }
        }
    }

    public abstract static class Removed extends BloodlineEvent {
        protected Removed(OriginSource source, BloodlineInstance<?> instance) {
            super(source, instance);
        }

        public static final class Pre extends Removed implements ICancellableEvent {
            public Pre(OriginSource source, BloodlineInstance<?> instance) {
                super(source, instance);
            }
        }

        public static final class Post extends Removed {
            public Post(OriginSource source, BloodlineInstance<?> instance) {
                super(source, instance);
            }
        }
    }
}
