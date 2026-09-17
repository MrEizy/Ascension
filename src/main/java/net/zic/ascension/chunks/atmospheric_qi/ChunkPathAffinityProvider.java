package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.*;

//TODO change to use new value containers
public class ChunkPathAffinityProvider implements PathBonusProvider{
    public static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");

    private final Map<Identifier,ValueContainer> affinities = new HashMap<>();

    protected ValueContainer getOrCreate(Identifier path){
        return affinities.computeIfAbsent(path,(key)->new ValueContainer(path,0));
    }

    public void addAffinity(Identifier path,double val){
        getOrCreate(path).setBaseValue(val);
    }
    public void addAffinityModifier(Identifier path, ValueContainerModifier modifier){
        getOrCreate(path).addModifier(modifier);
    }

    public void removeAffinity(Identifier path,double val){
        if(!affinities.containsKey(path)) return;
        addAffinity(path,-val);
    }

    public void removeAffinityModifier(Identifier path,Identifier modifier){
        if(!affinities.containsKey(path)) return;
        affinities.get(path).removeModifier(modifier);
    }




    public double getAffinity(Identifier path) {
        return affinities.containsKey(path) ?  affinities.get(path).getValue() :0;
    }
    public Collection<Identifier> getAllAffinities(){
        return affinities.keySet();
    }

    @Override
    public ValueContainer getPathBonusContainer(Identifier category, Identifier path) {
        return category.equals(AFFINITY_CATEGORY) ?  affinities.get(path) : null;
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return category.equals(AFFINITY_CATEGORY)&& affinities.containsKey(path) ?  affinities.get(path).getValue() :0;
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        List<PathBonus> pathBonuses = new ArrayList<>();
        affinities.keySet().forEach(path->pathBonuses.add(new PathBonus(AFFINITY_CATEGORY,path)));
        return pathBonuses;
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return category.equals(AFFINITY_CATEGORY) ? affinities.keySet() : List.of();
    }

}
