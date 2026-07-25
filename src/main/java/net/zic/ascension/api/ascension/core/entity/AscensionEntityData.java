package net.zic.ascension.api.ascension.core.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.source.SourceChangesSnapshot;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Collection;

/**
 * All OriginSource wrappers must Implement this
 */
public interface AscensionEntityData {

    LivingEntity getEntity();

    AscensionOriginSource getSource();

    //runs when the entity is fully constructed
    void initialize();

    @Deprecated
    void markDirty(SourceChangesSnapshot snapshot);
    void markDirty(OriginSourcePatch patch);
    boolean isCultivationSuppressed();
    void setCultivationSuppressed(boolean state);

    // Stat Suppression methods
    default double getAttributeSuppression(Holder<Attribute> attribute) {return 1.0D;}
    default void setAttributeSuppression(Holder<Attribute> attribute, double percentage) {}
    default void applyAttributeSuppression(Holder<Attribute> attribute) {}
    default void applyAllAttributeSuppressions() {}

    //──Affinity────────────────────────────────────────────────────────



    default boolean hasAffinity(Identifier path){
         return getSource().hasAffinity(path);
    }
    default boolean hasAffinity(Identifier path,Identifier category){
        return getSource().hasAffinity(path,category);
    }
    default void addAffinity(Identifier path, double val){
        getSource().addAffinity(path,val);
    }
    default void removeAffinity(Identifier path,double val){
        addAffinity(path,-val);

    }
    default void addAffinityModifier(Identifier path, ValueContainerModifier modifier){
        getSource().addAffinityModifier(path,modifier);
    }

    default void removeAffinityModifier(Identifier path,Identifier modifier){
        getSource().removeAffinityModifier(path,modifier);
    }

    default double getAffinity(Identifier path){
        return getSource().getAffinity(path);
    }
    default double getBaseAffinity(Identifier path){
        return getSource().getBaseAffinity(path);
    }

    default void addAffinity(Identifier category,Identifier path,double val){
        getSource().addAffinity(category,path,val);
    }
    default void removeAffinity(Identifier category,Identifier path,double val){
        getSource().removeAffinity(category,path,val);

    }
    default void addAffinityModifier(Identifier category,Identifier path,ValueContainerModifier modifier){
        getSource().addAffinityModifier(category,path,modifier);
    }

    default void removeAffinityModifier(Identifier category,Identifier path,Identifier modifier){
        getSource().removeAffinityModifier(category,path,modifier);
    }

    default double getAffinity(Identifier category,Identifier path){

        return getSource().getAffinity(category,path);
    }
    default double getBaseAffinity(Identifier category,Identifier path){
        return getSource().getBaseAffinity(category,path);
    }

    default Collection<Identifier> getAllAffinities(){
        return getSource().getAllAffinity();
    }
    default Collection<Identifier> getAllAffinities(Identifier category){
        return getSource().getAllAffinity(category);
    }
}
