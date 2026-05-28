package net.zic.ascension.api.event.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;

//TODO update all events to hold RegistryAccess by default
public abstract class TechniqueChangedEvent extends Event {

    private final Identifier technique;
    private final TechniqueData techniqueData;
    private Identifier newTechnique;
    private TechniqueData newTechniqueData;
    private final OriginSource source;

    protected TechniqueChangedEvent(Identifier technique, TechniqueData techniqueData,Identifier newTechnique,TechniqueData newTechniqueData, OriginSource source) {
        this.technique = technique;
        this.techniqueData = techniqueData;
        this.source = source;
        this.newTechnique = newTechnique;
        this.newTechniqueData = newTechniqueData;
    }

    public Identifier getTechniqueIdentifier(){
        return technique;
    }
    public Technique getTechnique(RegistryAccess access){
        return CoreRegistries.TECHNIQUE_REGISTRY.get(access).getValue(getTechniqueIdentifier());
    }
    public TechniqueData getTechniqueData(){
        return techniqueData;
    }

    public Identifier getNewTechniqueIdentifier(){
        return newTechnique;
    }
    public Technique getNewTechnique(RegistryAccess access){
        return CoreRegistries.TECHNIQUE_REGISTRY.get(access).getValue(getNewTechniqueIdentifier());
    }
    public TechniqueData getNewTechniqueData(){
        return newTechniqueData;
    }

    protected void setNewTechnique(Identifier technique,RegistryAccess access){
        this.newTechnique = technique;
        if(getNewTechniqueIdentifier() == null){
            setNewTechniqueData(null);
            return;
        }
        setNewTechniqueData(getNewTechnique(access).newData());
    }
    protected void setNewTechniqueData(TechniqueData techniqueData){
        this.newTechniqueData = techniqueData;
    }

    public OriginSource getSource(){return source;}

    public static class Pre extends TechniqueChangedEvent implements ICancellableEvent{

        public Pre(Identifier technique, TechniqueData techniqueData, Identifier newTechnique, TechniqueData newTechniqueData, OriginSource source) {
            super(technique, techniqueData, newTechnique, newTechniqueData, source);
        }

        @Override
        public void setNewTechniqueData(TechniqueData techniqueData) {
            super.setNewTechniqueData(techniqueData);
        }

        @Override
        public void setNewTechnique(Identifier technique, RegistryAccess access) {
            super.setNewTechnique(technique, access);
        }
    }

    public static class Post extends TechniqueChangedEvent{

        public Post(Identifier technique, TechniqueData techniqueData, Identifier newTechnique, TechniqueData newTechniqueData, OriginSource source) {
            super(technique, techniqueData, newTechnique, newTechniqueData, source);
        }
    }
}
