package net.zic.ascension.api.ascension.core.path.bonus;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;

import java.util.*;

public class PathBonusHolder{

    private final Map<PathBonus, ValueContainer<Double>> pathBonuses = new HashMap<>();

    private final Set<PathBonus> dirtyBonuses = new HashSet<>();
    private final Set<PathBonus> removedBonuses = new HashSet<>();

    public ValueContainer<Double> getContainer(Identifier category,Identifier path){
        return pathBonuses.get(PathBonus.of(category,path));
    }

    private ValueContainer<Double> getOrCreateContainer(Identifier category,Identifier path){
        return pathBonuses.computeIfAbsent(PathBonus.of(category,path), key->
                    ValueContainerHelpers.doubleValueContainer(key.path())
                );
    }

    public boolean hasPathBonus(Identifier category,Identifier path){
        return pathBonuses.containsKey(PathBonus.of(category,path));
    }

    public void addFlatModifier(Identifier category, Identifier path, Modifier<Double> modifier){
        getOrCreateContainer(category,path).addFlatModifier(modifier);
        dirtyBonuses.add(PathBonus.of(category,path));

    }

    public void addMultiplierModifier(Identifier category,Identifier path,Modifier<Double> modifier){
        getOrCreateContainer(category,path).addMultiplierModifier(modifier);
        dirtyBonuses.add(PathBonus.of(category,path));

    }
    public void removeModifier(Identifier category,Identifier path,Identifier modifier){
        if(!hasPathBonus(category,path)) return;
        PathBonus bonus = PathBonus.of(category,path);
        getContainer(category,path).removeModifier(modifier);
        if(getContainer(category,path).isEmpty()) pathBonuses.remove(bonus);
        removedBonuses.add(bonus);

    }
    public double getPathBonus(Identifier category,Identifier path){
        return getContainer(category,path)  == null ? 0 : getContainer(category,path).getValue();
    }

    public void removePathBonusContainer(Identifier category,Identifier path){
        PathBonus bonus = PathBonus.of(category,path);
        pathBonuses.remove(bonus);
        removedBonuses.add(bonus);
    }

    //assumes container ID is the path
    public void setPathBonusContainer(Identifier category,ValueContainer<Double> container){
        PathBonus bonus = PathBonus.of(category,container.getContainerId());
        pathBonuses.put(bonus,container);
        dirtyBonuses.add(bonus);
    }

    public Collection<PathBonus> getPathBonuses(){
        return pathBonuses.keySet();
    }

    public Collection<PathBonus> dirtyBonuses(){
        return dirtyBonuses;
    }
    public Collection<PathBonus> removedBonuses(){
        return removedBonuses;
    }
    public void clearCache(){
        dirtyBonuses.clear();
        removedBonuses.clear();
    }




}
