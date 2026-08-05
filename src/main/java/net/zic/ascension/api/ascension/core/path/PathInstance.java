package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public interface PathInstance {




    /**
     * progresses path by amount
     * @param path the path used to progress this path(can be different from this path)
     * @param amount the amount we are progressing by
     * @param source the origin source this path is attached too
     */
    void progressPath(Identifier path, double amount, OriginSource source);
    double getProgress();
    double getMaxProgress();

    //while they may be "opposite" that is only in our implementation, and there could be scenarios
    //where progress is blocked but they cannot progress

    boolean canProgress();
    boolean canBreakthrough();


    int getCurrentMajorRealm();
    int getCurrentMinorRealm();

    int getMaxMinorRealm(int realm);
    //──Tribulation────────────────────────────────────────────────────────
    //TODO:
    // for now I have not included tribulations. this is because they might not actually be needed?
    // or at least it is not a min requirement for paths to work

    //──Logic────────────────────────────────────────────────────────

    void onRealmUp(OriginSource source);
    void onRealmDown(OriginSource source);


    void handleRealmChange(Realm newRealm,OriginSource source);
    //──Data Simulation────────────────────────────────────────────────────────
    //caches the current state then simulates applying it
    void simulateProgression(OriginSource source);

    //removes it from a specific source while maintaining data
    void removeFromSource(OriginSource source);


    void write(ValueOutput output, RegistryAccess access);
    void encode(ByteBuf buf, RegistryAccess access);

}
