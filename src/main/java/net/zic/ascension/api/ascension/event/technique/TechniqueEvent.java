package net.zic.ascension.api.ascension.event.technique;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.TechniqueInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class TechniqueEvent extends Event {
    private final OriginSource source;
    private final TechniqueInstance<?> instance;

    protected TechniqueEvent(OriginSource source, TechniqueInstance<?> instance) {
        this.source = source;
        this.instance = instance;
    }

    public OriginSource getSource() {
        return source;
    }

    public TechniqueInstance<?> getTechniqueInstance() {
        return instance;
    }

    public Identifier getTechniqueIdentifier() {
        return instance.getTechniqueId();
    }

    public Technique<?> getTechnique() {
        return instance.getTechnique();
    }

    public TechniqueData getTechniqueData() {
        return instance.getData();
    }

    public abstract static class Added extends TechniqueEvent {
        protected Added(OriginSource source, TechniqueInstance<?> instance) {
            super(source, instance);
        }

        public static final class Pre extends Added implements ICancellableEvent {
            public Pre(OriginSource source, TechniqueInstance<?> instance) {
                super(source, instance);
            }
        }

        public static final class Post extends Added {
            public Post(OriginSource source, TechniqueInstance<?> instance) {
                super(source, instance);
            }
        }
    }

    public abstract static class Removed extends TechniqueEvent {
        protected Removed(OriginSource source, TechniqueInstance<?> instance) {
            super(source, instance);
        }

        public static final class Pre extends Removed implements ICancellableEvent {
            public Pre(OriginSource source, TechniqueInstance<?> instance) {
                super(source, instance);
            }
        }

        public static final class Post extends Removed {
            public Post(OriginSource source, TechniqueInstance<?> instance) {
                super(source, instance);
            }
        }
    }
}
