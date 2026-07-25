package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;

import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;

import java.util.Collection;
import java.util.Optional;

public interface  Physique {


    PhysiqueType getType();

    Component name();

    Component description();

    /**
     * called when the physique is added to an origin source
     * @param source the origin source it is being added to
     * @param data the data for this physique
     * @return a collection of paths this physique wants to try and add
     */
    Collection<Identifier> onAdded(OriginSource source, PhysiqueData data);

    /**
     * Called when the physique is removed from a source
     * @param source the source it is removed from
     * @param data the data of this physique
     * @return a collection of paths this physique wants to try and remove
     */
    Collection<Identifier> onRemoved(OriginSource source, PhysiqueData data);

    //called when an entity that owns an origin detects the physique was changed
    void applyToEntity(LivingEntity entity,PhysiqueData data);

    //called when either an entity is detached from an origin or the physique is removed from the origin
    void removeFromEntity(LivingEntity entity,PhysiqueData data);

    PhysiqueData newData(RegistryAccess access);
    PhysiqueData loadData(ValueInput input, RegistryAccess access);
    PhysiqueData loadData(ByteBuf buf);

    default Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return Optional.empty();
    }
}
