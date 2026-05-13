package net.zic.ascension.refactor_packages.techniques.custom.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.techniques.ITechniqueData;
import net.zic.ascension.refactor_packages.util.ByteBufUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MultiPartTechniqueData implements ITechniqueData {

    private final HashMap<ResourceLocation,Boolean> parts = new HashMap<>();

    public MultiPartTechniqueData(List<ResourceLocation> parts){
        for(ResourceLocation part : parts){
            this.parts.put(part,false);
        }
    }
    public boolean getPartStatus(ResourceLocation part){return parts.get(part);}
    public void setPartStatus(ResourceLocation part,boolean status){this.parts.put(part,status);}

    public Set<ResourceLocation> getParts(){return parts.keySet();}
    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        ListTag ls = new ListTag();
        for(ResourceLocation part : parts.keySet()){
            CompoundTag partTag = new CompoundTag();
            partTag.putString("part",part.toString());
            partTag.putBoolean("state",parts.get(part));
            ls.add(partTag);
        }
        tag.put("parts",ls);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(parts.size());
        for(ResourceLocation part : parts.keySet()){
            ByteBufUtil.encodeString(buf,part.toString());
            buf.writeBoolean(parts.get(part));
        }
    }
    public static MultiPartTechniqueData decode(RegistryFriendlyByteBuf buf){
        int size = buf.readInt();
        MultiPartTechniqueData data = new MultiPartTechniqueData(List.of());
        for(int i = 0;i<size;i++){
            data.setPartStatus(ByteBufUtil.readResourceLocation(buf),buf.readBoolean());
        }
        return data;
    }
    public static MultiPartTechniqueData read(CompoundTag tag){
        ListTag ls = tag.getList("parts", Tag.TAG_COMPOUND);
        MultiPartTechniqueData data = new MultiPartTechniqueData(List.of());
        for(int i = 0;i<ls.size();i++){
            CompoundTag partTag = ls.getCompound(i);
            data.setPartStatus(ResourceLocation.parse(partTag.getString("part")),partTag.getBoolean("state"));
        }
        return data;

    }
}
