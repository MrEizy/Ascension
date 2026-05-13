package net.zic.ascension.refactor_packages.events.path_data;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;

public class TryRemovePathDataEvent extends Event implements ICancellableEvent {

    private final IEntityData entityData;
    private final ResourceLocation path;


    public TryRemovePathDataEvent(IEntityData entityData, ResourceLocation path) {
        this.entityData = entityData;
        this.path = path;
    }

    public IEntityData getEntityData(){
        return entityData;
    }
    public ResourceLocation getPath(){return path;}
}
