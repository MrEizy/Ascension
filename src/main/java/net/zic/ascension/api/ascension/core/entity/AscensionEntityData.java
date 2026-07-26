package net.zic.ascension.api.ascension.core.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Collection;

/**
 * All OriginSource wrappers must Implement this
 */
public interface AscensionEntityData extends StatProvider, PathBonusProvider {

    LivingEntity getEntity();


    default void registerProviders(){
        getEntity().getData(ZenithAttachments.STAT_HOLDER).registerStatProvider(this);
        getEntity().getData(CoreAttachments.PATH_BONUS_HOLDER).removePathBonusProvider(this);
    }

    AscensionOriginSource getSource();

    //runs when the entity is fully constructed
    void initialize();

    void markDirty(OriginSourcePatch patch);
    boolean isCultivationSuppressed();
    void setCultivationSuppressed(boolean state);

    // Stat Suppression methods
    default double getAttributeSuppression(Holder<Attribute> attribute) {return 1.0D;}
    default void setAttributeSuppression(Holder<Attribute> attribute, double percentage) {}
    default void applyAttributeSuppression(Holder<Attribute> attribute) {}
    default void applyAllAttributeSuppressions() {}


}
