package net.zic.ascension.api.ascension.core.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealmDefinition;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * A path as defined in the path registry.
 * Holds the realm definitions and the progression logic, per-origin state belongs in {@link PathData}.
 * Paths held by an origin are always accessed through a {@link PathInstance}, which pairs the path with its typed data.
 * @param <D> the data this path creates and runs on
 */
public interface Path<D extends PathData> {

    PathType getType();

    //data is null when the path is displayed without an owner
    Component getName(@Nullable D data);
    Component getDescription(@Nullable D data);

    default Identifier getId(RegistryAccess access){
        return CoreRegistries.PATH_REGISTRY.get(access).getKey(this);
    }
    //──Realms────────────────────────────────────────────────────────
    int getMaxMajorRealm();
    int getMaxMinorRealm(int majorRealm);

    Component getMajorRealmName(int majorRealm);
    Component getMinorRealmName(int majorRealm,int minorRealm);
    Component getRealmName(int majorRealm,int minorRealm);

    CompositeRealmDefinition getRealmDefinition(int majorRealm);

    double getMaxProgress(int majorRealm,int minorRealm);



    //id of the tribulation in the tribulation registry, null if there is none
    @Nullable Identifier getTribulationId(int majorRealm, int minorRealm);
    @Nullable Tribulation<?> getTribulation(int majorRealm, int minorRealm, RegistryAccess access);
    boolean hasTribulation(int majorRealm, int minorRealm);

    //──Path Interactions────────────────────────────────────────────────────────

    // methods marked with source treat this path as source, and opposite for those marked with target


    boolean hasSourceInteraction(Identifier target,RegistryAccess access);
    boolean hasSourceInteraction(Identifier target,PathInteractionType type,RegistryAccess access);
    boolean hasTargetInteraction(Identifier source,RegistryAccess access);
    boolean hasTargetInteraction(Identifier source,PathInteractionType type,RegistryAccess access);

    PathInteraction getSourceInteraction(Identifier target,RegistryAccess access);
    PathInteraction getTargetInteraction(Identifier source,RegistryAccess access);

    Collection<PathInteraction> getAllSourceInteractions(RegistryAccess access);
    Collection<PathInteraction> getAllTargetInteractions(RegistryAccess access);

    //──Progression────────────────────────────────────────────────────────

    /**
     * progresses the path by amount
     * @param source the origin source this path is attached too
     * @param instance the instance being progressed
     * @param amount the amount we are progressing by
     * @param entitySource the entity that caused the path to progress
     */
    void progress(OriginSource source, PathInstance<D> instance, double amount, LivingEntity entitySource);

    double getMaxProgress(D data);

    //while they may be "opposite" that is only in our implementation, and there could be scenarios
    //where progress is blocked but they cannot progress
    boolean canProgress(D data);
    boolean canBreakthrough(D data);

    int getMaxMinorRealm(D data, int majorRealm);
    Realm getPreviousRealm(D data);
    Realm getNextRealm(D data);

    //moves the instance to newRealm, running the realm logic of every realm it passes through
    void changeRealm(OriginSource source, PathInstance<D> instance, Realm newRealm);

    //──Data Simulation────────────────────────────────────────────────────────
    //caches the current state then simulates applying it
    void simulateProgression(OriginSource source, PathInstance<D> instance);

    //removes it from a specific source while maintaining data
    void removeFromSource(OriginSource source, PathInstance<D> instance);

    //──Data────────────────────────────────────────────────────────

    //creates fresh data, also used as the target when decoding
    D newData(RegistryAccess access);

    //saves and loads the data, read is expected to return new data
    SerializerHandler<D> serializerHandler();

    /**
     * syncs the data to clients. decode writes into existing data, so always call {@link #newData} first and decode into that.
     * <p>
     * Do not use {@link SyncHandler.StreamCodecSyncHandler}, its decode does nothing.
     */
    SyncHandler<D> syncHandler();
}
