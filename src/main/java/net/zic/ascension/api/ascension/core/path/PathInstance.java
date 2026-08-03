package net.zic.ascension.api.ascension.core.path;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.Collection;
import java.util.UUID;

public interface PathInstance {


    /**
     * progresses path by amount
     * @param path the path used to progress this path(can be different from this path)
     * @param amount the amount we are progressing by
     * @param source the origin source this path is attached too
     */
    void progressPath(Identifier path, double amount, OriginSource source);
    double getProgress();

    boolean canProgress();

    int getCurrentMajorRealm();
    int getCurrentMinorRealm();

    //──Tribulation────────────────────────────────────────────────────────
    //TODO:
    // for now I have not included tribulations. this is because they might not actually be needed?
    // or at least it is not a min requirement for paths to work

    //──Logic────────────────────────────────────────────────────────

    void onRealmUp(OriginSource source);
    void onRealmDown(OriginSource source);

    //──Data Simulation────────────────────────────────────────────────────────
    //caches the current state then simulates applying it
    void simulateProgression(OriginSource source);

    //removes it from a specific source while maintaining data
    void removeFromSource(OriginSource source);


}
