package net.zic.ascension.api.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;

import java.util.Collection;

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


    BloodlineData newData();
    BloodlineData loadData(ValueInput input);
    BloodlineData loadData(ByteBuf buf);
}
