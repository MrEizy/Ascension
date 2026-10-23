package net.zic.ascension.api.ascension.core.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A technique as defined in the technique registry.
 * Holds the configuration and the logic, per-origin state belongs in {@link TechniqueData}.
 * Techniques held by an origin are always accessed through a {@link TechniqueInstance}, which pairs the technique with its typed data.
 * @param <D> the data this technique creates and runs on
 */
public interface Technique<D extends TechniqueData> {

    TechniqueType getType();

    //data is null when the technique is displayed without an owner, e.g. on a technique manual
    Component getName(@Nullable D data);
    Component getDescription(@Nullable D data);

    /**
     * called when the technique is added to an origin source
     * @param source the origin source it is being added to
     * @param instance the instance being added
     */
    void onAdded(OriginSource source, TechniqueInstance<D> instance);

    /**
     * Called when the technique is removed from a source
     * @param source the source it is removed from
     * @param instance the instance being removed
     */
    void onRemoved(OriginSource source, TechniqueInstance<D> instance);

    //called when an entity that owns an origin detects the technique was added
    default void applyToEntity(LivingEntity entity, TechniqueInstance<D> instance) {
    }

    //called when either an entity is detached from an origin or the technique is removed from the origin
    default void removeFromEntity(LivingEntity entity, TechniqueInstance<D> instance) {
    }

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
