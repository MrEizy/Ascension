package net.zic.ascension.api.ascension.event.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class TechniqueEvent extends Event {
    private final Identifier technique;
    private final TechniqueData data;
    private final OriginSource source;

    protected TechniqueEvent(Identifier technique, TechniqueData data, OriginSource source) {
        this.technique = technique;
        this.data = data;
        this.source = source;
    }

    public Identifier getTechniqueIdentifier() {
        return technique;
    }

    public Technique getTechnique(RegistryAccess access) {
        return CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, technique, access);
    }

    public TechniqueData getTechniqueData() {
        return data;
    }

    public OriginSource getSource() {
        return source;
    }

    public abstract static class Added extends TechniqueEvent {
        protected Added(Identifier technique, TechniqueData data, OriginSource source) {
            super(technique, data, source);
        }

        public static final class Pre extends Added implements ICancellableEvent {
            public Pre(Identifier technique, TechniqueData data, OriginSource source) {
                super(technique, data, source);
            }
        }

        public static final class Post extends Added {
            public Post(Identifier technique, TechniqueData data, OriginSource source) {
                super(technique, data, source);
            }
        }
    }

    public abstract static class Removed extends TechniqueEvent {
        protected Removed(Identifier technique, TechniqueData data, OriginSource source) {
            super(technique, data, source);
        }

        public static final class Pre extends Removed implements ICancellableEvent {
            public Pre(Identifier technique, TechniqueData data, OriginSource source) {
                super(technique, data, source);
            }
        }

        public static final class Post extends Removed {
            public Post(Identifier technique, TechniqueData data, OriginSource source) {
                super(technique, data, source);
            }
        }
    }
}
