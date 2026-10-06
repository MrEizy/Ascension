package net.zic.ascension.api.ascension.core.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;

/**
 * All OriginSource wrappers must Implement this
 */
public interface AscensionEntityData{

    LivingEntity getEntity();


    OriginSource getSource();

    //runs when the entity is fully constructed
    default void initialize(){};
    default void initializeAfterRespawn() {
        initialize();
    }


    void markDirty(OriginSourcePatch patch,boolean fullPatch);
    boolean isCultivationSuppressed();
    void setCultivationSuppressed(boolean state);


}
