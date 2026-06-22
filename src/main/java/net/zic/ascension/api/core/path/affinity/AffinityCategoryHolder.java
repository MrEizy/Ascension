package net.zic.ascension.api.core.path.affinity;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class AffinityCategoryHolder {
    private final HashMap<Identifier, ValueContainer> affinity = new HashMap<>();

    public void addAffinity(Identifier path,double bonus){
        if(!affinity.containsKey(path)){
            affinity.put(path,new ValueContainer(path, 0));
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
        if(!affinity.containsKey(path)) return 0; //affinity is x(1+affinity)

        return affinity.get(path).getValue();
    }


    public double getBaseAffinity(Identifier path){
        if(!affinity.containsKey(path)) return 1;
        return affinity.get(path).getBaseValue();
    }

    public boolean hasAffinity(Identifier path){
        return affinity.containsKey(path);
    }
    public ValueContainer getAffinityContainer(Identifier path){
        return affinity.get(path);
    }

    public void setAffinity(ValueContainer container){
        affinity.put(container.getIdentifier(),container);
    }

    public Collection<Identifier> getPaths(){
        return affinity.keySet();
    }

    public void encode(ByteBuf buf){
        ByteBufHelpers.encodeCollection(affinity.values(),buf,(data, byteBuf)-> {
            ValueContainer.encode(byteBuf,data);
        });
    }
    public static void decode(AffinityCategoryHolder holder,ByteBuf buf){
        holder.affinity.clear();
        List<ValueContainer> containers= ByteBufHelpers.decodeArray(buf, ValueContainer::decode);

        for (ValueContainer container : containers){
            holder.affinity.put(container.getIdentifier(),container);
        }
    }
}
