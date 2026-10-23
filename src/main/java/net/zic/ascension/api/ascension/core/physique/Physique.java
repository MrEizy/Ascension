package net.zic.ascension.api.ascension.core.physique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A physique as defined in the physique registry.
 * Holds the configuration and the logic, per-origin state belongs in {@link PhysiqueData}.
 * The physique held by an origin is always accessed through a {@link PhysiqueInstance}, which pairs the physique with its typed data.
 * @param <D> the data this physique creates and runs on
 */
public interface Physique<D extends PhysiqueData> {


    PhysiqueType getType();

    //data is null when the physique is displayed without an owner, e.g. on a physique essence
    Component getName(@Nullable D data);

    Component getDescription(@Nullable D data);

    /**
     * called when the physique is added to an origin source
     * @param source the origin source it is being added to
     * @param instance the instance being added
     * @return a collection of paths this physique wants to try and add
     */
    Collection<Identifier> onAdded(OriginSource source, PhysiqueInstance<D> instance);

    /**
     * Called when the physique is removed from a source
     * @param source the source it is removed from
     * @param instance the instance being removed
     * @return a collection of paths this physique wants to try and remove
     */
    Collection<Identifier> onRemoved(OriginSource source, PhysiqueInstance<D> instance);

    //called when an entity that owns an origin detects the physique was changed
    void applyToEntity(LivingEntity entity, PhysiqueInstance<D> instance);

    //called when either an entity is detached from an origin or the physique is removed from the origin
    void removeFromEntity(LivingEntity entity, PhysiqueInstance<D> instance);

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

    default Collection<Identifier> unlockedPaths() {return List.of();}

    default RequirementHolder requirements() {
        return RequirementHolder.EMPTY;
    }
    default Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return Optional.empty();
    }
}
