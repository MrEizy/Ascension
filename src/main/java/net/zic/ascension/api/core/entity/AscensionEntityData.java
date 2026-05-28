package net.zic.ascension.api.core.entity;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.SourceChangesSnapshot;

/**
 * All OriginSource wrappers must Implement this
 */
public interface AscensionEntityData {

    LivingEntity getEntity();

    OriginSource getSource();

    //runs when the entity is fully constructed
    void initialize();

    void markDirty(SourceChangesSnapshot snapshot);
}
