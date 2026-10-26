package net.zic.ascension.api.ascension.event.golden_core;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCore;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreData;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.UUID;

public abstract class GoldenCoreEvent extends Event {
    private final OriginSource source;
    private final GoldenCoreInstance<?> instance;

    protected GoldenCoreEvent(OriginSource source, GoldenCoreInstance<?> instance) {
        this.source = source;
        this.instance = instance;
    }

    public OriginSource getSource() {
        return source;
    }

    public GoldenCoreInstance<?> getGoldenCoreInstance() {
        return instance;
    }

    public UUID getId() {
        return instance.getId();
    }

    public Identifier getGoldenCoreId() {
        return instance.getCoreId();
    }

    public GoldenCore<?> getGoldenCore() {
        return instance.getCore();
    }

    public GoldenCoreData getGoldenCoreData() {
        return instance.getData();
    }

    public abstract static class Added extends GoldenCoreEvent {
        protected Added(OriginSource source, GoldenCoreInstance<?> instance) {
            super(source, instance);
        }

        public static final class Pre extends Added implements ICancellableEvent {
            public Pre(OriginSource source, GoldenCoreInstance<?> instance) {
                super(source, instance);
            }
        }

        public static final class Post extends Added {
            public Post(OriginSource source, GoldenCoreInstance<?> instance) {
                super(source, instance);
            }
        }
    }

    public abstract static class Removed extends GoldenCoreEvent {
        protected Removed(OriginSource source, GoldenCoreInstance<?> instance) {
            super(source, instance);
        }

        public static final class Pre extends Removed implements ICancellableEvent {
            public Pre(OriginSource source, GoldenCoreInstance<?> instance) {
                super(source, instance);
            }
        }

        public static final class Post extends Removed {
            public Post(OriginSource source, GoldenCoreInstance<?> instance) {
                super(source, instance);
            }
        }
    }
}
