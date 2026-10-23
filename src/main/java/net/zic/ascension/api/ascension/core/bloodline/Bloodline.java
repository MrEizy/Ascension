package net.zic.ascension.api.ascension.core.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * A bloodline as defined in the bloodline registry.
 * Holds the configuration and the logic, per-origin state belongs in {@link BloodlineData}.
 * Bloodlines held by an origin are always accessed through a {@link BloodlineInstance}, which pairs the bloodline with its typed data.
 * @param <D> the data this bloodline creates and runs on
 */
public interface Bloodline<D extends BloodlineData> {


    BloodlineType getType();

    //data is null when the bloodline is displayed without an owner, e.g. on a bloodline essence
    Component getName(@Nullable D data);

    Component getDescription(@Nullable D data);


    /**
     * called when the bloodline is added to an origin source
     * @param source the origin source it is being added to
     * @param instance the instance being added
     * @return a collection of paths this bloodline wants to try and add
     */
    Collection<Identifier> onAdded(OriginSource source, BloodlineInstance<D> instance);

    /**
     * Called when the bloodline is removed from a source
     * @param source the source it is removed from
     * @param instance the instance being removed
     * @return a collection of paths this bloodline wants to try and remove
     */
    Collection<Identifier> onRemoved(OriginSource source, BloodlineInstance<D> instance);

    //called when an entity that owns an origin detects the bloodline was changed
    void applyToEntity(LivingEntity entity, BloodlineInstance<D> instance);

    //called when either an entity is detached from an origin or the bloodline is removed from the origin
    void removeFromEntity(LivingEntity entity, BloodlineInstance<D> instance);

    //takes in a prospective purity change, clamps it and then breaks it down into percentage increments
    default void handlePurityChange(OriginSource source, BloodlineInstance<D> instance, int newPurity){
        D data = instance.getData();
        newPurity = Math.clamp(newPurity,1,100);
        if(newPurity < data.getPurity()){
            //purity decreased
            for(int purity = data.getPurity();purity>newPurity;purity--){
                data.setPurity(purity - 1);
                purityDown(source,instance);
            }
        }else{

            //purity increased
            for(int purity = data.getPurity()+1;purity<=newPurity;purity++){
                data.setPurity(purity);
                purityUp(source,instance);
            }
        }

    }

    //represents a single purity increment
    //for handler on down should actually use the previous value rather than the current one

    void purityDown(OriginSource source, BloodlineInstance<D> instance);
    void purityUp(OriginSource source, BloodlineInstance<D> instance);

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
