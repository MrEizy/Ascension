package net.zic.ascension.refactor_packages.stats;

import net.minecraft.network.chat.Component;

public class Stat {

    private final Component displayName;
    private final Component shortName;
    private final Component description;

    private final int MAX_VALUE = Integer.MAX_VALUE; //TODO update to pull from a config
    private final int MIN_VALUE = 0; //HARD LIMIT

    public Stat(Component displayName,Component shortName, Component description) {
        this.displayName = displayName;
        this.description = description;
        this.shortName = shortName;
    }

    public StatInstance instance(){return new StatInstance(this,0);}
    public Component getDisplayName(){
        return displayName;
    }
    public Component getDescription(){
        return description;
    }
    public Component getShortName(){return this.shortName;}
    public int getMaxValue(){
        return MAX_VALUE;
    }
    public int getMinValue(){
        return MIN_VALUE;
    }
}
