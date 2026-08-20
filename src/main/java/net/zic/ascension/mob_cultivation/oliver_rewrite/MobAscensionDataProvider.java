package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public class MobAscensionDataProvider implements AscensionEntityDataProvider {
    private final Mob mob;

    public MobAscensionDataProvider(Mob mob) {
        this.mob = mob;
    }


    @Override
    public LivingEntity entity() {
        return mob;
    }

    @Override
    public boolean hasData() {
        return mob.hasData(AscensionAttachments.MOB_ENTITY_DATA);
    }

    @Override
    public MobAscensionData getData() {
        return mob.getData(AscensionAttachments.MOB_ENTITY_DATA);
    }

    @Override
    public void markDirty() {
        mob.syncData(AscensionAttachments.MOB_ENTITY_DATA);
    }
}
