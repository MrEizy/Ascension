package net.zic.ascension.api.ascension.event.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class BloodlineEvent extends Event {
    private final Identifier bloodline;
    private final BloodlineData data;
    private final OriginSource source;

    protected BloodlineEvent(Identifier bloodline, BloodlineData data, OriginSource source) {
        this.bloodline = bloodline;
        this.data = data;
        this.source = source;
    }

    public Identifier getBloodlineIdentifier() {
        return bloodline;
    }

    public Bloodline getBloodline(RegistryAccess access) {
        return CoreRegistries.BLOODLINE_REGISTRY.get(access).getValue(bloodline);
    }

    public BloodlineData getBloodlineData() {
        return data;
    }

    public OriginSource getSource() {
        return source;
    }

    public abstract static class Added extends BloodlineEvent {
        protected Added(Identifier bloodline, BloodlineData data, OriginSource source) {
            super(bloodline, data, source);
        }

        public static final class Pre extends Added implements ICancellableEvent {
            public Pre(Identifier bloodline, BloodlineData data, OriginSource source) {
                super(bloodline, data, source);
            }
        }

        public static final class Post extends Added {
            public Post(Identifier bloodline, BloodlineData data, OriginSource source) {
                super(bloodline, data, source);
            }
        }
    }

    public abstract static class Removed extends BloodlineEvent {
        protected Removed(Identifier bloodline, BloodlineData data, OriginSource source) {
            super(bloodline, data, source);
        }

        public static final class Pre extends Removed implements ICancellableEvent {
            public Pre(Identifier bloodline, BloodlineData data, OriginSource source) {
                super(bloodline, data, source);
            }
        }

        public static final class Post extends Removed {
            public Post(Identifier bloodline, BloodlineData data, OriginSource source) {
                super(bloodline, data, source);
            }
        }
    }
}
