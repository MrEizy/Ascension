package net.zic.ascension.api.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.entity.AscensionEntityData;

public interface AscensionEntityDataHolder {

    AscensionEntityData getData(LivingEntity entity);
}
