package net.zic.ascension.refactor_packages.events;

import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.techniques.ITechniqueData;

public class TechniqueChangeEvent {
    public static class Pre{
        public final ResourceLocation oldTechnique;
        public final ITechniqueData oldTechniqueData;
        public final IEntityData entityData;
        private ResourceLocation newTechnique;

        public Pre(ResourceLocation oldTechnique, ITechniqueData oldTechniqueData, ResourceLocation newTechnique, IEntityData entityData){
            this.oldTechnique = oldTechnique;
            this.newTechnique = newTechnique;
            this.oldTechniqueData = oldTechniqueData;
            this.entityData = entityData;

        }
        public ResourceLocation getNewTechnique(){return this.newTechnique;}
        public void setNewTechnique(ResourceLocation newTechnique){this.newTechnique =newTechnique;}
    }
    public static class Post{
        public final ResourceLocation oldTechnique;
        public final ITechniqueData oldTechniqueData;
        public final IEntityData entityData;
        public final ResourceLocation newTechnique;
        public final ITechniqueData newTechniqueData;

        public Post(ResourceLocation oldTechnique, ITechniqueData oldTechniqueData, IEntityData entityData, ResourceLocation newTechnique, ITechniqueData newTechniqueData){

            this.oldTechnique = oldTechnique;
            this.oldTechniqueData = oldTechniqueData;
            this.entityData = entityData;
            this.newTechnique = newTechnique;
            this.newTechniqueData = newTechniqueData;
        }
        public Post(Pre event, ITechniqueData techniqueData){
            this(event.oldTechnique,event.oldTechniqueData,event.entityData,event.getNewTechnique(),techniqueData);
        }
    }
}
