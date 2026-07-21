package net.zic.ascension.api.ascension.core.path.affinity;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Holds a generic AffinityCategoryHolder + a map of Identifier-> CategoryHolder
 */
public class AffinityHolder {

    private final AffinityCategoryHolder genericAffinity = new AffinityCategoryHolder();

    private final HashMap<Identifier,AffinityCategoryHolder> categorizedAffinity = new HashMap<>();


    public void addAffinity(Identifier path,double bonus){
       genericAffinity.addAffinity(path,bonus);
    }
    public void addAffinity(Identifier category,Identifier path,double bonus){
        categorizedAffinity.computeIfAbsent(category,key->new AffinityCategoryHolder());
        categorizedAffinity.get(category).addAffinity(path,bonus);
    }

    public void removeAffinity(Identifier path,double bonus){
        genericAffinity.removeAffinity(path,bonus);
    }
    public void removeAffinity(Identifier category,Identifier path,double bonus){
        categorizedAffinity.computeIfAbsent(category,key->new AffinityCategoryHolder());
        categorizedAffinity.get(category).removeAffinity(path,bonus);
    }

    public void addAffinityModifier(Identifier path, ValueContainerModifier modifier){
        genericAffinity.addAffinityModifier(path,modifier);
    }
    public void addAffinityModifier(Identifier category,Identifier path,ValueContainerModifier modifier){
        categorizedAffinity.computeIfAbsent(category,key->new AffinityCategoryHolder());
        categorizedAffinity.get(category).addAffinityModifier(path,modifier);
    }

    public void removeAffinityModifier(Identifier path,Identifier modifier){
        genericAffinity.removeAffinityModifier(path,modifier);
    }
    public void removeAffinityModifier(Identifier category,Identifier path,Identifier modifier){
        if(!categorizedAffinity.containsKey(category)) return;
        categorizedAffinity.get(category).removeAffinityModifier(path,modifier);
    }

    public double getAffinity(Identifier path){
        return genericAffinity.getAffinity(path);
    }
    public double getAffinity(Identifier category,Identifier path){
        return categorizedAffinity.containsKey(category) ? categorizedAffinity.get(category).getAffinity(path) : 0;
    }

    public double getBaseAffinity(Identifier path){
       return genericAffinity.getBaseAffinity(path);
    }
    public double getBaseAffinity(Identifier category,Identifier path){
        return categorizedAffinity.containsKey(category) ? categorizedAffinity.get(category).getBaseAffinity(path) : 0;
    }

    public boolean hasAffinity(Identifier path){
        return genericAffinity.hasAffinity(path);
    }
    public boolean hasAffinity(Identifier category,Identifier path){
        if(!categorizedAffinity.containsKey(category)) return false;
        return categorizedAffinity.get(category).hasAffinity(path);
    }

    public ValueContainer getAffinityContainer(Identifier path){
        return genericAffinity.getAffinityContainer(path);
    }
    public ValueContainer getAffinityContainer(Identifier category,Identifier path){
        return categorizedAffinity.containsKey(category) ? categorizedAffinity.get(category).getAffinityContainer(path) : null;
    }

    public void setAffinity(ValueContainer container){
        genericAffinity.setAffinity(container);
    }
    public void setAffinity(Identifier category,ValueContainer container){
        categorizedAffinity.computeIfAbsent(category,key->new AffinityCategoryHolder());
        categorizedAffinity.get(category).setAffinity(container);
    }

    public Collection<Identifier> getPaths(){
        return genericAffinity.getPaths();
    }
    public Collection<Identifier> getPaths(Identifier category){
        if(!categorizedAffinity.containsKey(category)) return List.of();
        return categorizedAffinity.get(category).getPaths();
    }

    public void encode(ByteBuf buf){
        genericAffinity.encode(buf);
        buf.writeInt(categorizedAffinity.size());
        for(Identifier category : categorizedAffinity.keySet()){
            ByteBufHelpers.encodeIdentifier(category,buf);
            categorizedAffinity.get(category).encode(buf);
        }

    }
    public void decode(ByteBuf buf){
        AffinityCategoryHolder.decode(genericAffinity,buf);
        categorizedAffinity.clear();
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            Identifier category = ByteBufHelpers.decodeIdentifier(buf);
            AffinityCategoryHolder holder = new AffinityCategoryHolder();
            AffinityCategoryHolder.decode(holder,buf);
            categorizedAffinity.put(category,holder);
        }
    }

}
