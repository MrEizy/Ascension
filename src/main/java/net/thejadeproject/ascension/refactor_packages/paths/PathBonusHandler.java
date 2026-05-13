package net.thejadeproject.ascension.refactor_packages.paths;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainer;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

import java.util.HashMap;

public class PathBonusHandler {

    private final HashMap<ResourceLocation, ValueContainer> pathBonuses = new HashMap<>();


    public void addPathBonus(ResourceLocation path,double bonus){
        if(!pathBonuses.containsKey(path)){
            pathBonuses.put(path,new ValueContainer(path, AscensionRegistries.Paths.PATHS_REGISTRY.get(path).getDisplayTitle(),1));
        }
        pathBonuses.get(path).setBaseValue(pathBonuses.get(path).getBaseValue()+bonus);
    }
    public void removePathBonus(ResourceLocation path,double bonus){
        if(!pathBonuses.containsKey(path)) return;
        addPathBonus(path,-bonus);
    }
    public void addPathBonusModifier(ResourceLocation path, ValueContainerModifier modifier){
        addPathBonus(path,0);
        pathBonuses.get(path).addModifier(modifier);
    }
    public void removePathBonusModifier(ResourceLocation path,ResourceLocation modifier){
        if(!pathBonuses.containsKey(path))return;
        pathBonuses.get(path).removeModifier(modifier);
    }

    public double getPathBonus(ResourceLocation path){
        //TODO handle all path interactions
        if(!pathBonuses.containsKey(path))return 1;
        return pathBonuses.get(path).getValue();
    }
}
