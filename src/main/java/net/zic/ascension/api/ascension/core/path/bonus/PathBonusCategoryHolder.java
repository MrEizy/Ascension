package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.HashMap;
import java.util.HashSet;

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


    public void encode(ByteBuf buf){

    }
    public void decode(ByteBuf buf){

    }

}
