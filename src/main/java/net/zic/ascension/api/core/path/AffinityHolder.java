package net.zic.ascension.api.core.path;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.HashMap;

public class AffinityHolder {
    private final HashMap<Identifier, ValueContainer> affinity = new HashMap<>();

    public void addAffinity(Identifier path,double bonus){
        if(!affinity.containsKey(path)){
            affinity.put(path,new ValueContainer(path, 1));
        }
        affinity.get(path).setBaseValue(affinity.get(path).getBaseValue()+bonus);
    }
    public void removeAffinity(Identifier path,double bonus){
        if(!affinity.containsKey(path)) return;
        addAffinity(path,-bonus);
    }

    public void addAffinityModifier(Identifier path, ValueContainerModifier modifier){
        addAffinity(path,0); //makes sure we have the path
        affinity.get(path).addModifier(modifier);
    }

    public void removeAffinityModifier(Identifier path,Identifier modifier){
        if(!affinity.containsKey(path)) return;
        affinity.get(path).removeModifier(modifier);
    }
    public double getAffinity(Identifier path){
        if(!affinity.containsKey(path)) return 1;
        return affinity.get(path).getValue();
    }

}
