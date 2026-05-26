package net.zic.ascension.api.core.entity;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.OriginSource;

/**
 * All OriginSource wrappers must Implement this
 */
public interface AscensionEntityData {

    LivingEntity getEntity();

    OriginSource getSource();
}
