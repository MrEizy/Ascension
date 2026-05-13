package net.zic.ascension.refactor_packages.skills.custom.cultivation.skill_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.skills.IPersistentSkillData;

import java.util.HashSet;
import java.util.Set;

public class GenericCultivationSkillData implements IPersistentSkillData {

    private double baseRate;
    private Set<ResourceLocation> secondaryPaths;

    public GenericCultivationSkillData(double baseRate, Set<ResourceLocation> secondaryPaths){
        this.baseRate = baseRate;

        this.secondaryPaths = secondaryPaths;
    }
    public GenericCultivationSkillData(CompoundTag tag){
        baseRate = tag.getDouble("base_rate");

        HashSet<ResourceLocation> secondaryPaths = new HashSet<>();
        ListTag listTag = tag.getList("secondary_paths", Tag.TAG_STRING);
        for(int i =0;i<listTag.size();i++){
            secondaryPaths.add(ResourceLocation.parse(listTag.getString(i)));
        }
        this.secondaryPaths = secondaryPaths;
    }
    public GenericCultivationSkillData(RegistryFriendlyByteBuf buf){

    }
    public double getBaseRate(){return baseRate;}

    public Set<ResourceLocation> getSecondaryPaths(){return secondaryPaths;}
    public void setSecondaryPaths(Set<ResourceLocation> secondaryPaths){
        this.secondaryPaths = secondaryPaths;
    }
    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("base_rate",baseRate);
        ListTag secondaryPaths = new ListTag();
        for(ResourceLocation secondaryPath : this.secondaryPaths){
            secondaryPaths.add(StringTag.valueOf(secondaryPath.toString()));
        }
        tag.put("secondary_paths",secondaryPaths);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public IPersistentSkillData copy() {
        return null;
    }

    @Override
    public IPersistentSkillData merge(IPersistentSkillData other) {
        return null;
    }
}
