package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public class MobAscensionDataProvider implements AscensionEntityDataProvider {




    @Override
    public LivingEntity entity() {
        return null;
    }

    @Override
    public boolean hasData() {
        return false;
    }

    @Override
    public AscensionEntityData getData() {
        return null;
    }

    @Override
    public void markDirty() {

    }
}
