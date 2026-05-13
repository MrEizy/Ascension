package net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.stats.Stat;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

import java.util.HashMap;
//FOR NOW JUST ADDS IT TO ACTIVE FORM. WILL BE CHANGED LATER
public class BasicStatChangeHandler{

    private HashMap<ResourceLocation, ValueContainerModifier> minorRealmStatModifierMap = new HashMap<>();
    private HashMap<ResourceLocation, ValueContainerModifier> majorRealmStatModifierMap = new HashMap<>();

    private HashMap<Holder<Attribute>,ValueContainerModifier> minorRealmAttributeModifierMap = new HashMap<>();
    private HashMap<Holder<Attribute>,ValueContainerModifier> majorRealmAttributeModifierMap = new HashMap<>();


    public ResourceLocation create(String prefix,int majorRealm,int minorRealm){
        return ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,prefix+"_"+majorRealm+"_"+minorRealm);
    }
    public BasicStatChangeHandler addMinorRealmStatModifier(ResourceLocation stat,ValueContainerModifier modifier){
        minorRealmStatModifierMap.put(stat,modifier);
        return this;
    }
    public BasicStatChangeHandler addMajorRealmStatModifier(ResourceLocation stat,ValueContainerModifier modifier){
        majorRealmStatModifierMap.put(stat,modifier);
        return this;
    }
    public BasicStatChangeHandler addMinorRealmAttributeModifier(Holder<Attribute> attributeHolder,ValueContainerModifier modifier){
        minorRealmAttributeModifierMap.put(attributeHolder,modifier);
        return this;
    }
    public BasicStatChangeHandler addMajorRealmAttributeModifier(Holder<Attribute> attributeHolder,ValueContainerModifier modifier){
        majorRealmAttributeModifierMap.put(attributeHolder,modifier);
        return this;
    }


    public Stat getStat(ResourceLocation stat){
        return AscensionRegistries.Stats.STATS_REGISTRY.get(stat);
    }

    public ValueContainerModifier duplicate(ResourceLocation identifier,ValueContainerModifier template){
        return new ValueContainerModifier(template.getVal(),template.getOperation(),identifier,template.getGroupIdentifier());
    }

    public void applyAllMinorRealmChanges(IEntityData entityData,int majorRealm,int minorRealm){
        //System.out.println("adding minor realm changes");
        for(ResourceLocation stat : minorRealmStatModifierMap.keySet()){
            ResourceLocation identifier = create("minor",majorRealm,minorRealm);
            entityData.getActiveFormData().getStatSheet().addStatModifier(getStat(stat),duplicate(identifier,minorRealmStatModifierMap.get(stat)));
        }
        for(Holder<Attribute> attributeHolder : minorRealmAttributeModifierMap.keySet()){
            ResourceLocation identifier = create("minor",majorRealm,minorRealm);
            entityData.getAscensionAttributeHolder().getAttribute(attributeHolder).addModifier(duplicate(identifier,minorRealmAttributeModifierMap.get(attributeHolder)));
        }
    }
    public void applyAllMajorRealmChanges(IEntityData entityData,int majorRealm){
        for(ResourceLocation stat : majorRealmStatModifierMap.keySet()){
            ResourceLocation identifier = create("major",majorRealm,0);
            entityData.getActiveFormData().getStatSheet().addStatModifier(getStat(stat),duplicate(identifier,majorRealmStatModifierMap.get(stat)));
        }
        for(Holder<Attribute> attributeHolder : majorRealmAttributeModifierMap.keySet()){
            ResourceLocation identifier = create("major",majorRealm,0);
            entityData.getAscensionAttributeHolder().getAttribute(attributeHolder).addModifier(duplicate(identifier,majorRealmAttributeModifierMap.get(attributeHolder)));
        }
    }
    public void removeMajorRealmChanges(IEntityData entityData,int majorRealm){
        for(ResourceLocation stat : majorRealmStatModifierMap.keySet()){
            ResourceLocation identifier = create("major",majorRealm,0);
            Stat statInstance = AscensionRegistries.Stats.STATS_REGISTRY.get(stat);
            entityData.getActiveFormData().getStatSheet().removeStatModifier(statInstance,identifier);
        }
        for(Holder<Attribute> attributeHolder : majorRealmAttributeModifierMap.keySet()){
            ResourceLocation identifier = create("major",majorRealm,0);
            entityData.getAscensionAttributeHolder().getAttribute(attributeHolder).removeModifier(identifier);
        }
    }
    public void removeMinorRealmChanges(IEntityData entityData,int majorRealm,int minorRealm){
        for(ResourceLocation stat : minorRealmStatModifierMap.keySet()){
            ResourceLocation identifier = create("minor",majorRealm,minorRealm);
            Stat statInstance = AscensionRegistries.Stats.STATS_REGISTRY.get(stat);
            entityData.getActiveFormData().getStatSheet().removeStatModifier(statInstance,identifier);
        }
        for(Holder<Attribute> attributeHolder : minorRealmAttributeModifierMap.keySet()){
            ResourceLocation identifier = create("minor",majorRealm,minorRealm);
            entityData.getAscensionAttributeHolder().getAttribute(attributeHolder).removeModifier(identifier);
        }
    }
    //TODO can be streamlined with a getRealms between method, then i either include or exclude each end
    public void applyChanges(IEntityData entityData, ITechnique technique, int oldMajorRealm, int oldMinorRealm, int newMajorRealm, int newMinorRealm){
        if(oldMajorRealm < newMajorRealm || (oldMajorRealm == newMajorRealm && newMinorRealm>oldMinorRealm)){
            int majorRealmsChanged = newMajorRealm-oldMajorRealm;
            for(int i =1;i<=majorRealmsChanged;i++){
                applyAllMajorRealmChanges(entityData,oldMajorRealm);
            }

            if(newMajorRealm != oldMajorRealm) {
                int minorRealms = technique.getMaxMinorRealm(oldMajorRealm)-oldMinorRealm;
                for(int i =1;i<=minorRealms;i++){
                    applyAllMinorRealmChanges(entityData,oldMajorRealm,oldMinorRealm+i);
                }
                for (int i = 1; i < majorRealmsChanged; i++) {
                    int majorRealm = oldMajorRealm+i;
                    int minorRealmsForRealm = technique.getMaxMinorRealm(majorRealm);
                    for(int j = 1;j<=minorRealmsForRealm;j++){
                        applyAllMinorRealmChanges(entityData,majorRealm,j);
                    }
                }

                for(int i =1;i<=newMinorRealm;i++){
                    applyAllMinorRealmChanges(entityData,newMajorRealm,i);
                }
            }else{
                for(int i = oldMinorRealm+1;i<=newMinorRealm;i++){
                    applyAllMinorRealmChanges(entityData,newMajorRealm,i);
                }
            }

        }else{
            for(int i = oldMajorRealm; i > newMajorRealm; i--){
                removeMajorRealmChanges(entityData, i);
            }
            if(newMajorRealm != oldMajorRealm){
                for(int i = oldMinorRealm; i > 0; i--){
                    removeMinorRealmChanges(entityData, oldMajorRealm, i);
                }
                for(int i = oldMajorRealm - 1; i > newMajorRealm; i--){
                    int minorRealmsForRealm = technique.getMaxMinorRealm(i);
                    for(int j = minorRealmsForRealm; j > 0; j--){
                        removeMinorRealmChanges(entityData, i, j);
                    }
                }
                int minorRealms = technique.getMaxMinorRealm(newMajorRealm);
                for(int i = minorRealms; i > newMinorRealm; i--){
                    removeMinorRealmChanges(entityData, newMajorRealm, i);
                }
            }else{
                for(int i = oldMinorRealm; i > newMinorRealm; i--){
                    removeMinorRealmChanges(entityData, newMajorRealm, i);
                }
            }
        }
        entityData.getAscensionAttributeHolder().updateAttributes(entityData);
    }
}
