package net.thejadeproject.ascension.refactor_packages.skills;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class HeldSkills {




    private final HashMap<ResourceLocation,HeldSkill> skills = new HashMap<>();


    private final ArrayList<HeldSkill> modifiedSyncBuffer = new ArrayList<>();
    private final ArrayList<HeldSkill> additionSyncBuffer = new ArrayList<>();
    private final ArrayList<HeldSkill> removalSyncBuffer = new ArrayList<>();


    /*
        if a skill is fixed it just means if a technique is removed the skill stays,
        ideally this should be set up such that when loading the data the technique history is able to add the skill
        so it should not be a situation where a player can keep a skill after LOSING cultivation since lost realms are not tracked

        this is a limitation of the system that we will have to deal with

        should i even handle it like this though? or should i instead removed the idea of fixed and offload removal checks?

        like an on remove event that can be cancelled? so if for example i try to remove a skill that another source gives it can cancel it
        
     */

    public void addSkill(ResourceLocation skill,IPersistentSkillData skillData){
        HeldSkill heldSkill = new HeldSkill(skill);
        heldSkill.setPersistentData(skillData);
        skills.put(skill,heldSkill);
        additionSyncBuffer.add(heldSkill);
    }


    //does not mark as dirty since this is only used client side
    private void updateModifiedSkill(HeldSkill skill){
        HeldSkill heldSkill =  skills.get(skill.getKey());
        heldSkill.setPersistentData(skill.getPersistentData());

    }


    public HeldSkill getHeldSkill(ResourceLocation skillKey){
        return skills.get(skillKey);
    }

    public void setPersistentData(ResourceLocation skillKey,IPersistentSkillData persistentData){
        if(!skills.containsKey(skillKey)) return;
        skills.get(skillKey).setPersistentData(persistentData);
        markDirty(skillKey);
    }


    public void markDirty(ResourceLocation skillKey){
        if(!skills.containsKey(skillKey)) return;
        modifiedSyncBuffer.add(skills.get(skillKey));
    }

    public IPersistentSkillData removeSkill(ResourceLocation skillKey){
        if(!skills.containsKey(skillKey))return null;
        HeldSkill heldSkill = skills.remove(skillKey);
        removalSyncBuffer.add(heldSkill);
        return heldSkill.getPersistentData();

    }

    public boolean hasSkill(ResourceLocation skillKey){
        return skills.containsKey(skillKey);
    }
    public Collection<HeldSkill> getSkills(){
        return skills.values();
    }

    private void clearBuffers(){
        additionSyncBuffer.clear();
        modifiedSyncBuffer.clear();
        removalSyncBuffer.clear();
    }

    public void onFormRemoved(IEntityData entityData,ResourceLocation form){

    }
    public void onFormAdded(IEntityData entityData,ResourceLocation form){

    }

    public  void removeFrom(IEntityData entityData){
        //TODO used when it is removed from an entity but we want the skills to be maintained
        //TODO if entity has an existing instance of persistent skill data make a copy and do not call removed
    }
    public  void addTo(IEntityData entityData){
        //TODO used when it is added to an existing entity data
    }

    //============================== NETWORK =================================
    public static HeldSkills decodeFull(RegistryFriendlyByteBuf buf){

        HeldSkills heldSkills = new HeldSkills();
        int skills = buf.readInt();

        for(int i =0;i<skills;i++){

            HeldSkill heldSkill = HeldSkill.decode(buf);
            heldSkills.skills.put(heldSkill.getKey(),heldSkill);

        }
        return heldSkills;
    }
    public static void encodeFull(RegistryFriendlyByteBuf buf,HeldSkills heldSkills){

        buf.writeInt(heldSkills.skills.size());
        //System.out.println("encoding : "+heldSkills.skills.size());
        Collection<HeldSkill> group = new ArrayList<>(heldSkills.skills.values());
        for(HeldSkill skill : group){
            //System.out.println("encoding skill :" +skill.getKey());
            skill.encode(buf);
            //System.out.println("finished encoding");
        }

    }


    public void decode(RegistryFriendlyByteBuf buf){

        if(buf.readBoolean())decodeChanges(buf);
        else decodeFull(buf);
    }

    private void decodeChanges(RegistryFriendlyByteBuf buf){
        //added
        for(int i =0;i<buf.readInt();i++){
            HeldSkill skill = HeldSkill.decode(buf);
            skills.put(skill.getKey(),skill);
        }
        //removed
        for(int i =0;i<buf.readInt();i++){
            skills.remove(ByteBufUtil.readResourceLocation(buf));
        }

        //modified skills
        for(int i =0;i<buf.readInt();i++){
            updateModifiedSkill(HeldSkill.decode(buf));
        }


        if(FMLLoader.getDist() != Dist.CLIENT) clearBuffers(); //should prevent accidental memory leak
    }


    public void encode(RegistryFriendlyByteBuf buf,boolean onlyChanges){

        buf.writeBoolean(onlyChanges);
        if(onlyChanges) encodeChanges(buf);

    }
    private void encodeChanges(RegistryFriendlyByteBuf buf){
        //write added

        buf.writeInt(additionSyncBuffer.size());
        for(HeldSkill heldSkill : additionSyncBuffer){
            heldSkill.encode(buf);
        }
        //write removed
        buf.writeInt(removalSyncBuffer.size());
        for(HeldSkill heldSkill : removalSyncBuffer){
            ByteBufUtil.encodeString(buf,heldSkill.getKey().toString());
        }
        //write modified
        buf.writeInt(modifiedSyncBuffer.size());
        for(HeldSkill heldSkill : modifiedSyncBuffer){
            heldSkill.encode(buf);
        }
        clearBuffers();

    }


}
