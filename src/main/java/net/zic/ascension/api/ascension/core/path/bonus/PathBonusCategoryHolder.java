package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;

import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PathBonusCategoryHolder {

    //maps a path -> bonus
    private final HashMap<Identifier, ValueContainer> pathBonus = new HashMap<>();

    private final HashSet<Identifier> dirtyPathBonus = new HashSet<>();


    protected ValueContainer getPathBonus(Identifier path){
        pathBonus.computeIfAbsent(path,key->{
            ValueContainer container = new ValueContainer(path,0);
            container.setMinValue(0);
            return container;
        });
        return pathBonus.get(path);
    }


    public void addBonus(Identifier path,double val){
        getPathBonus(path).setBaseValue(val + getPathBonus(path).getBaseValue());
        dirtyPathBonus.add(path);
    }
    public void addBonusModifier( Identifier path, ValueContainerModifier modifier){
        getPathBonus(path).addModifier(modifier);
        dirtyPathBonus.add(path);
    }

    public void removeBonus(Identifier path,double val){
        if(!pathBonus.containsKey(path)) return;

        getPathBonus(path).setBaseValue(getPathBonus(path).getBaseValue()-val);
        dirtyPathBonus.add(path);
    }

    public void removeBonusModifier(Identifier path,Identifier modifier){
        if(!pathBonus.containsKey(path)) return;
        getPathBonus(path).removeModifier(modifier);
        dirtyPathBonus.add(path);
    }


    public double getBonus(Identifier path){
        return pathBonus.containsKey(path) ? getPathBonus(path).getValue() : 0;
    }

    public void setPathBonusContainer(Identifier path,ValueContainer container){
        pathBonus.put(path,container);
    }


    public Collection<Identifier> getAllPaths(){
        return pathBonus.keySet();
    }
    public Collection<Identifier> getDirtyPaths(){
        return dirtyPathBonus;
    }
    public ValueContainer getPathBonusContainer(Identifier path){
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
            ValueContainer.encode(buf,pathBonus.get(path));
        }
    }
    protected void encodePartialPatch(ByteBuf buf){
        buf.writeInt(dirtyPathBonus.size());
        for(Identifier dirtyPathBonus : dirtyPathBonus){
            ValueContainer.encode(buf,pathBonus.get(dirtyPathBonus));
        }
    }
    public void decode(ByteBuf buf){

        if(buf.readBoolean()) pathBonus.clear();
        int size = buf.readInt();
        for(int i = 0;i<size; i++){
            ValueContainer container = ValueContainer.decode(buf);
            pathBonus.put(container.getIdentifier(),container);
        }

        dirtyPathBonus.clear();
    }
}
