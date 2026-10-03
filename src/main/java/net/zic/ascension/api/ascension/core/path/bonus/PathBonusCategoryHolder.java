package net.zic.ascension.api.ascension.core.path.bonus;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;

import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.RangedValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PathBonusCategoryHolder {

    //maps a path -> bonus
    private final HashMap<Identifier, ValueContainer<Double>> pathBonus = new HashMap<>();

    private final HashSet<Identifier> dirtyPathBonus = new HashSet<>();


    protected ValueContainer<Double> getPathBonus(Identifier path){
        pathBonus.computeIfAbsent(path, ValueContainerHelpers::doubleValueContainer);
        return pathBonus.get(path);
    }

    public void addFlatModifier(Identifier path, Modifier<Double> modifier){
        getPathBonus(path).addFlatModifier(modifier);
        dirtyPathBonus.add(path);
    }
    public void addMultiplierModifier(Identifier path,Modifier<Double> modifier){
        getPathBonus(path).addMultiplierModifier(modifier);
        dirtyPathBonus.add(path);
    }
    public void removeModifier(Identifier path,Identifier modifier){
        if(!pathBonus.containsKey(path)) return;

        getPathBonus(path).removeModifier(modifier);
        dirtyPathBonus.add(path);
    }

    public void removePath(Identifier path){
        pathBonus.remove(path);

    }

    public double getBonus(Identifier path){
        return pathBonus.containsKey(path) ? getPathBonus(path).getValue() : 0;
    }

    public void setPathBonusContainer(Identifier path,ValueContainer<Double> container){
        pathBonus.put(path,container);
    }


    public Collection<Identifier> getAllPaths(){
        return pathBonus.keySet();
    }
    public Collection<Identifier> getDirtyPaths(){
        return dirtyPathBonus;
    }
    public ValueContainer<Double> getPathBonusContainer(Identifier path){
        return pathBonus.get(path);
    }

    public void encode(ByteBuf buf,boolean fullPatch){

        buf.writeBoolean(fullPatch);
        if(fullPatch) encodeFullPatch(buf);
        else encodePartialPatch(buf);

        dirtyPathBonus.clear();

    }
    protected void encodeFullPatch(ByteBuf buf){
        buf.writeInt(pathBonus.size());
        for(Identifier path : pathBonus.keySet()){
            ValueContainer.encode(pathBonus.get(path),buf, Codec.DOUBLE);
        }
    }
    protected void encodePartialPatch(ByteBuf buf){
        buf.writeInt(dirtyPathBonus.size());
        for(Identifier dirtyPathBonus : dirtyPathBonus){
            ValueContainer.encode(pathBonus.get(dirtyPathBonus),buf, Codec.DOUBLE);
        }
    }
    public void decode(ByteBuf buf){

        if(buf.readBoolean()) pathBonus.clear();
        int size = buf.readInt();
        for(int i = 0;i<size; i++){
            ValueContainer<Double> container = ValueContainer.decode(ValueContainerHelpers::doubleValueContainer,buf,Codec.DOUBLE);
            pathBonus.put(container.getContainerId(),container);
        }

        dirtyPathBonus.clear();
    }
}
