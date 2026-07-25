package net.zic.ascension.api.ascension.event.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;

public abstract class PathEvent extends Event {
    private final Identifier path;
    private final PathData data;
    private final AscensionOriginSource source;

    protected PathEvent(Identifier path, PathData data, AscensionOriginSource source) {
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

    public PathData getPathData(){
        return data;
    }
    public AscensionOriginSource getSource(){
        return source;
    }
}
