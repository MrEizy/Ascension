package net.zic.ascension.api.core.source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.path.PathEffectValueUtil;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.ascension.api.event.EventReason;
import net.zic.ascension.api.event.bloodline.BloodlineAddedEvent;
import net.zic.ascension.api.event.bloodline.BloodlineRemovedEvent;
import net.zic.ascension.api.event.path.PathAddedEvent;
import net.zic.ascension.api.event.path.PathRemovedEvent;
import net.zic.ascension.api.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.api.event.skill.SkillAddedEvent;
import net.zic.ascension.api.event.skill.SkillRemovedEvent;
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
//TODO go through and update EVERYTHING to not rely on a registry access stored inside OriginSource, if it needs it the method should accept registryaccess
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
    private final HashMap<Identifier,HashSet<ValueContainer>> dirtyCategorizedAffinity = new HashMap<>();

    private ProcessType currentProcess = null;


    public ServerOriginSource(){}
    public ServerOriginSource(ValueInput input){
        super(input);
    }
    public ServerOriginSource(CompoundTag input){
        super(input);
    }





    @Override
    public boolean setPhysique(Identifier physique, PhysiqueData physiqueData, EventReason reason) {
        if(physique == null) return false;
        Identifier oldPhysique = getPhysique();
        PhysiqueData oldPhysiqueData = getPhysiqueData();

        PhysiqueChangedEvent.Pre pre = new PhysiqueChangedEvent.Pre(oldPhysique,oldPhysiqueData,physique,physiqueData,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;
        if(pre.getNewPhysiqueIdentifier() == null) return false;



        boolean result = super.setPhysique(pre.getNewPhysiqueIdentifier(), pre.getPhysiqueData(),reason);
        if(!result) return false;

        physiqueDirty = true;

        startProcess(ProcessType.PHYSIQUE);

        Collection<Identifier> toRemove = oldPhysique == null ? List.of() : pre.getPhysique(getRegistryAccess()).onRemoved(this,oldPhysiqueData);
        if(pre.getPhysique(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getPhysique(getRegistryAccess()).removeFromEntity(entity,oldPhysiqueData);
            }
        }
        Collection<Identifier> toAdd = pre.getNewPhysique(getRegistryAccess()).onAdded(this, pre.getNewPhysiqueData());
        if(pre.getNewPhysique(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getNewPhysique(getRegistryAccess()).applyToEntity(entity,pre.getNewPhysiqueData());
            }
        }
        PhysiqueChangedEvent.Post post = new PhysiqueChangedEvent.Post(oldPhysique,oldPhysiqueData,pre.getNewPhysiqueIdentifier(),pre.getNewPhysiqueData(),this,reason);
        NeoForge.EVENT_BUS.post(post);


        //first add all new paths with the new physique as owner, this ensures that if there is path overlap there is owners >1

        for(Identifier path : toAdd){
            if(toRemove.contains(path)) continue;
            addPath(path,post.getNewPhysiqueIdentifier());
        }
        for(Identifier path : toRemove){
            if(toAdd.contains(path)) continue;
            removePath(path,oldPhysique);
        }
        resolveProcess(ProcessType.PHYSIQUE);
        return true;
    }

    //TODO consider creating a replace bloodline event as well
    @Override
    public boolean addBloodline(Identifier bloodline, BloodlineData data , EventReason reason) {
        if(bloodline == null) return false;
        if(hasBloodline(bloodline)) {
            mergeBloodline(bloodline,data);
            return true;
        };

        BloodlineAddedEvent.Pre pre = new BloodlineAddedEvent.Pre(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addBloodline(bloodline, data,reason);
        if(!result) return false;

        startProcess(ProcessType.ADD_BLOODLINE);
        int purity = data.getPurity();
        data.setPurity(1);
        Collection<Identifier> toAdd = pre.getBloodline(getRegistryAccess()).onAdded(this,pre.getBloodlineData());

        if(pre.getBloodline(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getBloodline(getRegistryAccess()).applyToEntity(entity,pre.getBloodlineData());
            }
        }
        pre.getBloodline(getRegistryAccess()).handlePurityChange(this,data,purity);

        for(Identifier path : toAdd){
            addPath(path,pre.getBloodlineIdentifier());
        }



        BloodlineAddedEvent.Post post= new BloodlineAddedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        toAddBloodlines.put(bloodline,data);
        resolveProcess(ProcessType.ADD_BLOODLINE);
        return true;

    }
    //TODO consider adding a replace bloodline event
    @Override
    public boolean removeBloodline(Identifier bloodline, EventReason reason) {
        if(bloodline == null) return false;
        if(!hasBloodline(bloodline)) return false;

        BloodlineData data = getBloodlineData(bloodline);
        BloodlineRemovedEvent.Pre pre = new BloodlineRemovedEvent.Pre(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;



        boolean result =  super.removeBloodline(bloodline,reason);
        if(!result) return false;

        startProcess(ProcessType.REMOVE_BLOODLINE);
        pre.getBloodline(getRegistryAccess()).handlePurityChange(this,data,1);

        Collection<Identifier> toRemove = pre.getBloodline(getRegistryAccess()).onRemoved(this,pre.getBloodlineData());
        if(pre.getBloodline(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getBloodline(getRegistryAccess()).removeFromEntity(entity,pre.getBloodlineData());
            }
        }
        for(Identifier path : toRemove){
            removePath(path,pre.getBloodlineIdentifier());
        }

        BloodlineRemovedEvent.Post post= new BloodlineRemovedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        toRemoveBloodlines.add(bloodline);
        resolveProcess(ProcessType.REMOVE_BLOODLINE);
        return true;
    }


    @Override
    public boolean addPath(Identifier path, PathData existingData,Identifier owner, EventReason reason) {
        if(path == null || existingData == null) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(getRegistryAccess()).containsKey(path)) return false;
        if(cachedPathData.containsKey(path)) existingData = cachedPathData.get(path);
        PathAddedEvent.Pre pre = new PathAddedEvent.Pre(path,existingData,this);

        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addPath(path, existingData,owner, reason);
        if(!result) return false;

        startProcess(ProcessType.ADD_PATH);

        existingData.simulateProgression(this);
        PathAddedEvent.Post post = new PathAddedEvent.Post(path,existingData,this);
        NeoForge.EVENT_BUS.post(post);

        toAddPaths.put(path,existingData);
        resolveProcess(ProcessType.ADD_PATH);
        return true;
    }

    @Override
    public boolean broadcastTechniqueAddedAttempt(Identifier technique, TechniqueData data) {
        return super.broadcastTechniqueAddedAttempt(technique, data);
    }

    @Override
    public void broadcastTechniqueAdded(Identifier technique, TechniqueData data) {
        super.broadcastTechniqueAdded(technique, data);
    }

    @Override
    public boolean broadcastTechniqueRemovedAttempt(Identifier technique, TechniqueData data) {
        return super.broadcastTechniqueRemovedAttempt(technique, data);
    }

    @Override
    public void broadcastTechniqueRemoved(Identifier technique, TechniqueData data) {
        super.broadcastTechniqueRemoved(technique, data);
    }

    @Override
    public boolean removePath(Identifier path,Identifier owner, EventReason reason) {
        if(path == null || !hasPath(path)) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(getRegistryAccess()).containsKey(path)) return false;
        PathData data = getPathData(path);
        PathRemovedEvent.Pre pre = new PathRemovedEvent.Pre(path,data,this);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.removePath(path,owner, reason);
        if(!result) return false;

        startProcess(ProcessType.REMOVE_PATH);
        data.removeFromSource(this);
        PathRemovedEvent.Post post = new PathRemovedEvent.Post(path,data,this);
        NeoForge.EVENT_BUS.post(post);

        toRemovePaths.add(path);
        resolveProcess(ProcessType.REMOVE_PATH);
        return true;
    }


    @Override
    public boolean addSkill(Identifier skill, SkillData data,Identifier owner) {
        if(skill == null) return false;
        if(!CoreRegistries.SKILL_REGISTRY.get(getRegistryAccess()).containsKey(skill)) return false;
        if(cachedSkillData.containsKey(skill))  data = cachedSkillData.get(skill);
        SkillAddedEvent.Pre pre = new SkillAddedEvent.Pre(this,skill,data,null);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addSkill(skill, data,owner);
        if(!result) return false;

        startProcess(ProcessType.ADD_SKILL);
        if(pre.getSkill(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getSkill(getRegistryAccess()).applyToEntity(entity,pre.getSkillData());
            }
        }
        pre.getSkill(getRegistryAccess()).onAdded(this,data);
        SkillAddedEvent.Post post = new SkillAddedEvent.Post(this,skill,data,null);
        NeoForge.EVENT_BUS.post(post);

        toAddSkills.put(skill,data);
        resolveProcess(ProcessType.ADD_SKILL);
        return true;
    }
    //TODO updated to include EventReason
    @Override
    public boolean removeSkill(Identifier skill,Identifier owner) {
        if(skill == null || !hasSkill(skill)) return false;
        if(!CoreRegistries.SKILL_REGISTRY.get(getRegistryAccess()).containsKey(skill)) return false;
        SkillData data = getSkillData(skill);
        SkillRemovedEvent.Pre pre = new SkillRemovedEvent.Pre(this,skill,data,null);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.removeSkill(skill,owner);
        if(!result) return false;

        startProcess(ProcessType.REMOVE_SKILL);
        pre.getSkill(getRegistryAccess()).onRemoved(this,data);
        if(pre.getSkill(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getSkill(getRegistryAccess()).removeFromEntity(entity,pre.getSkillData());
            }
        }
        SkillRemovedEvent.Post post = new SkillRemovedEvent.Post(this,skill,data,null);
        NeoForge.EVENT_BUS.post(post);

        toRemoveSkills.add(skill);
        resolveProcess(ProcessType.REMOVE_SKILL);
        return true;
    }

    /**
     * Changes a toggleable skill state and synchronizes the state together with
     * any source modifiers changed by the skill.
     */
    public boolean setSkillEnabled(
            LivingEntity initiator,
            Identifier skillId,
            boolean enabled
    ) {
        if (initiator == null || skillId == null || !hasSkill(skillId)) {
            return false;
        }

        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                getRegistryAccess()
        );
        SkillData data = getSkillData(skillId);

        if (!(skill instanceof ToggleableSkill toggleable) || data == null) {
            return false;
        }
        if (toggleable.isEnabled(data) == enabled) {
            return true;
        }
        if (enabled && !toggleable.canEnable(initiator, this, data)) {
            return false;
        }

        startProcess(ProcessType.MODIFY_SKILL);

        if (enabled) {
            toggleable.setEnabled(data, true);
            toggleable.onEnabled(this, data);
            for (LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)) {
                toggleable.applyEnabledToEntity(entity, data);
            }
        } else {
            for (LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)) {
                toggleable.removeEnabledFromEntity(entity, data);
            }
            toggleable.onDisabled(this, data);
            toggleable.setEnabled(data, false);
        }

        markSkillDirty(skillId);
        return true;
    }

    @Override
    public RegistryAccess getRegistryAccess() {
        return ServerLifecycleHooks.getCurrentServer() != null ? ServerLifecycleHooks.getCurrentServer().registryAccess() : null;
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
    public void addAffinity(Identifier category, Identifier path, double val) {
        super.addAffinity(category, path, val);
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) {
            dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        }else {
            dirtyCategorizedAffinity.computeIfAbsent(category, key -> new HashSet<>());
            dirtyCategorizedAffinity.get(path).add(getAffinityHolder().getAffinityContainer(category, path));
        }
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
    public void removeAffinity(Identifier category, Identifier path, double val) {
        super.removeAffinity(category, path, val);
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) {
            dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        }else {
            dirtyCategorizedAffinity.computeIfAbsent(category, key -> new HashSet<>());
            dirtyCategorizedAffinity.get(path).add(getAffinityHolder().getAffinityContainer(category, path));
        }
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
    public void addAffinityModifier(Identifier category, Identifier path, ValueContainerModifier modifier) {
        super.addAffinityModifier(category, path, modifier);
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) {
            dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        }else {
            dirtyCategorizedAffinity.computeIfAbsent(category, key -> new HashSet<>());
            dirtyCategorizedAffinity.get(path).add(getAffinityHolder().getAffinityContainer(category, path));
        }
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
    public void removeAffinityModifier(Identifier category, Identifier path, Identifier modifier) {
        super.removeAffinityModifier(category, path, modifier);

        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) {
            dirtyAffinity.add(getAffinityHolder().getAffinityContainer(path));
        }else {
            dirtyCategorizedAffinity.computeIfAbsent(category, key -> new HashSet<>());
            dirtyCategorizedAffinity.get(path).add(getAffinityHolder().getAffinityContainer(category, path));
        }
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
        if(currentProcess != null) return;

        currentProcess = processType;
    }

    //only resolve if the process matches the current one
    public void resolveProcess(ProcessType processType){
        if(processType != this.currentProcess) return;
        currentProcess = null;
        sync();

    }
    protected void sync(){
        Collection<LivingEntity> entities = AscensionCraft.getSourceHandler().getLoadedWatchers(this);
        SourceChangesSnapshot snapshot = new SourceChangesSnapshot(
                physiqueDirty ? getPhysique() : null,
                physiqueDirty ? getPhysiqueData() : null,
                new HashMap<>(toAddBloodlines),
                Set.copyOf(toRemoveBloodlines),
                new HashMap<>(toAddPaths),
                Set.copyOf(toRemovePaths),
                new HashMap<>(toAddSkills),
                Set.copyOf(toRemoveSkills),
                new HashMap<>(toAddDataSources),
                Set.copyOf(toRemoveDataSources),
                Set.copyOf(dirtyStats),
                Set.copyOf(dirtyAffinity),
                new HashMap<>(dirtyCategorizedAffinity)
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
        dirtyCategorizedAffinity.clear();
    }
}
