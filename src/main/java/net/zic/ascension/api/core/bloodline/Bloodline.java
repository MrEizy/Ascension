package net.zic.ascension.api.core.bloodline;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.physique.PhysiqueData;
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
    Collection<Path> onAdded(OriginSource source, BloodlineData data);

    /**
     * Called when the bloodline is removed from a source
     * @param source the source it is removed from
     * @param data the data of this bloodline
     * @return a collection of paths this bloodline wants to try and remove
     */
    Collection<Path> onRemoved(OriginSource source, BloodlineData data);


    BloodlineData newData();
    BloodlineData loadData(ValueInput input);
    BloodlineData loadData(RegistryFriendlyByteBuf buf);
}
