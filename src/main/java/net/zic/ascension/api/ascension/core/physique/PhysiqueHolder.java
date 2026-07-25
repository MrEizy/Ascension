package net.zic.ascension.api.ascension.core.physique;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

public class PhysiqueHolder implements DataSourceInstance {

    private Identifier physique;
    private PhysiqueData data;

    public Identifier getPhysique(){
        return physique;
    }
    public PhysiqueData getData(){
        return data;
    }
    public Physique getPhysique(Level level){
        return CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,getPhysique(),level.registryAccess());
    }

    public void setPhysique(Identifier id,PhysiqueData data){
        physique = id;
        this.data = data;
    }

    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.get();
    }
}
