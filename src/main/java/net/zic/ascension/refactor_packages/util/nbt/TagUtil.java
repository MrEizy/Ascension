package net.zic.ascension.refactor_packages.util.nbt;

import net.minecraft.nbt.*;

public class TagUtil {
    //TODO add a registry or smth? does not have to be a proper one but something that stores a type and how to encode/decode it
    public <T> Tag encodePrimitive(T val){
        if(val instanceof Integer) return IntTag.valueOf((Integer) val);
        else if(val instanceof String)return StringTag.valueOf((String) val);
        return null;

    }
}
