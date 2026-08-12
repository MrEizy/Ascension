package net.zic.ascension.api.ascension.datapack.path.realm;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class PathRealmChangeEvent extends RealmEvent{
    Identifier path;
    PathInstance pathInstance;
    Realm oldRealm;
    public PathRealmChangeEvent(OriginSource source, Realm realm,Realm oldRealm, Identifier path, PathInstance pathInstance) {
        super(source, realm);
        this.path = path;
        this.oldRealm = oldRealm;
        this.pathInstance = pathInstance;
    }

    public Realm getOldRealm() {
        return oldRealm;
    }

    public Identifier getPathId() {
        return path;
    }
    public Path getPath(){
        return CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,getSource().getRegistryAccess());
    }

    public PathInstance getPathInstance() {
        return pathInstance;
    }

    public static class PathRealmUpEvent extends PathRealmChangeEvent {


        public PathRealmUpEvent(OriginSource source, Realm realm, Realm oldRealm, Identifier path, PathInstance pathInstance) {
            super(source, realm, oldRealm, path, pathInstance);
        }
    }

    public static class PathRealmDownEvent extends PathRealmChangeEvent {


        public PathRealmDownEvent(OriginSource source, Realm realm, Realm oldRealm, Identifier path, PathInstance pathInstance) {
            super(source, realm, oldRealm, path, pathInstance);
        }
    }
}
