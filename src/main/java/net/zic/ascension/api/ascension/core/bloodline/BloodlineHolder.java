package net.zic.ascension.api.ascension.core.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class BloodlineHolder implements DataSourceInstance<BloodlineDataSource> {

    final HashMap<Identifier, BloodlineData> bloodlines = new HashMap<>();

    final HashSet<Identifier> dirtyBloodlines = new HashSet<>();
    final HashSet<Identifier> toRemoveBloodlines = new HashSet<>();
    public boolean addBloodline(Identifier bloodline,BloodlineData data){
        if(hasBloodline(bloodline)) return false;
        bloodlines.put(bloodline,data);
        dirtyBloodlines.add(bloodline);
        return true;
    }
    public boolean removeBloodline(Identifier bloodline){
        if(hasBloodline(bloodline)){
            toRemoveBloodlines.add(bloodline);
            dirtyBloodlines.remove(bloodline);
            bloodlines.remove(bloodline);
            return true;
        }
        return false;
    }
    public boolean hasBloodline(Identifier bloodline){
        return bloodlines.containsKey(bloodline);
    }

    public BloodlineData getBloodline(Identifier bloodline){
        return bloodlines.get(bloodline);
    }
    public Bloodline getBloodline(Identifier bloodline, RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodline,access);
    }
    public Collection<Identifier> getBloodlines(){
        return bloodlines.keySet();
    }

    public void markBloodlineDirty(Identifier bloodline){
        if(hasBloodline(bloodline)) dirtyBloodlines.add(bloodline);
    }
    @Override
    public BloodlineDataSource getDataSource() {
        return CoreDataSources.BLOODLINE_DATA_SOURCE.get();
    }

}
