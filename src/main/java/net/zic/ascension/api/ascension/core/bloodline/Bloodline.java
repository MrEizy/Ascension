package net.zic.ascension.api.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;

import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;

import java.util.Collection;
import java.util.Optional;

public interface Bloodline {


    BloodlineType getType();

    Component getName();

    Component getDescription();


    /**
     * called when the bloodline is added to an origin source
     * @param source the origin source it is being added to
     * @param data the data for this bloodline
     * @return a collection of paths this bloodline wants to try and add
     */
    Collection<Identifier> onAdded(OriginSource source, BloodlineData data);

    /**
     * Called when the bloodline is removed from a source
     * @param source the source it is removed from
     * @param data the data of this bloodline
     * @return a collection of paths this bloodline wants to try and remove
     */
    Collection<Identifier> onRemoved(OriginSource source, BloodlineData data);

    //called when an entity that owns an origin detects the bloodline was changed
    void applyToEntity(LivingEntity entity,BloodlineData data);

    //called when either an entity is detached from an origin or the bloodline is removed from the origin
    void removeFromEntity(LivingEntity entity,BloodlineData data);

    //takes in a prospective purity change, clamps it and then breaks it down into percentage increments
    default void handlePurityChange(OriginSource source, BloodlineData data, int newPurity){

        newPurity = Math.clamp(newPurity,1,100);
        int oldPurity = data.getPurity();
        if(newPurity < data.getPurity()){
            //purity decreased
            for(int purity = data.getPurity();purity>newPurity;purity--){
                data.setPurity(purity);
                purityDown(source,data);
            }
        }else{

            //purity increased
            for(int purity = data.getPurity()+1;purity<=newPurity;purity++){
                System.out.println(purity);
                data.setPurity(purity);
                purityUp(source,data);
            }
        }

    }

    //represents a single purity increment
    //for handler on down should actually use the previous value rather than the current one

    void purityDown(OriginSource source, BloodlineData data);
    void purityUp(OriginSource source, BloodlineData data);

    BloodlineData newData(RegistryAccess access);
    BloodlineData loadData(ValueInput input,RegistryAccess access);
    BloodlineData loadData(ByteBuf buf);

    default Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return Optional.empty();
    }
}
