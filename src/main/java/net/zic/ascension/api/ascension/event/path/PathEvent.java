package net.zic.ascension.api.ascension.event.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class PathEvent extends Event {
    private final Identifier path;
    private final PathData data;
    private final OriginSource source;

    protected PathEvent(Identifier path, PathData data, OriginSource source) {
        this.path = path;
        this.data = data;
        this.source = source;
    }

    public Identifier getPathIdentifier() {
        return path;
    }

    public Path getPath(RegistryAccess access) {
        return CoreRegistries.PATH_REGISTRY.get(access).getValue(path);
    }

    public PathData getPathData() {
        return data;
    }

    public OriginSource getSource() {
        return source;
    }

    public abstract static class Added extends PathEvent {
        protected Added(Identifier path, PathData data, OriginSource source) {
            super(path, data, source);
        }

        public static final class Pre extends Added implements ICancellableEvent {
            public Pre(Identifier path, PathData data, OriginSource source) {
                super(path, data, source);
            }
        }

        public static final class Post extends Added {
            public Post(Identifier path, PathData data, OriginSource source) {
                super(path, data, source);
            }
        }
    }

    public abstract static class Removed extends PathEvent {
        protected Removed(Identifier path, PathData data, OriginSource source) {
            super(path, data, source);
        }

        public static final class Pre extends Removed implements ICancellableEvent {
            public Pre(Identifier path, PathData data, OriginSource source) {
                super(path, data, source);
            }
        }

        public static final class Post extends Removed {
            public Post(Identifier path, PathData data, OriginSource source) {
                super(path, data, source);
            }
        }
    }
}
