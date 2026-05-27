package net.zic.ascension.api.core;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.event.EventReason;
import net.zic.ascension.api.event.bloodline.BloodlineAddedEvent;
import net.zic.ascension.api.event.bloodline.BloodlineRemovedEvent;
import net.zic.ascension.api.event.path.PathAddedEvent;
import net.zic.ascension.api.event.path.PathRemovedEvent;
import net.zic.ascension.api.event.physique.PhysiqueChangedEvent;

import java.util.Collection;
import java.util.List;

/**
 * Exact same data as an origin source, but further expands upon server only behaviour
 *
 *
 * TODO for better sync behaviour i need to be able to mark different parts as dirty, then stage these syncs
 * TODO also have a flag for isLoading, if markDirty() and isLoading do not sync
 * TODO then do a big sync at the end
 * TODO using custom dirt flags for each thing i can select what to sync
 * TODO e.g for path i would have a HashSet of dirty paths, when sync is called only dirty paths are synced
 * TODO (this would be dirty sets and a set of removed sets)
 * TODO think of a way for the resolveDirty() to be able to check if it is allowed to
 * TODO e.g if physique marksDirty first and then marks a PathData dirty the path data cannot resolve until
 * TODO physique resolves
 * TODO maybe when we mark as dirty we provide an enum type? and same with resolve? and only if they match do we sync?
 * TODO this way we only sync when whatever started the process wants to sync
 * TODO so lets say i add a physique, even though we add stats, modifers skills and paths, it will not sync until physique calls resolve
 */
public class ServerOriginSource extends OriginSource {


    public ServerOriginSource(RegistryAccess access) {
        super(access);
    }

    @Override
    public boolean setPhysique(Identifier physique, PhysiqueData physiqueData, RegistryAccess access, EventReason reason) {
        if(physique == null) return false;
        Identifier oldPhysique = getPhysique();
        PhysiqueData oldPhysiqueData = getPhysiqueData();

        PhysiqueChangedEvent.Pre pre = new PhysiqueChangedEvent.Pre(oldPhysique,oldPhysiqueData,physique,physiqueData,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;
        if(pre.getNewPhysiqueIdentifier() == null) return false;


        Collection<Identifier> toRemove = oldPhysique == null ? List.of() : pre.getPhysique(access).onRemoved(this,oldPhysiqueData);

        boolean result = super.setPhysique(pre.getNewPhysiqueIdentifier(), pre.getPhysiqueData(),access,reason);
        if(!result) return false;

        Collection<Identifier> toAdd = pre.getNewPhysique(access).onAdded(this, pre.getNewPhysiqueData());

        PhysiqueChangedEvent.Post post = new PhysiqueChangedEvent.Post(oldPhysique,oldPhysiqueData,pre.getNewPhysiqueIdentifier(),pre.getNewPhysiqueData(),this,reason);
        NeoForge.EVENT_BUS.post(post);

        for(Identifier path : toRemove){
            if(toAdd.contains(path)) continue;
            removePath(path,access);
        }
        for(Identifier path : toAdd){
            if(toRemove.contains(path)) continue;
            addPath(path,access);
        }

        return true;
    }

    //TODO consider creating a replace bloodline event as well
    @Override
    public boolean addBloodline(Identifier bloodline, BloodlineData data, RegistryAccess access, EventReason reason) {
        if(bloodline == null) return false;
        if(hasBloodline(bloodline)) return false;

        BloodlineAddedEvent.Pre pre = new BloodlineAddedEvent.Pre(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addBloodline(bloodline, data, access,reason);
        if(!result) return false;

        Collection<Identifier> toAdd = pre.getBloodline(access).onAdded(this,pre.getBloodlineData());

        for(Identifier path : toAdd){
            addPath(path,access);
        }


        BloodlineAddedEvent.Post post= new BloodlineAddedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        return true;

    }
    //TODO consider adding a replace bloodline event
    @Override
    public boolean removeBloodline(Identifier bloodline, RegistryAccess access, EventReason reason) {
        if(bloodline == null) return false;
        if(!hasBloodline(bloodline)) return false;

        BloodlineData data = getBloodlineData(bloodline);
        BloodlineRemovedEvent.Pre pre = new BloodlineRemovedEvent.Pre(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;



        boolean result =  super.removeBloodline(bloodline, access,reason);

        if(!result) return false;

        Collection<Identifier> toRemove = pre.getBloodline(access).onRemoved(this,pre.getBloodlineData());

        for(Identifier path : toRemove){
            removePath(path,access);
        }

        BloodlineRemovedEvent.Post post= new BloodlineRemovedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        return true;
    }


    @Override
    public boolean addPath(Identifier path, PathData existingData, RegistryAccess registryAccess, EventReason reason) {
        if(path == null || existingData == null) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(registryAccess).containsKey(path)) return false;
        PathAddedEvent.Pre pre = new PathAddedEvent.Pre(path,existingData,this);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addPath(path, existingData, registryAccess, reason);
        if(!result) return false;

        existingData.simulateProgression(this,registryAccess);
        PathAddedEvent.Post post = new PathAddedEvent.Post(path,existingData,this);
        NeoForge.EVENT_BUS.post(post);

        return true;
    }

    @Override
    public boolean removePath(Identifier path, RegistryAccess access, EventReason reason) {
        if(path == null || !hasPath(path)) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(access).containsKey(path)) return false;
        PathData data = getPathData(path);
        PathRemovedEvent.Pre pre = new PathRemovedEvent.Pre(path,data,this);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.removePath(path, access, reason);
        if(!result) return false;

        data.removeFromSource(this,access);
        PathRemovedEvent.Post post = new PathRemovedEvent.Post(path,data,this);
        NeoForge.EVENT_BUS.post(post);

        return true;
    }
}
