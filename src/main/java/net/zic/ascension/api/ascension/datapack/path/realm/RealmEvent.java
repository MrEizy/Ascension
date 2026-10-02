package net.zic.ascension.api.ascension.datapack.path.realm;

import net.neoforged.bus.api.Event;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public abstract class RealmEvent extends Event {

    private final OriginSource source;
    private final Realm realm;

    public RealmEvent(OriginSource source, Realm realm) {
        this.source = source;
        this.realm = realm;
    }

    public Realm getRealm() {
        return realm;
    }
    public OriginSource getSource(){
        return source;
    }
}
