package net.zic.ascension.api.ascension.core.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

/**
 * All OriginSource wrappers must Implement this
 */
public interface AscensionEntityData extends StatProvider, PathBonusProvider {

    LivingEntity getEntity();


    default void registerProviders(){
        getEntity().getData(ZenithAttachments.STAT_HOLDER).registerStatProvider(this);
        getEntity().getData(CoreAttachments.PATH_BONUS_HOLDER).removePathBonusProvider(this);
    }

    OriginSource getSource();

    //runs when the entity is fully constructed
    default void initialize(){
        registerProviders();
    };

    void addBonus(Identifier category,Identifier path,double val);
    void addBonusModifier(Identifier category, Identifier path, ValueContainerModifier modifier);
    void removeBonus(Identifier category,Identifier path,double val);
    void removeBonusModifier(Identifier category,Identifier path,Identifier modifier);

    void addStat(Stat stat, double val);
    void removeStat(Stat stat, double val);
    void addStatModifier(Stat stat, ValueContainerModifier modifier);
    void removeStatModifier(Stat stat,Identifier identifier);





    void markDirty(OriginSourcePatch patch);
    boolean isCultivationSuppressed();
    void setCultivationSuppressed(boolean state);


}
