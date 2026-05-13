package net.zic.ascension.refactor_packages.skill_casting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.HashMap;

/*
    works for all skills
    by default the identifier is just the skill id, however skills can use a different one if they want to be able
    to process multiple cooldowns
*/
public class SkillCooldownHandler {

    private final HashMap<ResourceLocation,HashMap<String,Integer>> cooldowns = new HashMap<>();


    public void addCooldown(ResourceLocation skill,String identifier,int time){

        if(!cooldowns.containsKey(skill)) cooldowns.put(skill,new HashMap<>());

        cooldowns.get(skill).put(identifier,time);


    }

    public void addCooldown(ResourceLocation skill,int time){
        //create a default identifier, this will just be the skill id
        addCooldown(skill,skill.toString(),time);
    }

    public int getCooldown(ResourceLocation skill,String identifier){
        if(!cooldowns.containsKey(skill)) return 0;
        if(!cooldowns.get(skill).containsKey(identifier)) return 0;
        return cooldowns.get(skill).get(identifier);
    }

    public int getCooldown(ResourceLocation skill){
        return getCooldown(skill,skill.toString());
    }
    public boolean isOnCooldown(ResourceLocation skill,String identifier){
        if(!cooldowns.containsKey(skill)) return false;
        return cooldowns.get(skill).containsKey(identifier);
    }
    public boolean isOnCooldown(ResourceLocation skill){
        return isOnCooldown(skill,skill.toString());
    }

    public void tick(IEntityData entityData){
        for (ResourceLocation key : cooldowns.keySet()){

            for(String identifier : cooldowns.get(key).keySet()){
                cooldowns.get(key).put(identifier,cooldowns.get(key).get(identifier)-1);
                if(cooldowns.get(key).get(identifier) <= 0){
                    cooldowns.get(key).remove(identifier);
                    if(cooldowns.get(key).isEmpty()) cooldowns.remove(key);
                    AscensionRegistries.Skills.SKILL_REGISTRY.get(key).finishedCooldown(entityData,identifier);
                }
            }

        }
    }


    public void write(CompoundTag tag){
        ListTag cooldownsTag = new ListTag();
        for(ResourceLocation identifier : cooldowns.keySet()){
            CompoundTag cooldownTag = new CompoundTag();
            cooldownTag.putString("identifier",identifier.toString());
            ListTag individualCooldowns = new ListTag();
            for(String id : cooldowns.get(identifier).keySet()){
                CompoundTag instanceTag = new CompoundTag();
                instanceTag.putString("id",id);
                instanceTag.putInt("cooldown",cooldowns.get(identifier).get(id));
                individualCooldowns.add(instanceTag);
            }
            cooldownTag.put("skill_cooldowns",individualCooldowns);
            cooldownsTag.add(cooldownTag);
        }
        tag.put("cooldowns",cooldownsTag);
    }
    public void read(CompoundTag tag){
        ListTag cooldownsList = tag.getList("cooldowns", Tag.TAG_COMPOUND);
        for(int i =0;i<cooldownsList.size();i++){
            CompoundTag skillTag = cooldownsList.getCompound(i);
            ResourceLocation identifier = ResourceLocation.parse(skillTag.getString("identifier"));
            ListTag skillCooldowns = skillTag.getList("skill_cooldowns",Tag.TAG_COMPOUND);
            cooldowns.put(identifier,new HashMap<>());
            for(int j=0;j<skillCooldowns.size();j++){
                CompoundTag cooldown = skillCooldowns.getCompound(i);
                cooldowns.get(identifier).put(cooldown.getString("id"),cooldown.getInt("cooldown"));
            }
        }
    }
}
