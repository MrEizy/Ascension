package net.zic.ascension.api.ascension.core.physique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

public class PhysiqueHolder implements DataSourceInstance<PhysiqueDataSource> {

    Identifier physique;
    PhysiqueData data;

    public Identifier getPhysique(){
        return physique;
    }
    public PhysiqueData getData(){
        return data;
    }
    public Physique getPhysique(RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,getPhysique(),access);
    }

    public boolean setPhysique(Identifier id,PhysiqueData data){
        if((id != null && id.equals(physique)) || (id == null && physique == null)) return false;
        physique = id;
        this.data = data;
        return true;
    }


    @Override
    public PhysiqueDataSource getDataSource() {
        return null;//TODO CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.get();
    }
}
