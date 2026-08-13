package net.zic.ascension.capabilities.entity_holder;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public record PlayerDataProvider(LivingEntity entity) implements AscensionEntityDataProvider {

    @Override
    public boolean hasData() {
        return entity.hasData(AscensionAttachments.SIMPLE_ENTITY_DATA);
    }

    @Override
    public AscensionEntityData getData() {
        //all other things should go ABOVE here. if it reaches this point always return it
        return entity.getData(AscensionAttachments.SIMPLE_ENTITY_DATA);
    }

    @Override
    public void markDirty() {
        if (entity.hasData(AscensionAttachments.SIMPLE_ENTITY_DATA)) {
            entity.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        }
    }
}
