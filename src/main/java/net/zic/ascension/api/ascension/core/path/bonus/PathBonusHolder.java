package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.HashMap;
import java.util.HashSet;

public class PathBonusHolder {


    //maps a category -> category bonus holder
    private final HashMap<Identifier,PathBonusCategoryHolder> categories = new HashMap<>();

    private final HashSet<Identifier> dirtyCategories = new HashSet<>();

    protected PathBonusCategoryHolder getCategoryHolder(Identifier category){
        categories.computeIfAbsent(category,key->new PathBonusCategoryHolder());
        return categories.get(category);
    }

    public void addBonus(Identifier category,Identifier path,double val){
        getCategoryHolder(category).addBonus(path,val);
        dirtyCategories.add(category);
    }
    public void addBonusModifier(Identifier category, Identifier path, ValueContainerModifier modifier){
        getCategoryHolder(category).addBonusModifier(path,modifier);
        dirtyCategories.add(category);
    }

    public void removeBonus(Identifier category,Identifier path,double val){
        if(!categories.containsKey(category)) return;
        getCategoryHolder(category).removeBonus(path,val);
        dirtyCategories.add(category);
    }

    public void removeBonusModifier(Identifier category,Identifier path,Identifier modifier){
        if(!categories.containsKey(category)) return;
        getCategoryHolder(category).removeBonusModifier(path,modifier);
        dirtyCategories.add(category);
    }


    public double getBonus(Identifier category,Identifier path){
        return categories.containsKey(category) ? getCategoryHolder(category).getBonus(path) : 0;
    }


    public void encode(ByteBuf buf){

    }
    public void decode(ByteBuf buf){

    }
}
