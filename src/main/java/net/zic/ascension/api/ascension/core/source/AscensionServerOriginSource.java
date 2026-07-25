package net.zic.ascension.api.ascension.core.source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.data_source.DataSourceInstance;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.event.EventReason;
import net.zic.ascension.api.ascension.event.bloodline.BloodlineAddedEvent;
import net.zic.ascension.api.ascension.event.bloodline.BloodlineRemovedEvent;
import net.zic.ascension.api.ascension.event.path.PathAddedEvent;
import net.zic.ascension.api.ascension.event.path.PathRemovedEvent;
import net.zic.ascension.api.ascension.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.api.ascension.event.skill.SkillAddedEvent;
import net.zic.ascension.api.ascension.event.skill.SkillRemovedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
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
public class AscensionServerOriginSource extends AscensionOriginSource {
    //──Sync Data────────────────────────────────────────────────────────


    private final HashMap<Identifier, SkillData> toAddSkills= new HashMap<>();
    private final HashSet<Identifier> toRemoveSkills = new HashSet<>();

    private final HashSet<StatInstance> dirtyStats = new HashSet<>();
    private final HashSet<ValueContainer> dirtyAffinity = new HashSet<>();
    private final HashMap<Identifier,HashSet<ValueContainer>> dirtyCategorizedAffinity = new HashMap<>();



    public AscensionServerOriginSource(){}
    public AscensionServerOriginSource(ValueInput input){
        super(input);
    }
    public AscensionServerOriginSource(CompoundTag input){
        super(input);
    }





    @Override
    public boolean setPhysique(Identifier physique, PhysiqueData physiqueData) {
        if(physique == null) return false;
        Identifier oldPhysique = getPhysique();
        PhysiqueData oldPhysiqueData = getPhysiqueData();

        PhysiqueChangedEvent.Pre pre = new PhysiqueChangedEvent.Pre(oldPhysique,oldPhysiqueData,physique,physiqueData,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;
        if(pre.getNewPhysiqueIdentifier() == null) return false;



        boolean result = super.setPhysique(pre.getNewPhysiqueIdentifier(), pre.getPhysiqueData());
        if(!result) return false;

        markDataSourceDirty(CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.getId());

        startProcess("set_physique");

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
        PhysiqueChangedEvent.Post post = new PhysiqueChangedEvent.Post(oldPhysique,oldPhysiqueData,pre.getNewPhysiqueIdentifier(),pre.getNewPhysiqueData(),this);
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
        resolveProcess("set_physique");
        return true;
    }

    //TODO consider creating a replace bloodline event as well
    @Override
    public boolean addBloodline(Identifier bloodline, BloodlineData data ) {
        if(bloodline == null) return false;
        if(hasBloodline(bloodline)) {
            mergeBloodline(bloodline,data);
            return true;
        };

        BloodlineAddedEvent.Pre pre = new BloodlineAddedEvent.Pre(bloodline,data,this);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addBloodline(bloodline, data);
        if(!result) return false;

        startProcess("add_bloodline");
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



        BloodlineAddedEvent.Post post= new BloodlineAddedEvent.Post(bloodline,data,this);
        NeoForge.EVENT_BUS.post(post);
        markDataSourceDirty(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());

        resolveProcess("add_bloodline");
        return true;

    }
    //TODO consider adding a replace bloodline event
    @Override
    public boolean removeBloodline(Identifier bloodline) {
        if(bloodline == null) return false;
        if(!hasBloodline(bloodline)) return false;

        BloodlineData data = getBloodlineData(bloodline);
        BloodlineRemovedEvent.Pre pre = new BloodlineRemovedEvent.Pre(bloodline,data,this);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;



        boolean result =  super.removeBloodline(bloodline);
        if(!result) return false;

        startProcess("remove_bloodline");
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

        BloodlineRemovedEvent.Post post= new BloodlineRemovedEvent.Post(bloodline,data,this);
        NeoForge.EVENT_BUS.post(post);

        markDataSourceDirty(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());

        resolveProcess("remove_bloodline");
        return true;
    }


    @Override
    public boolean addPath(Identifier path, PathData existingData,Identifier owner) {

        if(path == null || existingData == null) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(getRegistryAccess()).containsKey(path)) return false;
        if(getPathHolder().hasCachedPath(path)) existingData = getPathHolder().removeCachedPath(path);
        PathAddedEvent.Pre pre = new PathAddedEvent.Pre(path,existingData,this);

        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addPath(path, existingData,owner);
        if(!result) return false;

        startProcess("add_path");

        existingData.simulateProgression(this);
        PathAddedEvent.Post post = new PathAddedEvent.Post(path,existingData,this);
        NeoForge.EVENT_BUS.post(post);

        markDataSourceDirty(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());

        resolveProcess("add_path");
        return true;
    }


    @Override
    public boolean removePath(Identifier path,Identifier owner) {
        if(path == null || !hasPath(path)) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(getRegistryAccess()).containsKey(path)) return false;
        PathData data = getPathData(path);
        PathRemovedEvent.Pre pre = new PathRemovedEvent.Pre(path,data,this);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.removePath(path,owner);
        if(!result) return false;

        startProcess("remove_path");
        data.removeFromSource(this);
        PathRemovedEvent.Post post = new PathRemovedEvent.Post(path,data,this);
        NeoForge.EVENT_BUS.post(post);

        markDataSourceDirty(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());


        resolveProcess("remove_path");
        return true;
    }


    @Override
    public boolean addSkill(Identifier skill, SkillData data,Identifier owner) {
        if(skill == null) return false;
        if(!CoreRegistries.SKILL_REGISTRY.get(getRegistryAccess()).containsKey(skill)) return false;
        if(getSkillHolder().hasCachedSkill(skill))  data = getSkillHolder().removeCachedSkill(skill);
        SkillAddedEvent.Pre pre = new SkillAddedEvent.Pre(this,skill,data,null);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addSkill(skill, data,owner);
        if(!result) return false;

        startProcess("add_skill");
        if(pre.getSkill(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getSkill(getRegistryAccess()).applyToEntity(entity,pre.getSkillData());
            }
        }
        pre.getSkill(getRegistryAccess()).onAdded(this,data);

        SkillAddedEvent.Post post = new SkillAddedEvent.Post(this,skill,data,null);

        NeoForge.EVENT_BUS.post(post);

        markDataSourceDirty(CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
        resolveProcess("add_skill");
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

        startProcess("remove_skill");
        pre.getSkill(getRegistryAccess()).onRemoved(this,data);
        if(pre.getSkill(getRegistryAccess()) != null){
            for(LivingEntity entity : AscensionCraft.getSourceHandler().getLoadedWatchers(this)){
                pre.getSkill(getRegistryAccess()).removeFromEntity(entity,pre.getSkillData());
            }
        }
        SkillRemovedEvent.Post post = new SkillRemovedEvent.Post(this,skill,data,null);
        NeoForge.EVENT_BUS.post(post);

        markDataSourceDirty(CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
        resolveProcess("remove_skill");
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
    public void markSkillDirty(Identifier skill) {
        toAddSkills.put(skill,getSkillData(skill));
        startProcess(ProcessType.MODIFY_SKILL);
        resolveProcess(ProcessType.MODIFY_SKILL);
    }



    @Override
    public boolean resolveProcess(String processId) {
        boolean result = super.resolveProcess(processId);
        if(!result) return false;

        OriginSourcePatch patch = resolvePatch();

        for(LivingEntity entity : getAttachedEntities()){
            AscensionEntityDataProvider holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            holder.getData(entity).markDirty(patch);
        }
        return true;
    }


}
