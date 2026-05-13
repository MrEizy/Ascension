package net.thejadeproject.ascension.refactor_packages.skills;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

public class HeldSkill  {



    private final ResourceLocation skillKey;
    private IPersistentSkillData persistentData;





    public HeldSkill(ResourceLocation skillKey){
        this.skillKey = skillKey;

    }


    public ISkill getSkill(){return AscensionRegistries.Skills.SKILL_REGISTRY.get(skillKey);}
    public ResourceLocation getKey(){return skillKey;}

    public IPersistentSkillData getPersistentData(){return persistentData;}
    public void setPersistentData(IPersistentSkillData persistentData){this.persistentData = persistentData;}

    public CompoundTag write(){
        CompoundTag tag = new CompoundTag();
        tag.putString("skill_key",getKey().toString());
        if(getPersistentData() != null) tag.put("skill_data",getPersistentData().write());

        return tag;
    }
    public static HeldSkill read(CompoundTag tag,IEntityData heldEntity){
        HeldSkill heldSkill = new HeldSkill(ResourceLocation.bySeparator(tag.getString("skill_key"),':'));

        if(tag.contains("skill_data")){
            heldSkill.setPersistentData(heldSkill.getSkill().fromCompound(tag,heldEntity));
        }

        return heldSkill;
    }

    public void encode(RegistryFriendlyByteBuf buf){
        //System.out.println("writing skill : "+getKey().toString());
        ByteBufUtil.encodeString(buf,getKey().toString());

        //System.out.println("data encoded? : "+(persistentData != null));
        if(persistentData != null) persistentData.encode(buf);


    }
    public static HeldSkill decode(RegistryFriendlyByteBuf buf){
        ResourceLocation skillKey = ByteBufUtil.readResourceLocation(buf);

        HeldSkill skill = new HeldSkill(skillKey);


        IPersistentSkillData data = skill.getSkill().fromNetwork(buf);

        skill.setPersistentData(data);

        return skill;
    }
}
