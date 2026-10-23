package net.zic.ascension.impl.core.path.simple;

import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.impl.core.path.CompletedTribulation;
import net.zic.ascension.impl.core.path.realms.MajorRealm;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * the state of a {@link SimplePath}. Only SimplePath may modify it, everything outside the package only gets read access
 */
public final class SimplePathData implements PathData {

    //one entry per major realm reached, the last one is the current major realm
    final List<MajorRealm> realms = new ArrayList<>();
    //realms held while simulating progression
    final List<MajorRealm> cachedRealms = new ArrayList<>();

    final Map<Realm, CompletedTribulation<?>> completedTribulations = new HashMap<>();

    @Nullable UUID activeTribulationId;

    double progress; //TODO consider updating this to be a resource that handles multiple "elements"

    SimplePathData() {
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public int getCurrentMajorRealm() {
        return realms.size()-1;
    }

    @Override
    public int getCurrentMinorRealm() {
        return realms.isEmpty() ? 0 : realms.getLast().getCurrentRealm();
    }

    @Override
    public Realm getCurrentRealm() {
        return getCurrentMajorRealm() < 0 ? Realm.of(-1,0) : Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm());
    }

    public boolean isLimitBroken(){
        if(getCurrentMajorRealm() < 0) return false;
        return realms.getLast().isLimitBroken();
    }

    //the id of the last breakthrough tribulation started, use SimplePath#isBreakingThrough to check it is still running
    public @Nullable UUID getActiveTribulationId(){
        return activeTribulationId;
    }
}
