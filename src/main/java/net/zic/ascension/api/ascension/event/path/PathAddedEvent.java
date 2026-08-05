package net.zic.ascension.api.ascension.event.path;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class PathAddedEvent extends PathEvent{
    protected PathAddedEvent(Identifier path, PathInstance data, OriginSource source) {
        super(path, data, source);
    }
    public static class Pre extends PathAddedEvent implements ICancellableEvent{

        public Pre(Identifier path, PathInstance data, OriginSource source) {
            super(path, data, source);
        }
    }
    public static class Post extends PathAddedEvent{

        public Post(Identifier path, PathInstance data, OriginSource source) {
            super(path, data, source);
        }
    }
}
