package net.zic.ascension.api.ascension.event.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class PathEvent extends Event {
    private final Identifier path;
    private final PathInstance data;
    private final OriginSource source;

    protected PathEvent(Identifier path, PathInstance data, OriginSource source) {
        this.path = path;
        this.data = data;
        this.source = source;
    }

    public Identifier getPathIdentifier() {
        return path;
    }
    public Path getPath(RegistryAccess access){
        return CoreRegistries.PATH_REGISTRY.get(access).getValue(path);
    }

    public PathInstance getPathInstance(){
        return data;
    }
    public OriginSource getSource(){
        return source;
    }
}
