package net.zic.ascension.api.ascension.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;

public interface AscensionEntityDataProvider {

    AscensionEntityData getData(LivingEntity entity);

    void markDirty(LivingEntity entity);

}
