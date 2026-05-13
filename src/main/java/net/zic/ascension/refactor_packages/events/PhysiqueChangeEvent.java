package net.zic.ascension.refactor_packages.events;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.physiques.IPhysiqueData;


//the physique removed method is called BEFORE this event. same with physique added
public class PhysiqueChangeEvent  {

    public static class Pre extends Event implements ICancellableEvent {
        public final ResourceLocation oldPhysique;
        public final IPhysiqueData oldPhysiqueData;
        public final IEntityData entityData;

        private ResourceLocation newPhysique;

        public Pre(ResourceLocation oldPhysique, IPhysiqueData oldPhysiqueData,ResourceLocation newPhysique, IEntityData entityData){
            this.oldPhysique = oldPhysique;
            this.oldPhysiqueData = oldPhysiqueData;
            this.entityData = entityData;
            this.newPhysique = newPhysique;
        }


        public ResourceLocation getNewPhysique(){return newPhysique;}
        public void setNewPhysique(ResourceLocation physique){this.newPhysique =physique;}
    }
    public static class Post extends  Event{
        public final ResourceLocation oldPhysique;
        public final ResourceLocation newPhysique;
        public final IPhysiqueData oldPhysiqueData;
        public final IPhysiqueData newPhysiqueData;

        public final IEntityData entityData;

        public Post(ResourceLocation oldPhysique, ResourceLocation physique, IPhysiqueData oldPhysiqueData, IPhysiqueData newPhysiqueData, IEntityData entityData) {
            this.oldPhysique = oldPhysique;
            newPhysique = physique;
            this.oldPhysiqueData = oldPhysiqueData;
            this.newPhysiqueData = newPhysiqueData;
            this.entityData = entityData;
        }
        public Post(Pre event,IPhysiqueData newPhysiqueData){
            this(event.oldPhysique,event.newPhysique,event.oldPhysiqueData,newPhysiqueData,event.entityData);
        }
    }

}
