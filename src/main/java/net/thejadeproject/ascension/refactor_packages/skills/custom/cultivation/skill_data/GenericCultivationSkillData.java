package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.skill_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.GenericCultivationSkill;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

import java.util.HashSet;
import java.util.Set;

public class GenericCultivationSkillData implements IPersistentSkillData {

    private double baseRate;
    private Set<ResourceLocation> secondaryPaths;

    public GenericCultivationSkillData(double baseRate, Set<ResourceLocation> secondaryPaths) {
        this.baseRate = baseRate;
        this.secondaryPaths = secondaryPaths == null ? new HashSet<>() : new HashSet<>(secondaryPaths);
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

    public GenericCultivationSkillData(RegistryFriendlyByteBuf buf) {
        this.baseRate = buf.readDouble();

        int pathCount = buf.readInt();
        this.secondaryPaths = new HashSet<>();

        for (int i = 0; i < pathCount; i++) {
            this.secondaryPaths.add(ByteBufUtil.readResourceLocation(buf));
        }
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
        buf.writeDouble(baseRate);
        buf.writeInt(secondaryPaths.size());

        for (ResourceLocation secondaryPath : secondaryPaths) {
            ByteBufUtil.encodeString(buf, secondaryPath.toString());
        }
    }

    @Override
    public IPersistentSkillData copy() {
        return new GenericCultivationSkillData(baseRate, secondaryPaths);
    }

    @Override
    public IPersistentSkillData merge(IPersistentSkillData other) {
        if (other instanceof GenericCultivationSkillData otherData) {
            return otherData.copy();
        }

        return copy();
    }
}
