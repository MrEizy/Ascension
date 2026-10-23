package net.zic.ascension.api.ascension.event.physique;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.physique.PhysiqueInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

public abstract class PhysiqueEvent extends Event {

    private final @Nullable PhysiqueInstance<?> instance;
    private final OriginSource source;


    protected PhysiqueEvent(@Nullable PhysiqueInstance<?> instance, OriginSource source) {
        this.instance = instance;
        this.source = source;
    }
    //null when the origin held no physique
    public @Nullable PhysiqueInstance<?> getPhysiqueInstance(){
        return instance;
    }
    public @Nullable Identifier getPhysiqueIdentifier(){
        return instance == null ? null : instance.getPhysiqueId();
    }
    public @Nullable Physique<?> getPhysique(){
        return instance == null ? null : instance.getPhysique();
    }
    public @Nullable PhysiqueData getPhysiqueData(){
        return instance == null ? null : instance.getData();
    }
    public OriginSource getSource(){
        return source;
    }

}
