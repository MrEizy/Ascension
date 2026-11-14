package net.zic.ascension.api.ascension.core.golden_core;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.golden_core.GoldenCoreType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A golden core as defined in the golden core registry.
 * Holds the configuration and the logic, per-origin state belongs in {@link GoldenCoreData}.
 * A held golden core is always accessed through a {@link GoldenCoreInstance}, which pairs the core with its typed data,
 * the path it tracks and the realm it was acquired at.
 * <p>
 * an origin can hold multiple instances of the same golden core, so anything the core adds to the origin
 * should be keyed by {@link GoldenCoreInstance#getId()} or {@link GoldenCoreInstance#contextId()}, never only the core id
 * @param <D> the data this golden core creates and runs on
 */
public interface GoldenCore<D extends GoldenCoreData> {

    GoldenCoreType getType();

    //data is null when the core is displayed without an owner
    Component getName(@Nullable D data);

    Component getDescription(@Nullable D data);

    //called when the core is added to an origin source, before any realm actions are run
    void onAdded(OriginSource source, GoldenCoreInstance<D> instance);

    //called when the core is removed from an origin source, after all realm actions are rolled back
    void onRemoved(OriginSource source, GoldenCoreInstance<D> instance);

    /**
     * called once for every realm the tracked path enters or leaves above the acquisition realm
     * @param path the tracked path
     * @param realm the realm being entered (UP) or left (DOWN)
     * @param direction the direction
     */
    void onRealmChange(OriginSource source, GoldenCoreInstance<D> instance, PathInstance<?> path, Realm realm, ProgressDirection direction);

    //called when the core is added to the origin source, or a new entity holds the origin
    void applyToEntity(LivingEntity entity, GoldenCoreInstance<D> instance);

    //called when either an entity is detached from an origin or the core is removed from the origin
    void removeFromEntity(LivingEntity entity, GoldenCoreInstance<D> instance);

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

    default RequirementHolder requirements() {
        return RequirementHolder.EMPTY;
    }

    default Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return Optional.empty();
    }
}
