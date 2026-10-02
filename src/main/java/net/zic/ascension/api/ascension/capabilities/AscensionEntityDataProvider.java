package net.zic.ascension.api.ascension.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;

public interface AscensionEntityDataProvider {

    LivingEntity entity();

    //if using attachments can be used for .hasData, but also might want to use to check if the can have data instead
    boolean hasData();
    AscensionEntityData getData();

    void markDirty();

}
