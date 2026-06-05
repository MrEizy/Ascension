package net.zic.ascension.capabilities.entity_holder;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.common.AscensionAttachments;

public class PlayerDataHolder implements AscensionEntityDataHolder {
    @Override
    public AscensionEntityData getData(LivingEntity entity) {
        //all other things should go ABOVE here. if it reaches this point always return it
        return entity.getData(AscensionAttachments.SIMPLE_ENTITY_DATA);
    }

    @Override
    public void markDirty(LivingEntity entity) {
        if(entity.hasData(AscensionAttachments.SIMPLE_ENTITY_DATA)){
            entity.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        }
    }
}
