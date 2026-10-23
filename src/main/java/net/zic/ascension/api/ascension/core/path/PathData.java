package net.zic.ascension.api.ascension.core.path;

import net.zic.ascension.api.ascension.core.path.realm.Realm;

/**
 * per-origin state of a path. Saving and syncing is handled by the owning {@link Path}
 * through its {@link Path#serializerHandler()} and {@link Path#syncHandler()}.
 * Every path tracks a realm and progress towards the next one, everything that depends on the
 * path's definitions (max progress, next realm etc.) lives on the {@link Path}.
 */
public interface PathData {
    double getProgress();

    //-1 when no realm is held
    int getCurrentMajorRealm();
    //0 when no realm is held
    int getCurrentMinorRealm();
    Realm getCurrentRealm();
}
