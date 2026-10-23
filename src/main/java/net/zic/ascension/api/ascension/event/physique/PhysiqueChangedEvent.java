package net.zic.ascension.api.ascension.event.physique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.physique.PhysiqueInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

/**
 * An event that is called when the physique on an origin source is changed
 *
 * is split into a pre event (can be cancelled)
 * and a post event (cannot be cancelled)
 *
 */
public abstract class PhysiqueChangedEvent extends PhysiqueEvent{
    private @Nullable PhysiqueInstance<?> newInstance;

    protected PhysiqueChangedEvent(@Nullable PhysiqueInstance<?> instance, @Nullable PhysiqueInstance<?> newInstance, OriginSource source) {
        super(instance, source);
        this.newInstance = newInstance;
    }

    protected void setNewPhysiqueInstance(@Nullable PhysiqueInstance<?> newInstance){
        this.newInstance = newInstance;
    }

    //null when the physique is being removed
    public @Nullable PhysiqueInstance<?> getNewPhysiqueInstance(){
        return newInstance;
    }
    public @Nullable Identifier getNewPhysiqueIdentifier(){
        return newInstance == null ? null : newInstance.getPhysiqueId();
    }
    public @Nullable Physique<?> getNewPhysique(){
        return newInstance == null ? null : newInstance.getPhysique();
    }
    public @Nullable PhysiqueData getNewPhysiqueData(){
        return newInstance == null ? null : newInstance.getData();
    }

    public static class Pre extends PhysiqueChangedEvent implements ICancellableEvent {


        public Pre(@Nullable PhysiqueInstance<?> instance, @Nullable PhysiqueInstance<?> newInstance, OriginSource source) {
            super(instance, newInstance, source);
        }

        //replaces the new physique with a fresh instance of physique, null removes the physique instead
        public void setNewPhysique(@Nullable Identifier physique, RegistryAccess access){
            setNewPhysiqueInstance(physique == null ? null : PhysiqueInstance.create(physique, access));
        }

        @Override
        public void setNewPhysiqueInstance(@Nullable PhysiqueInstance<?> newInstance) {
            super.setNewPhysiqueInstance(newInstance);
        }
    }
    public static class Post extends PhysiqueChangedEvent {


        public Post(@Nullable PhysiqueInstance<?> instance, @Nullable PhysiqueInstance<?> newInstance, OriginSource source) {
            super(instance, newInstance, source);
        }
    }
}
