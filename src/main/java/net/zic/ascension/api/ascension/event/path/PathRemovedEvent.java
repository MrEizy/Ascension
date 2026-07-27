package net.zic.ascension.api.ascension.event.path;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public class PathRemovedEvent extends PathEvent{
    protected PathRemovedEvent(Identifier path, PathData data, OriginSource source) {
        super(path, data, source);
    }
    public static class Pre extends PathRemovedEvent implements ICancellableEvent {

        public Pre(Identifier path, PathData data, OriginSource source) {
            super(path, data, source);
        }
    }
    public static class Post extends PathRemovedEvent{

        public Post(Identifier path, PathData data, OriginSource source) {
            super(path, data, source);
        }
    }
}
