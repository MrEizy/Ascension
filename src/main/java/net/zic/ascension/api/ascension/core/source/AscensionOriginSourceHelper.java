package net.zic.ascension.api.ascension.core.source;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineHolder;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineInstance;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreInstance;
import net.zic.ascension.api.ascension.core.golden_core.data_source.GoldenCoreHolder;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.event.golden_core.GoldenCoreEvent;

import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.bonus.EntityPathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.data_source.DataSourcePathBonusHolder;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.physique.PhysiqueHolder;
import net.zic.ascension.api.ascension.core.physique.PhysiqueInstance;
import org.jspecify.annotations.Nullable;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.data_source.SkillHolder;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.TechniqueHolder;
import net.zic.ascension.api.ascension.core.technique.TechniqueInstance;
import net.zic.ascension.api.ascension.event.bloodline.BloodlineEvent;
import net.zic.ascension.api.ascension.event.path.PathAddedEvent;
import net.zic.ascension.api.ascension.event.path.PathRemovedEvent;
import net.zic.ascension.api.ascension.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.api.ascension.event.skill.SkillEvent;
import net.zic.ascension.api.ascension.event.technique.TechniqueEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Contains methods to interact with an origin source.
 * These methods are specific to Ascension DataSources
 * includes a combination of getters and setters
  */
public class AscensionOriginSourceHelper {

    private static final Random random=  new Random();

