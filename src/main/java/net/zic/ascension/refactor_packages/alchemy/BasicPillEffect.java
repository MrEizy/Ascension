package net.zic.ascension.refactor_packages.alchemy;

import net.minecraft.network.chat.Component;

public  abstract class BasicPillEffect implements IPillEffect {
    private final Component name;
    private final Component description;

    public BasicPillEffect(Component name,Component description){
        this.name = name;
        this.description =description;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public Component getName() {
        return name;
    }
}
