package net.zic.ascension.api.core.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.SourceChangesSnapshot;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

/**
 * All OriginSource wrappers must Implement this
 */
public interface AscensionEntityData {

    LivingEntity getEntity();

    OriginSource getSource();

    //runs when the entity is fully constructed
    void initialize();

    void markDirty(SourceChangesSnapshot snapshot);


    //──Affinity────────────────────────────────────────────────────────

    /*TODO
    right now does nothing, but in the future will be used to allow for entity specific affinity bonuses,
    so i am ensuring I expose the necessary api endpoints
     */

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
}
