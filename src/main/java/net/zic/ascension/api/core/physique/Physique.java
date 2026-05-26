package net.zic.ascension.api.core.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;

import java.util.Collection;

public interface  Physique {


    PhysiqueType getType();

    Component getName();

    Component getDescription();

    /**
     * called when the physique is added to an origin source
     * @param source the origin source it is being added to
     * @param data the data for this physique
     * @return a collection of paths this physique wants to try and add
     */
    Collection<Path> onAdded(OriginSource source, PhysiqueData data);

    /**
     * Called when the physique is removed from a source
     * @param source the source it is removed from
     * @param data the data of this physique
     * @return a collection of paths this physique wants to try and remove
     */
    Collection<Path> onRemoved(OriginSource source, PhysiqueData data);

    //called when an entity that owns an origin detects the physique was changed
    void applyToEntity(LivingEntity entity,PhysiqueData data);

    //called when either an entity is detached from an origin or the physique is removed from the origin
    void removeFromEntity(LivingEntity entity,PhysiqueData data);

    PhysiqueData newData();
    PhysiqueData loadData(ValueInput input);
    PhysiqueData loadData(RegistryFriendlyByteBuf buf);
}
