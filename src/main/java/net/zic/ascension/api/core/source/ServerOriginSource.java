package net.zic.ascension.api.core.source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.event.EventReason;
import net.zic.ascension.api.event.bloodline.BloodlineAddedEvent;
import net.zic.ascension.api.event.bloodline.BloodlineRemovedEvent;
import net.zic.ascension.api.event.path.PathAddedEvent;
import net.zic.ascension.api.event.path.PathRemovedEvent;
import net.zic.ascension.api.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.core.source.SourceHandler;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.*;

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
    //──Sync Data────────────────────────────────────────────────────────
    private boolean physiqueDirty = true;

    private final HashMap<Identifier,BloodlineData> toAddBloodlines = new HashMap<>();
    private final HashSet<Identifier> toRemoveBloodlines = new HashSet<>();
    private final HashMap<Identifier,PathData> toAddPaths= new HashMap<>();
    private final HashSet<Identifier> toRemovePaths = new HashSet<>();
    private final HashMap<Identifier, SkillData> toAddSkills= new HashMap<>();
    private final HashSet<Identifier> toRemoveSkills = new HashSet<>();
    private final HashMap<Identifier, DataSourceInstance> toAddDataSources = new HashMap<>();
    private final HashSet<Identifier> toRemoveDataSources = new HashSet<>();
    private final HashSet<StatInstance> dirtyStats = new HashSet<>();
    private final HashSet<ValueContainer> dirtyAffinity = new HashSet<>();


    private ProcessType currentProcess = null;

    public ServerOriginSource(RegistryAccess access) {
        super(access);
    }
    public ServerOriginSource(RegistryAccess access, ValueInput input){
        super(access,input);
    }
    public ServerOriginSource(ValueInput input){
        super(input);
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



        boolean result = super.setPhysique(pre.getNewPhysiqueIdentifier(), pre.getPhysiqueData(),access,reason);
        if(!result) return false;

        physiqueDirty = true;

        startProcess(ProcessType.PHYSIQUE);

        Collection<Identifier> toRemove = oldPhysique == null ? List.of() : pre.getPhysique(access).onRemoved(this,oldPhysiqueData);

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
        resolveProcess(ProcessType.PHYSIQUE);
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

        startProcess(ProcessType.ADD_BLOODLINE);

        Collection<Identifier> toAdd = pre.getBloodline(access).onAdded(this,pre.getBloodlineData());

        for(Identifier path : toAdd){
            addPath(path,access);
        }


        BloodlineAddedEvent.Post post= new BloodlineAddedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        toAddBloodlines.put(bloodline,data);
        resolveProcess(ProcessType.ADD_BLOODLINE);
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

        startProcess(ProcessType.REMOVE_BLOODLINE);

        Collection<Identifier> toRemove = pre.getBloodline(access).onRemoved(this,pre.getBloodlineData());

        for(Identifier path : toRemove){
            removePath(path,access);
        }

        BloodlineRemovedEvent.Post post= new BloodlineRemovedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        toRemoveBloodlines.add(bloodline);
        resolveProcess(ProcessType.REMOVE_BLOODLINE);
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

        startProcess(ProcessType.ADD_PATH);

        existingData.simulateProgression(this,registryAccess);
        PathAddedEvent.Post post = new PathAddedEvent.Post(path,existingData,this);
        NeoForge.EVENT_BUS.post(post);

        toAddPaths.put(path,existingData);
        resolveProcess(ProcessType.ADD_PATH);
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

        startProcess(ProcessType.REMOVE_PATH);
        data.removeFromSource(this,access);
        PathRemovedEvent.Post post = new PathRemovedEvent.Post(path,data,this);
        NeoForge.EVENT_BUS.post(post);

        toRemovePaths.add(path);
        resolveProcess(ProcessType.REMOVE_PATH);
        return true;
    }
    //──Network────────────────────────────────────────────────────────


    @Override
    public void addStat(Stat stat, double val) {
        super.addStat(stat, val);
        dirtyStats.add(getStatInstance(stat));
        startProcess(ProcessType.STAT);
        resolveProcess(ProcessType.STAT);
    }

    @Override
    public void removeStat(Stat stat, double val) {
        super.removeStat(stat, val);
        dirtyStats.add(getStatInstance(stat));
        startProcess(ProcessType.STAT);
        resolveProcess(ProcessType.STAT);
    }

    @Override
    public void addStatModifier(Stat stat, ValueContainerModifier modifier) {
        super.addStatModifier(stat, modifier);
        dirtyStats.add(getStatInstance(stat));
        startProcess(ProcessType.STAT);
        resolveProcess(ProcessType.STAT);
    }

    @Override
    public void removeStatModifier(Stat stat, Identifier identifier) {
        super.removeStatModifier(stat, identifier);
        dirtyStats.add(getStatInstance(stat));
        startProcess(ProcessType.STAT);
        resolveProcess(ProcessType.STAT);
    }

    @Override
    public void addAffinity(Identifier path, double val) {
        super.addAffinity(path, val);
        dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        startProcess(ProcessType.AFFINITY);
        resolveProcess(ProcessType.AFFINITY);
    }

    @Override
    public void removeAffinity(Identifier path, double val) {
        super.removeAffinity(path, val);
        dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        startProcess(ProcessType.AFFINITY);
        resolveProcess(ProcessType.AFFINITY);
    }

    @Override
    public void addAffinityModifier(Identifier path, ValueContainerModifier modifier) {
        super.addAffinityModifier(path, modifier);
        dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        startProcess(ProcessType.AFFINITY);
        resolveProcess(ProcessType.AFFINITY);
    }

    @Override
    public void removeAffinityModifier(Identifier path, Identifier modifier) {
        super.removeAffinityModifier(path, modifier);
        dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        startProcess(ProcessType.AFFINITY);
        resolveProcess(ProcessType.AFFINITY);
    }

    @Override
    public void markPhysiqueDirty() {
        physiqueDirty = true;
        startProcess(ProcessType.MODIFY_PHYSIQUE);
        resolveProcess(ProcessType.MODIFY_PHYSIQUE);
    }

    @Override
    public void markBloodlineDirty(Identifier bloodline) {
        toAddBloodlines.put(bloodline,getBloodlineData(bloodline));
        startProcess(ProcessType.MODIFY_BLOODLINE);
        resolveProcess(ProcessType.MODIFY_BLOODLINE);
    }

    @Override
    public void markPathDirty(Identifier path) {
        toAddPaths.put(path,getPathData(path));
        startProcess(ProcessType.MODIFY_PATH);
        resolveProcess(ProcessType.MODIFY_PATH);
    }

    @Override
    public void markSkillDirty(Identifier skill) {
        toAddSkills.put(skill,getSkillData(skill));
        startProcess(ProcessType.MODIFY_SKILL);
        resolveProcess(ProcessType.MODIFY_SKILL);
    }

    @Override
    public void markDataSourceDirty(Identifier source) {
        toAddDataSources.put(source,getDataSourceInstance(source));
        startProcess(ProcessType.MODIFY_DATA_SOURCE);
        resolveProcess(ProcessType.MODIFY_DATA_SOURCE);
    }



    public void startProcess(ProcessType processType){
        if(currentProcess == null) return;

        currentProcess = processType;
    }

    //only resolve if the process matches the current one
    public void resolveProcess(ProcessType processType){
        if(processType != this.currentProcess) return;
        currentProcess = null;
        sync();

    }
    protected void sync(){
        Collection<LivingEntity> entities = SourceHandler.getLoadedWatchers(this);
        SourceChangesSnapshot snapshot = new SourceChangesSnapshot(
                physiqueDirty ? getPhysique() : null,
                physiqueDirty ? getPhysiqueData() : null,
                Map.copyOf(toAddBloodlines),
                Set.copyOf(toRemoveBloodlines),
                Map.copyOf(toAddPaths),
                Set.copyOf(toRemovePaths),
                Map.copyOf(toAddSkills),
                Set.copyOf(toRemoveSkills),
                Map.copyOf(toAddDataSources),
                Set.copyOf(toRemoveDataSources),
                Set.copyOf(dirtyStats),
                Set.copyOf(dirtyAffinity)
        );
        for(LivingEntity entity : entities){
            AscensionEntityDataHolder holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
            if(holder == null) continue;
            holder.getData(entity).markDirty(snapshot);
        }
        //clear sync caches

        physiqueDirty = false;
        toAddBloodlines.clear();
        toRemoveBloodlines.clear();
        toAddPaths.clear();
        toRemovePaths.clear();
        toAddSkills.clear();
        toRemoveSkills.clear();
        toAddDataSources.clear();
        toRemoveDataSources.clear();
        dirtyStats.clear();
        dirtyAffinity.clear();
    }
}
