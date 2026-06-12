package net.zic.ascension.api.core.path;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.path.interactions.PathInteraction;
import net.zic.ascension.api.core.path.interactions.PathInteractionType;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
    public double getRawAffinity(Identifier path){
        if(!affinity.containsKey(path)) return 0; //affinity is x(1+affinity)
        return affinity.get(path).getValue();
    }
    public double getAffinity(Identifier path){
        if(!affinity.containsKey(path)) return 0; //affinity is x(1+affinity)
        double initialValue = affinity.get(path).getValue();
        double finalValue = initialValue;
        Collection<PathInteraction> pathInteractions = AscensionCraft.getPathInteractionHolder().getTargetInteractionsFrom(
                path,
                affinity.keySet()
        );
        for(PathInteraction interaction : pathInteractions){
            if(!affinity.containsKey(interaction.pathA())) continue;
            Identifier sourcePath = interaction.pathA();
            if(interaction.type() == PathInteractionType.DESTRUCTIVE){

                double sourceAffinity = getRawAffinity(path);

                double value = 1/(1+sourceAffinity/initialValue*interaction.value());
            }else if(interaction.type() == PathInteractionType.GENERATIVE){
                double sourceAffinity = getRawAffinity(path);

                //TODO think about modifying this, because if the source affinity gets super large it is a good bonus
                //TODO but that could be "problematic" cus it is technically overpowering or affinity value
                //TODO consider another value type after affinity, called PathEffectValue? or something. this is the value that has been
                //TODO modified by all the things like interactions? since interactions dont directly interact with your affinity but your affinity effect?
                double value = (1+sourceAffinity/initialValue*interaction.value());
            }
        }
        return affinity.get(path).getValue();
    }


    public double getBaseAffinity(Identifier path){
        if(!affinity.containsKey(path)) return 1;
        return affinity.get(path).getBaseValue();
    }
    public ValueContainer getAffinityContainer(Identifier path){
        return affinity.get(path);
    }

    public void setAffinity(ValueContainer container){
        affinity.put(container.getIdentifier(),container);
    }

    public Collection<ValueContainer> getAllAffinityContainers() {
        return List.copyOf(affinity.values());
    }

    public void clear() {
        affinity.clear();
    }
}
