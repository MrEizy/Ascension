package net.thejadeproject.ascension.refactor_packages.events;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainer;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

import java.util.ArrayList;
import java.util.List;

public class CultivateEvent extends Event implements ICancellableEvent {
    private final Entity entity;
    private final ResourceLocation path;
    private final List<ResourceLocation> attributedPaths;
    private final ValueContainer container;


    public CultivateEvent(Entity entity, double baseRate, ResourceLocation path, List<ResourceLocation> attributedPaths) {
        this.entity = entity;
        this.path = path;
        this.attributedPaths = attributedPaths;
        this.container = new ValueContainer(
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"cultivate_event"),
                Component.empty(),
                baseRate
        );
    }

    public double getBaseRate(){return container.getBaseValue();}
    public double getRate(){return container.getValue();}
    public void addModifier(ValueContainerModifier modifier){container.addModifier(modifier);}
    public void removeModifier(ResourceLocation identifier){container.removeModifier(identifier);}
    public Entity getEntity(){return entity;}
    public ResourceLocation getPath(){return path;}
    public List<ResourceLocation> getAttributedPaths(){return attributedPaths;}

    public void log(){
        //System.out.print("cultivated ");
        //System.out.print(AscensionRegistries.Paths.PATHS_REGISTRY.get(path).getDisplayTitle().getString());
        //System.out.print(" with attributes ");
//        for(ResourceLocation attributedPath : getAttributedPaths()){
//            //System.out.println(AscensionRegistries.Paths.PATHS_REGISTRY.get(attributedPath).getDisplayTitle().getString());
//            //System.out.print(" ");
//        }
//        //System.out.println("for "+getRate() + " qi");
    }
}
