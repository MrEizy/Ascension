package net.thejadeproject.ascension.refactor_packages.stats;

import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainer;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

public class StatInstance extends ValueContainer {

    private final Stat stat;

    public StatInstance(Stat stat, double base) {
        super(AscensionRegistries.Stats.STATS_REGISTRY.getKey(stat), stat.getDisplayName(), base);
        this.stat = stat;
    }
    public StatInstance(ValueContainer container){
        super(container.getIdentifier(),container.getDisplayName(),container.getBaseValue());
        this.stat = AscensionRegistries.Stats.STATS_REGISTRY.get(container.getIdentifier());
        for(ValueContainerModifier modifier : container.getAllModifiers()){
            addModifier(modifier);
        }
    }

    public Stat getStat(){return stat;}


    public void log(){
//        //System.out.print(getDisplayName().getString()+" : ");
//        //System.out.print(getValue());
//        //System.out.println(" ("+getBaseValue()+")");
    }
}