    /**
     * Resolves the Ascension source used by any living entity. Players normally expose
     * the source through their capability, while cultivated mobs use the shared entity
     * data attachment directly.
     */
    public static OriginSource getEntitySource(LivingEntity entity) {
        AscensionEntityDataProvider provider = entity.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
        );
        if (provider != null) {
            return provider.getData().getSource();
        }
        return entity.getData(AscensionAttachments.SIMPLE_ENTITY_DATA).getSource();
    }

    //──Holder Access────────────────────────────────────────────────────────

    /**
     * returns a DataSource instance, if one is not present creates a new one
     * @param source the source we are trying to get the instance from
     * @param dataSource the data source we want the instance of
     * @return the instance, new or old
     * @param <T> the data source instance type, which is recursively bound
     */
    protected static <T extends DataSourceInstance<? extends DataSource<T>>> T getOrCreate(OriginSource source,DataSource<T> dataSource){
        source.addDataSource(dataSource);//if a holder does not exist creates one
        return source.getDataSource(dataSource);
    }
    protected static PhysiqueHolder getPhysiqueHolder(OriginSource source){
        return getOrCreate(source,CoreDataSources.PHYSIQUE_DATA_SOURCE.get());
    }


    protected static BloodlineHolder getBloodlineHolder(OriginSource source){
        return getOrCreate(source,CoreDataSources.BLOODLINE_DATA_SOURCE.get());
    }
    protected static PathHolder getPathHolder(OriginSource source){
        return getOrCreate(source, CoreDataSources.PATH_DATA_SOURCE.get());
    }
    protected static SkillHolder getSkillHolder(OriginSource source){
        return getOrCreate(source, CoreDataSources.SKILL_DATA_SOURCE.get());
    }
    protected static TechniqueHolder getTechniqueHolder(OriginSource source){
        return getOrCreate(source, CoreDataSources.TECHNIQUE_DATA_SOURCE.get());
    }
    protected static DataSourcePathBonusHolder getPathBonusHolder(OriginSource source){
        return getOrCreate(source, CoreDataSources.PATH_BONUS_DATA_SOURCE.get());
    }
    protected static GoldenCoreHolder getGoldenCoreHolder(OriginSource source){
        return getOrCreate(source, CoreDataSources.GOLDEN_CORE_DATA_SOURCE.get());
    }

    //TODO add the methods that dont take in data and create a fresh instance instead

    //──Physique Access────────────────────────────────────────────────────────

    public static @Nullable Identifier getPhysiqueId(OriginSource source){
        return getPhysiqueHolder(source).getPhysiqueId();
    }
    public static @Nullable PhysiqueInstance<?> getPhysique(OriginSource source){
        return getPhysiqueHolder(source).getPhysique();
    }
    public static @Nullable PhysiqueData getPhysiqueData(OriginSource source){
        PhysiqueInstance<?> instance = getPhysique(source);
        return instance == null ? null : instance.getData();
    }

    /**
     * sets the physique to a fresh instance of physique, checking its requirements
     * @param physique the new physique, null removes the current physique
     * @return true -> changed, false -> not changed
     */
    public static boolean setPhysique(OriginSource source,@Nullable Identifier physique){
        if (physique == null) return setPhysique(source, (PhysiqueInstance<?>) null, true);
        PhysiqueInstance<?> instance = PhysiqueInstance.create(physique,source.getRegistryAccess());
        if(instance == null) return false;
        return setPhysique(source, instance, true);
    }

    /**
     * replaces the current physique with instance
     * @param instance the new physique, null removes the current physique
     * @param checkRequirements if the new physique's requirements must pass
     * @return true -> changed, false -> not changed
     */
    public static boolean setPhysique(OriginSource source, @Nullable PhysiqueInstance<?> instance, boolean checkRequirements) {

        PhysiqueHolder holder = getPhysiqueHolder(source);

        PhysiqueInstance<?> oldPhysique = holder.getPhysique();

        PhysiqueChangedEvent.Pre pre = new PhysiqueChangedEvent.Pre(oldPhysique,instance,source);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        PhysiqueInstance<?> newPhysique = pre.getNewPhysiqueInstance();

        if (checkRequirements && newPhysique != null && !newPhysique.getPhysique().requirements().test(source)) {
            return false;
        }

        boolean result = holder.setPhysique(newPhysique);
        if(!result) return false;

        source.markDataSourceDirty(CoreDataSources.PHYSIQUE_DATA_SOURCE.getId());

        source.startProcess("set_physique");

        Collection<Identifier> toRemove = oldPhysique == null
                ? List.of() : oldPhysique.onRemoved(source);

        if(oldPhysique != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                oldPhysique.removeFromEntity(entity);
            }
        }
        Collection<Identifier> toAdd = newPhysique == null
                ? List.of() : newPhysique.onAdded(source);
        if(newPhysique != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                newPhysique.applyToEntity(entity);
            }
        }
        PhysiqueChangedEvent.Post post = new PhysiqueChangedEvent.Post(oldPhysique,newPhysique,source);
        NeoForge.EVENT_BUS.post(post);
        //first add all new paths with the new physique as owner, this ensures that if there is path overlap there is owners >1

        for(Identifier path : toAdd){
            addPath(source,path,newPhysique.getPhysiqueId());
        }
        for(Identifier path : toRemove){
            removePath(source,path,oldPhysique.getPhysiqueId());
        }

        resolveProcess(source,"set_physique");
        return true;
    }

    //should be used if you modified a physique data
    public static void markPhysiqueDirty(OriginSource source,Identifier bloodline){
        long id = random.nextLong();
        source.startProcess("modified_physique"+id);
        source.markDataSourceDirty(CoreDataSources.PHYSIQUE_DATA_SOURCE.getId());
        resolveProcess(source,"modified_physique"+id);
    }
    //──Bloodline Access────────────────────────────────────────────────────────


    public static Collection<Identifier> getBloodlines(OriginSource source){
       return getBloodlineHolder(source).getBloodlines();
    }
    public static Collection<BloodlineInstance<?>> getBloodlineInstances(OriginSource source){
        return getBloodlineHolder(source).getBloodlineInstances();
    }
    public static @Nullable BloodlineInstance<?> getBloodline(OriginSource source,Identifier bloodline){
        return getBloodlineHolder(source).getBloodline(bloodline);
    }
    public static @Nullable BloodlineData getBloodlineData(OriginSource source,Identifier bloodline){
        BloodlineInstance<?> instance = getBloodline(source,bloodline);
        return instance == null ? null : instance.getData();
    }
    public static boolean hasBloodline(OriginSource source,Identifier bloodline){
        return getBloodlineHolder(source).hasBloodline(bloodline);
    }

    /**
     * adds a bloodline with fresh data, checking its requirements
     * @param bloodline the bloodline we are adding
     * @return true -> added or merged, false -> not added
     */
    public static boolean addBloodline(OriginSource source,Identifier bloodline){
        if(bloodline == null)return false;
        BloodlineInstance<?> instance = BloodlineInstance.create(bloodline,source.getRegistryAccess());
        return instance != null && addBloodline(source,instance,true);
    }

    //increases the purity of a held bloodline by purityGain
    public static void mergeBloodline(OriginSource source,Identifier bloodline,int purityGain){
        BloodlineInstance<?> instance = getBloodline(source,bloodline);
        if(instance == null) return;

        source.startProcess("merge_bloodline");
        instance.handlePurityChange(source,instance.getData().getPurity()+purityGain);

        getBloodlineHolder(source).markBloodlineDirty(bloodline);
        source.markDataSourceDirty(CoreDataSources.BLOODLINE_DATA_SOURCE.getId());
        resolveProcess(source,"merge_bloodline");
    }

    /**
     * adds a bloodline instance, if the bloodline is already held the instance's purity is merged into it instead
     * @param instance the bloodline we are adding
     * @param checkRequirements if the bloodline's requirements must pass
     * @return true -> added or merged, false -> not added
     */
    //TODO consider creating a replace bloodline event as well
    public static boolean addBloodline(OriginSource source,BloodlineInstance<?> instance, boolean checkRequirements) {
        if(instance == null) return false;
        Identifier bloodline = instance.getBloodlineId();
        if(getBloodlineHolder(source).hasBloodline(bloodline)) {
            mergeBloodline(source,bloodline,instance.getData().getPurity());
            return true;
        }

        BloodlineEvent.Added.Pre pre = new BloodlineEvent.Added.Pre(source,instance);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;
        if (checkRequirements && !instance.getBloodline().requirements().test(source)) return false;

        boolean result = getBloodlineHolder(source).addBloodline(instance);
        if(!result) return false;

        source.startProcess("add_bloodline");
        BloodlineData data = instance.getData();
        int purity = data.getPurity();
        data.setPurity(1);
        Collection<Identifier> toAdd = instance.onAdded(source);

        for(LivingEntity entity : source.getAttachedEntities()){
            instance.applyToEntity(entity);
        }
        instance.handlePurityChange(source,purity);

        for(Identifier path : toAdd){
            addPath(source,path,bloodline);
        }

        NeoForge.EVENT_BUS.post(new BloodlineEvent.Added.Post(source,instance));
        source.markDataSourceDirty(CoreDataSources.BLOODLINE_DATA_SOURCE.getId());

        resolveProcess(source,"add_bloodline");
        return true;

    }
    public static boolean removeBloodline(OriginSource source,Identifier bloodline) {
        if(bloodline == null) return false;
        BloodlineInstance<?> instance = getBloodline(source,bloodline);
        if(instance == null) return false;

        BloodlineEvent.Removed.Pre pre = new BloodlineEvent.Removed.Pre(source,instance);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result =  getBloodlineHolder(source).removeBloodline(bloodline);
        if(!result) return false;

        source.startProcess("remove_bloodline");
        instance.handlePurityChange(source,1);

        Collection<Identifier> toRemove = instance.onRemoved(source);
        for(LivingEntity entity : source.getAttachedEntities()){
            instance.removeFromEntity(entity);
        }
        for(Identifier path : toRemove){
            removePath(source,path,bloodline);
        }

        NeoForge.EVENT_BUS.post(new BloodlineEvent.Removed.Post(source,instance));

        source.markDataSourceDirty(CoreDataSources.BLOODLINE_DATA_SOURCE.getId());

        resolveProcess(source,"remove_bloodline");
        return true;
    }

    //should be used if you modified a bloodlines data
    public static void markBloodlineDirty(OriginSource source,Identifier bloodline){
        long id = random.nextLong();
        source.startProcess("modified_bloodline"+id);
        source.markDataSourceDirty(CoreDataSources.BLOODLINE_DATA_SOURCE.getId());
        getBloodlineHolder(source).markBloodlineDirty(bloodline);
        resolveProcess(source,"modified_bloodline"+id);
    }
    //──Path Access────────────────────────────────────────────────────────

    //TODO add accessor methods
    public static boolean hasPath(OriginSource source,Identifier path){
        return getPathHolder(source).hasPath(path);
    }
    public static @Nullable PathInstance<?> getPathInstance(OriginSource source, Identifier path){
        return getPathHolder(source).getPath(path);
    }

    public static Collection<Identifier> getPaths(OriginSource source){
        return getPathHolder(source).getPaths();
    }
    public static Collection<PathInstance<?>> getPathInstances(OriginSource source){
        return getPathHolder(source).getPathInstances();
    }
    public static Collection<Identifier> getPathOwners(OriginSource source,Identifier path){
        return getPathHolder(source).getOwners(path);
    }

    /**
     * Adds a path, creating a fresh instance if it is not already held or cached
     * @param path the path to add
     * @param owner the source of this addition
     * @return true-> added, false -> not added
     */
    public static boolean addPath(OriginSource source,Identifier path,Identifier owner){
        if(path == null) return false;
        PathHolder holder = getPathHolder(source);
        if(holder.hasPath(path)){
            holder.addPath(holder.getPath(path),owner);
            return true;
        }
        PathInstance<?> instance = PathInstance.create(path,source.getRegistryAccess());
        if(instance == null) return false;
        return addPath(source,instance,owner);
    }

    /**
     * Adds a path instance, if the path is already held only the owner is added.
     * if the path is cached the cached instance is used instead
     * @param instance the path to add
     * @param owner the source of this addition
     * @return true-> added, false -> not added
     */
    public static boolean addPath(OriginSource source, PathInstance<?> instance, Identifier owner) {
        if(instance == null) return false;
        boolean usedCachedResult = false;
        Identifier path = instance.getPathId();
        PathHolder holder = getPathHolder(source);
        if(holder.hasPath(path)){
            holder.addPath(instance,owner);
            return true;
        }

        if(!CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).containsKey(path)) return false;
        if(holder.hasCachedPath(path)) {
            instance = holder.removeCachedPath(path);
            usedCachedResult = true;
        }


        if(instance == null) return false;

        PathAddedEvent.Pre pre = new PathAddedEvent.Pre(path,instance,source);

        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = holder.addPath(instance,owner);
        if(!result) return false;

        source.startProcess("add_path");

        if(!usedCachedResult) instance.simulateProgression(source);
        PathAddedEvent.Post post = new PathAddedEvent.Post(path,instance,source);
        NeoForge.EVENT_BUS.post(post);

        source.markDataSourceDirty(CoreDataSources.PATH_DATA_SOURCE.getId());

        resolveProcess(source,"add_path");
        return true;
    }


    public static boolean removePath(OriginSource source,Identifier path,Identifier owner) {
        if(path == null || !getPathHolder(source).hasPath(path)) return false;

        if(!CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).containsKey(path)) return false;
        PathInstance<?> data = getPathHolder(source).getPath(path);
        PathRemovedEvent.Pre pre = new PathRemovedEvent.Pre(path,data,source);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = getPathHolder(source).removePath(path,owner);
        if(!result) return false;

        source.startProcess("remove_path");
        data.removeFromSource(source);
        PathRemovedEvent.Post post = new PathRemovedEvent.Post(path,data,source);
        NeoForge.EVENT_BUS.post(post);

        source.markDataSourceDirty(CoreDataSources.PATH_DATA_SOURCE.getId());


        resolveProcess(source,"remove_path");
        return true;
    }

    //should be used if you changed a paths PathData
    public static void markPathDirty(OriginSource source,Identifier path){
        long id = random.nextLong();
        source.startProcess("modified_path"+id);
        source.markDataSourceDirty(CoreDataSources.PATH_DATA_SOURCE.getId());
        getPathHolder(source).markPathDirty(path);
        resolveProcess(source,"modified_path"+id);

    }

    /**
     * if an external source can trigger the current realm's MANUAL tribulation of a path right now
     * @param tribulation the tribulation the caller expects to trigger
     */
    public static boolean canTriggerPathTribulation(OriginSource source,Identifier path,Identifier tribulation){
        PathInstance<?> instance = getPathInstance(source,path);
        return instance != null && instance.canTriggerTribulation(tribulation);
    }

    /**
     * triggers the current realm's MANUAL tribulation of a path, and syncs the path
     * @param tribulation the tribulation the caller expects to trigger, if the realm's tribulation differs nothing is triggered
     * @param target the entity the tribulation targets
     * @param beforeStart runs before the tribulation starts, use it to modify the TribulationData (see {@link TribulationInstance#as})
     * @return the started tribulation, or null if it could not be triggered
     */
    public static @Nullable TribulationInstance<?> triggerPathTribulation(OriginSource source, Identifier path, Identifier tribulation, LivingEntity target,
                                                                          @Nullable Consumer<TribulationInstance<?>> beforeStart){
        PathInstance<?> instance = getPathInstance(source,path);
        if(instance == null) return null;
        TribulationInstance<?> started = instance.triggerTribulation(source,tribulation,target,beforeStart);
        if(started != null) markPathDirty(source,path);
        return started;
    }

    //the running breakthrough tribulation of a path, null if there is none
    public static @Nullable TribulationInstance<?> getActivePathTribulation(OriginSource source,Identifier path){
        PathInstance<?> instance = getPathInstance(source,path);
        return instance == null ? null : instance.getActiveTribulation();
    }

    //──Skill Access────────────────────────────────────────────────────────

    public static Collection<Identifier> getSkills(OriginSource source){return getSkillHolder(source).getSkills();}

    public static Collection<SkillInstance<?>> getSkillInstances(OriginSource source){return getSkillHolder(source).getSkillInstances();}

    public static boolean hasSkill(OriginSource source,Identifier skill){
        return getSkillHolder(source).hasSkill(skill);
    }
    public static @Nullable SkillInstance<?> getSkill(OriginSource source,Identifier skill){
        return getSkillHolder(source).getSkill(skill);
    }
    public static @Nullable SkillData getSkillData(OriginSource source,Identifier skill){
        SkillInstance<?> instance = getSkill(source,skill);
        return instance == null ? null : instance.getData();
    }

    /**
     * adds a skill, creating a new skill instance if it is not already held or cached
     * @param skill the skill we are adding
     * @param owner the source of this skill
     * @return true -> added, false -> not added
     */
    public static boolean addSkill(OriginSource source,Identifier skill,Identifier owner){
        if(skill == null) return false;
        SkillHolder holder = getSkillHolder(source);
        if(holder.hasSkill(skill)) return holder.addOwner(skill,owner);

        SkillInstance<?> instance = holder.takeCachedSkill(skill);
        if(instance == null) instance = SkillInstance.create(skill,source.getRegistryAccess());
        if(instance == null) return false;
        return addSkill(source,instance,owner);
    }

    /**
     * adds a skill instance, if the skill is already held only the owner is added
     * @param instance the skill we are adding
     * @param owner the source of this skill
     * @return true -> added, false -> not added
     */
    public static boolean addSkill(OriginSource source, SkillInstance<?> instance, Identifier owner) {
        if(instance == null) return false;
        SkillHolder holder = getSkillHolder(source);
        if(holder.hasSkill(instance.getSkillId())) return holder.addOwner(instance.getSkillId(),owner);

        SkillInstance<?> cached = holder.takeCachedSkill(instance.getSkillId());
        if(cached != null) instance = cached;

        SkillEvent.Added.Pre pre = new SkillEvent.Added.Pre(source,instance);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        if(!holder.addSkill(instance,owner)) return false;

        source.startProcess("add_skill");
        for(LivingEntity entity : source.getAttachedEntities()){
            instance.applyToEntity(entity);
        }
        instance.onAdded(source);

        NeoForge.EVENT_BUS.post(new SkillEvent.Added.Post(source,instance));

        source.markDataSourceDirty(CoreDataSources.SKILL_DATA_SOURCE.getId());
        resolveProcess(source,"add_skill");
        return true;
    }

    public static boolean removeSkill(OriginSource source,Identifier skill,Identifier owner) {
        if(skill == null) return false;
        SkillHolder holder = getSkillHolder(source);
        SkillInstance<?> instance = holder.getSkill(skill);
        if(instance == null) return false;

        SkillEvent.Removed.Pre pre = new SkillEvent.Removed.Pre(source,instance);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        if(!holder.removeSkill(skill,owner)) return false;

        source.startProcess("remove_skill");
        instance.onRemoved(source);
        for(LivingEntity entity : source.getAttachedEntities()){
            instance.removeFromEntity(entity);
        }
        NeoForge.EVENT_BUS.post(new SkillEvent.Removed.Post(source,instance));

        source.markDataSourceDirty(CoreDataSources.SKILL_DATA_SOURCE.getId());
        resolveProcess(source,"remove_skill");
        return true;
    }

    public static void markSkillDirty(OriginSource source,Identifier skill){
        long id = random.nextLong();
        source.startProcess("modified_skill"+id);
        source.markDataSourceDirty(CoreDataSources.SKILL_DATA_SOURCE.getId());
        getSkillHolder(source).markSkillDirty(skill);
        resolveProcess(source,"modified_skill"+id);
    }//should be used if you changed a skills skilLData

    //──Technique Access────────────────────────────────────────────────────────

    public static Collection<Identifier> getTechniques(OriginSource source) {
        return getTechniqueHolder(source).getTechniques();
    }

    public static Collection<TechniqueInstance<?>> getTechniqueInstances(OriginSource source) {
        return getTechniqueHolder(source).getTechniqueInstances();
    }

    public static boolean hasTechnique(OriginSource source, Identifier technique) {
        return getTechniqueHolder(source).hasTechnique(technique);
    }

    public static @Nullable TechniqueInstance<?> getTechnique(OriginSource source, Identifier technique) {
        return getTechniqueHolder(source).getTechnique(technique);
    }

    public static @Nullable TechniqueData getTechniqueData(OriginSource source, Identifier technique) {
        TechniqueInstance<?> instance = getTechnique(source, technique);
        return instance == null ? null : instance.getData();
    }

    /**
     * adds a technique with fresh data, checking its requirements
     * @param technique the technique we are adding
     * @return true -> added, false -> not added
     */
    public static boolean addTechnique(OriginSource source, Identifier technique) {
        if (technique == null) {
            return false;
        }
        TechniqueInstance<?> instance = TechniqueInstance.create(technique, source.getRegistryAccess());
        return instance != null && addTechnique(source, instance, true);
    }

    /**
     * adds a technique instance
     * @param instance the technique we are adding
     * @param checkRequirements if the technique's requirements must pass
     * @return true -> added, false -> not added
     */
    public static boolean addTechnique(OriginSource source, TechniqueInstance<?> instance, boolean checkRequirements) {
        if (instance == null || hasTechnique(source, instance.getTechniqueId())) {
            return false;
        }
        if (!broadcastTechniqueAddedAttempt(source, instance)) {
            return false;
        }
        if (checkRequirements && !instance.getTechnique().requirements().test(source)) {
            return false;
        }
        if (!getTechniqueHolder(source).addTechnique(instance)) {
            return false;
        }

        source.startProcess("add_technique");
        instance.onAdded(source);
        broadcastTechniqueAdded(source, instance);
        source.markDataSourceDirty(CoreDataSources.TECHNIQUE_DATA_SOURCE.getId());
        resolveProcess(source, "add_technique");
        return true;
    }

    public static boolean removeTechnique(OriginSource source, Identifier technique) {
        TechniqueInstance<?> instance = technique == null ? null : getTechnique(source, technique);
        if (instance == null || !broadcastTechniqueRemovedAttempt(source, instance)) {
            return false;
        }
        if (!getTechniqueHolder(source).removeTechnique(technique)) {
            return false;
        }

        source.startProcess("remove_technique");
        instance.onRemoved(source);
        broadcastTechniqueRemoved(source, instance);
        source.markDataSourceDirty(CoreDataSources.TECHNIQUE_DATA_SOURCE.getId());
        resolveProcess(source, "remove_technique");
        return true;
    }

    //should be used if you changed a techniques TechniqueData
    public static void markTechniqueDirty(OriginSource source, Identifier technique) {
        long id = random.nextLong();
        source.startProcess("modified_technique" + id);
        getTechniqueHolder(source).markTechniqueDirty(technique);
        source.markDataSourceDirty(CoreDataSources.TECHNIQUE_DATA_SOURCE.getId());
        resolveProcess(source, "modified_technique" + id);
    }


    public static boolean broadcastTechniqueAddedAttempt(OriginSource source, TechniqueInstance<?> instance) {
        TechniqueEvent.Added.Pre pre = new TechniqueEvent.Added.Pre(source, instance);
        NeoForge.EVENT_BUS.post(pre);
        return !pre.isCanceled();
    }


    public static void broadcastTechniqueAdded(OriginSource source, TechniqueInstance<?> instance) {
        for (LivingEntity entity : source.getAttachedEntities()) {
            instance.applyToEntity(entity);
        }
        NeoForge.EVENT_BUS.post(new TechniqueEvent.Added.Post(source, instance));
    }

    public static boolean broadcastTechniqueRemovedAttempt(OriginSource source, TechniqueInstance<?> instance) {
        TechniqueEvent.Removed.Pre pre = new TechniqueEvent.Removed.Pre(source, instance);
        NeoForge.EVENT_BUS.post(pre);
        return !pre.isCanceled();
    }

    public static void broadcastTechniqueRemoved(OriginSource source, TechniqueInstance<?> instance) {
        for (LivingEntity entity : source.getAttachedEntities()) {
            instance.removeFromEntity(entity);
        }
        NeoForge.EVENT_BUS.post(new TechniqueEvent.Removed.Post(source, instance));
    }
    //──Path Bonus Access────────────────────────────────────────────────────────

    /*
        If you start a process by yourself, ensure you keep track of the PathBonuses(category+path) that you modified
        so you can properly update them on the entity later
     */


    public static void addBonusFlatModifier(OriginSource source, Identifier category, Identifier path, Modifier<Double> modifier){
        getPathBonusHolder(source).addFlatModifier(category,path,modifier);
        markPathBonusHolderDirty(source);
        if(getPathBonusHolder(source).startAndResolveProcess()) updateEntityPathBonus(source,category,path);
    }
    public static void addBonusMultiplierModifier(OriginSource source,Identifier category,Identifier path,Modifier<Double> modifier){
        getPathBonusHolder(source).addMultiplierModifier(category,path,modifier);
        markPathBonusHolderDirty(source);
        if(getPathBonusHolder(source).startAndResolveProcess()) updateEntityPathBonus(source,category,path);
    }
    public static void removeBonusModifier(OriginSource source,Identifier category,Identifier path,Identifier modifier){
        getPathBonusHolder(source).removeModifier(category,path,modifier);
        markPathBonusHolderDirty(source);
        if(getPathBonusHolder(source).startAndResolveProcess()) updateEntityPathBonus(source,category,path);

    }


    public static ValueContainer<Double> getPathBonusContainer(OriginSource source, Identifier category, Identifier path) {
        return getPathBonusHolder(source).getPathBonusContainer(category,path);
    }


    public static double getPathBonus(OriginSource source,Identifier category, Identifier path) {
        return getPathBonusHolder(source).getPathBonus(category,path);
    }


    public static Collection<PathBonus> getAllPathBonuses(OriginSource source) {
        return getPathBonusHolder(source).getAllPathBonuses();
    }



    public static void markPathBonusHolderDirty(OriginSource source){
        String id = "modified_path_bonus"+ UUID.randomUUID();
        source.startProcess(id);
        source.markDataSourceDirty(CoreDataSources.PATH_BONUS_DATA_SOURCE.getId());
        resolveProcess(source,id);
    }
    //TODO update to utilize the new data attachment
    public static void updateEntityPathBonus(OriginSource source,Identifier category,Identifier path){
        for(LivingEntity entity : source.getAttachedEntities()){
            EntityPathBonusHolder holder = entity.getData(CoreAttachments.PATH_BONUS_HOLDER);
            holder.updatePathBonus(PathBonus.of(category,path));
        }
    }
    //──Affinity Quick Access────────────────────────────────────────────────────────
    public static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");

    public static void addAffinityFlatModifier(OriginSource source,Identifier path,Modifier<Double> modifier){
        addBonusFlatModifier(source,AFFINITY_CATEGORY,path,modifier);
    }
    public static void addAffinityMultiplierModifier(OriginSource source,Identifier path,Modifier<Double> moifier){
        addBonusMultiplierModifier(source,AFFINITY_CATEGORY,path,moifier);
    }
    public static void removeAffinityModifier(OriginSource source,Identifier path,Identifier modifier){
        removeBonusModifier(source,AFFINITY_CATEGORY,path,modifier);
    }

    public static double getAffinity(OriginSource source,Identifier path){
        return getPathBonus(source,AFFINITY_CATEGORY,path);
    }

    //──Golden Core Access────────────────────────────────────────────────────────

    public static Collection<UUID> getGoldenCores(OriginSource source){return getGoldenCoreHolder(source).getGoldenCores();}

    public static Collection<GoldenCoreInstance<?>> getGoldenCoreInstances(OriginSource source){return getGoldenCoreHolder(source).getGoldenCoreInstances();}

    public static boolean hasGoldenCore(OriginSource source,UUID id){
        return getGoldenCoreHolder(source).hasGoldenCore(id);
    }
    public static @Nullable GoldenCoreInstance<?> getGoldenCore(OriginSource source,UUID id){
        return getGoldenCoreHolder(source).getGoldenCore(id);
    }
    public static Collection<GoldenCoreInstance<?>> getGoldenCoresForPath(OriginSource source,Identifier path){
        return getGoldenCoreHolder(source).getGoldenCoresForPath(path);
    }
    public static Collection<Identifier> getGoldenCoreOwners(OriginSource source,UUID id){
        return getGoldenCoreHolder(source).getOwners(id);
    }

    /**
     * adds a golden core, creating a new instance if it is not already held or cached.
     * see {@link #addGoldenCore(OriginSource, GoldenCoreInstance, Identifier)} for how an existing id is handled
     * @param id the unique id of the instance
     * @param core the golden core
     * @param path the path the core tracks
     * @param acquisitionRealm the realm the core was acquired at, realm actions run for every realm above it
     * @param owner the source of this core
     * @return true -> added, false -> not added
     */
    public static boolean addGoldenCore(OriginSource source,UUID id,Identifier core,Identifier path,Realm acquisitionRealm,Identifier owner){
        if(id == null || core == null || path == null) return false;
        GoldenCoreInstance<?> instance = GoldenCoreInstance.create(id,core,path,acquisitionRealm,source.getRegistryAccess());
        if(instance == null) return false;
        return addGoldenCore(source,instance,owner);
    }

    /**
     * adds a golden core instance. if a core with the same id is already held (or cached) the owner is added to it, and
     * <ul>
     *     <li>if the core differs nothing else changes, the held core may have evolved</li>
     *     <li>if the path or acquisition realm differ the held core is updated to match them</li>
     * </ul>
     * @param instance the core we are adding
     * @param owner the source of this core
     * @return true -> added, false -> not added
     */
    public static boolean addGoldenCore(OriginSource source, GoldenCoreInstance<?> instance, Identifier owner) {
        if(instance == null) return false;
        GoldenCoreHolder holder = getGoldenCoreHolder(source);
        GoldenCoreInstance<?> existing = holder.getGoldenCore(instance.getId());
        if(existing != null){
            if(existing.getCoreId().equals(instance.getCoreId()) && !existing.hasSameDetails(instance)){
                updateGoldenCoreDetails(source,existing,instance.getPath(),instance.getAcquisitionRealm());
            }
            return holder.addOwner(instance.getId(),owner);
        }

        GoldenCoreInstance<?> cached = holder.takeCachedGoldenCore(instance.getId());
        if(cached != null){
            //nothing of the cached core is applied yet, so the details can be swapped directly
            instance = cached.getCoreId().equals(instance.getCoreId()) && !cached.hasSameDetails(instance)
                    ? cached.withDetails(instance.getPath(),instance.getAcquisitionRealm())
                    : cached;
        }

        GoldenCoreEvent.Added.Pre pre = new GoldenCoreEvent.Added.Pre(source,instance);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        if(!holder.addGoldenCore(instance,owner)) return false;

        source.startProcess("add_golden_core");
        for(LivingEntity entity : source.getAttachedEntities()){
            instance.applyToEntity(entity);
        }
        instance.onAdded(source);
        instance.syncRealms(source);

        NeoForge.EVENT_BUS.post(new GoldenCoreEvent.Added.Post(source,instance));

        source.markDataSourceDirty(CoreDataSources.GOLDEN_CORE_DATA_SOURCE.getId());
        resolveProcess(source,"add_golden_core");
        return true;
    }

    //removes the core and runs its logic again with the new details, keeps the id, data and owners
    private static void updateGoldenCoreDetails(OriginSource source, GoldenCoreInstance<?> existing, Identifier path, Realm acquisitionRealm){
        GoldenCoreHolder holder = getGoldenCoreHolder(source);
        long id = random.nextLong();
        source.startProcess("modified_golden_core"+id);

        existing.syncRealms(source,null,null);
        existing.onRemoved(source);

        GoldenCoreInstance<?> updated = existing.withDetails(path,acquisitionRealm);
        holder.replaceGoldenCore(updated);
        updated.onAdded(source);
        updated.syncRealms(source);

        source.markDataSourceDirty(CoreDataSources.GOLDEN_CORE_DATA_SOURCE.getId());
        resolveProcess(source,"modified_golden_core"+id);
    }

    public static boolean removeGoldenCore(OriginSource source,UUID id,Identifier owner) {
        if(id == null) return false;
        GoldenCoreHolder holder = getGoldenCoreHolder(source);
        GoldenCoreInstance<?> instance = holder.getGoldenCore(id);
        if(instance == null) return false;

        GoldenCoreEvent.Removed.Pre pre = new GoldenCoreEvent.Removed.Pre(source,instance);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        if(!holder.removeGoldenCore(id,owner)) return false;

        source.startProcess("remove_golden_core");
        instance.syncRealms(source,null,null);
        instance.onRemoved(source);
        for(LivingEntity entity : source.getAttachedEntities()){
            instance.removeFromEntity(entity);
        }
        NeoForge.EVENT_BUS.post(new GoldenCoreEvent.Removed.Post(source,instance));

        source.markDataSourceDirty(CoreDataSources.GOLDEN_CORE_DATA_SOURCE.getId());
        resolveProcess(source,"remove_golden_core");
        return true;
    }

    //should be used if you changed a cores GoldenCoreData
    public static void markGoldenCoreDirty(OriginSource source,UUID core){
        long id = random.nextLong();
        source.startProcess("modified_golden_core"+id);
        source.markDataSourceDirty(CoreDataSources.GOLDEN_CORE_DATA_SOURCE.getId());
        getGoldenCoreHolder(source).markGoldenCoreDirty(core);
        resolveProcess(source,"modified_golden_core"+id);
    }

    /**
     * syncs the realm actions of every core tracking a path. does nothing if the origin holds no golden cores
     * @param pathInstance the path instance, needed as the path may already be removed from the source
     * @param target the realm to sync to, null rolls back every applied realm
     */
    public static void syncGoldenCoreRealms(OriginSource source,Identifier path,@Nullable PathInstance<?> pathInstance,@Nullable Realm target){
        if(!source.hasDataSource(CoreDataSources.GOLDEN_CORE_DATA_SOURCE.get())) return;
        long id = random.nextLong();
        source.startProcess("sync_golden_core_realms"+id);
        for(GoldenCoreInstance<?> instance : getGoldenCoreHolder(source).getGoldenCoresForPath(path)){
            instance.syncRealms(source,pathInstance,target);
        }
        resolveProcess(source,"sync_golden_core_realms"+id);
    }

    //──Sync────────────────────────────────────────────────────────



    public static void resolveProcess(OriginSource source,String process){
        if(source.resolveProcess(process)) initializeSync(source);
    }
    public static void initializeSync(OriginSource source){
        OriginSourcePatch patch = source.resolvePatch();

        for(LivingEntity entity : source.getAttachedEntities()){
            AscensionEntityDataProvider holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            holder.getData().markDirty(patch,false);
        }

    }
}
