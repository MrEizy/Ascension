package net.zic.ascension.refactor_packages.stats.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.stats.Stat;

public class StatChangedEvent extends Event {

    private final IEntityData entityData;
    private final ResourceLocation form;
    private final Stat stat;

    public StatChangedEvent(IEntityData entityData,ResourceLocation form, Stat stat){
        this.entityData = entityData;
        this.form = form;
        this.stat = stat;
    }

    public ResourceLocation getForm(){return form;}
    public IEntityData getEntityData() {
        return entityData;
    }

    public Stat getStat() {
        return stat;
    }
}
