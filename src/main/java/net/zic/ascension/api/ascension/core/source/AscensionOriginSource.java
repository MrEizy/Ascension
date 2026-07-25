package net.zic.ascension.api.ascension.core.source;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineHolder;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.affinity.AffinityHolder;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.physique.PhysiqueHolder;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillHolder;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.event.EventReason;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.stats.StatSheet;
import net.zic.zenithlib.stats.event.StatsUpdatedEvent;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;


import java.util.*;

/**
 * the data of an entities abstract identity (multiple entities
 * might share a source)
 *<br>
 * holds no logic on HOW its content can be manipulated (other than basic null checks)
 *<br>
 * that is the job of the source wrapper
 *<br>
 * does not trigger events, that will only be done by ServerOriginSource
 *
 * TODO for each trigger onAdded
 */
public class AscensionOriginSource extends OriginSource {


    private final Random random = new Random();


    private final StatSheet statSheet = new StatSheet();
    private final AffinityHolder affinityHolder = new AffinityHolder();

    //──Cached data────────────────────────────────────────────────────────

    private RegistryAccess registryAccess;
    private long revision;
    private CompoundTag cachedCached;
    private ValueInput cached;



    public AscensionOriginSource(){

    }

    public AscensionOriginSource(CompoundTag input){
        this.cachedCached = input;
    }
    public AscensionOriginSource(ValueInput input){

        this.cached = input;
    }

    //──Holder Access────────────────────────────────────────────────────────

    /**
     * attempts to get a DataSource instance, creating a new one if it is not present
     * @param source the source we want to get the instance of
     * @return either an existing instance or a fresh one
     */
    protected DataSourceInstance getOrCreate(Identifier source){
        if(hasDataSource(source)) return getDataSource(source);
        DataSourceInstance instance = RPGEngineRegistries.DATA_SOURCE_REGISTRY.getValue(source).newInstance(getRegistryAccess());
        return addDataSource(source,instance)? instance:null;
    }

    protected PhysiqueHolder getPhysiqueHolder(){
        return (PhysiqueHolder) getOrCreate(CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.getId());
    }
    protected BloodlineHolder getBloodlineHolder(){
        return (BloodlineHolder)  getOrCreate(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());
    }
    protected PathHolder getPathHolder(){
        return (PathHolder) getOrCreate(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());
    }
    protected SkillHolder getSkillHolder(){
        return (SkillHolder) getOrCreate(CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
    }
    public boolean isLoaded(){ return cached != null;}

    public ValueInput getCached(){return cached;}


    public long getRevision() {
        return revision;
    }
    public void setRegistryAccess(RegistryAccess access){
        this.registryAccess = access;
    }

    //──Physique────────────────────────────────────────────────────────

    //add a fresh instance of a physique, cannot be null
    public boolean setPhysique(Identifier physique){
        if(physique == null){
            return false;
        }
        Physique physiqueInstance = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,physique,getRegistryAccess());
        if(physiqueInstance == null) return false;
        return setPhysique(physique, physiqueInstance.newData(getRegistryAccess()));

    }
    //Sets the current physique, cannot be null
    public boolean setPhysique(Identifier physique,PhysiqueData physiqueData){
        PhysiqueHolder holder = getPhysiqueHolder();
        return holder.setPhysique(physique,physiqueData);
    }

    public Identifier getPhysique(){
        PhysiqueHolder holder = getPhysiqueHolder();
        return holder.getPhysique();
    }

    public PhysiqueData getPhysiqueData(){
        PhysiqueHolder holder = getPhysiqueHolder();
        return holder.getData();
    }

    //──Bloodline────────────────────────────────────────────────────────


    public boolean addBloodline(Identifier bloodline){
        if(bloodline == null)return false;
        Bloodline bloodlineInstance = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodline,getRegistryAccess());
        if(bloodlineInstance == null) return false;
        return addBloodline(bloodline,bloodlineInstance.newData(getRegistryAccess()));
    }
    public void mergeBloodline(Identifier bloodline,BloodlineData data){

        startProcess("merge_bloodline");
        CoreRegistries.BLOODLINE_REGISTRY.get(getRegistryAccess()).getValue(bloodline).handlePurityChange(
                this,
                getBloodlineData(bloodline),
                getBloodlineData(bloodline).getPurity()+data.getPurity()
        );

        markBloodlineDirty(bloodline);
        resolveProcess("merge_bloodline");
    }
    public boolean addBloodline(Identifier bloodline,BloodlineData data){
        if(bloodline == null) return false;
        if(hasBloodline(bloodline)){
            mergeBloodline(bloodline,data);
            return true;
        }
        return getBloodlineHolder().addBloodline(bloodline,data);
    }


    public boolean removeBloodline(Identifier bloodline){
        return getBloodlineHolder().removeBloodline(bloodline);
    }


    public boolean hasBloodline(Identifier bloodline){
        return getBloodlineHolder().hasBloodline(bloodline);
    }

    public Collection<Identifier> getBloodlines(){
        return getBloodlineHolder().getBloodlines();
    }

    public BloodlineData getBloodlineData(Identifier bloodline){
        return getBloodlineHolder().getBloodline(bloodline);
    }

    //should be used if you modified a bloodlines data
    public void markBloodlineDirty(Identifier bloodline){
        long id = random.nextLong();
        startProcess("modified_bloodline"+id);
        markDataSourceDirty(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());
        getBloodlineHolder().markBloodlineDirty(bloodline);
        resolveProcess("modified_bloodline"+id);
    }
    //──Path────────────────────────────────────────────────────────

    /**
     * Adds a path, creating a fresh pathData instance
     * @param path the path to add
     * @param owner the source of this addition
     * @return true-> added, false -> not added
     */
    public boolean addPath(Identifier path,Identifier owner){
        if(path == null) return false;
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,getRegistryAccess());
        if(pathInstance == null) return false;
        return addPath(path,pathInstance.newData(getRegistryAccess()),owner);
    }

    /**
     * generic add path data (no specific event reason provided)
     * @param path the path to add
     * @param existingData either a fresh data instance or existing one
     * @param owner the source of this addition
     * @return true-> added, false -> not added
     */
    public boolean addPath(Identifier path,PathData existingData,Identifier owner){
        if(path == null || existingData == null) return false;
        return getPathHolder().addPath(path,existingData,owner);
    }


    /**
     * removes a path if there are no owners remaining
     * @param path the path to remove
     * @param owner the source of the removal
     * @return true->removed, false-> not removed
     */
    public boolean removePath(Identifier path,Identifier owner){
        return getPathHolder().removePath(path,owner);
    }


    public boolean hasPath(Identifier path){
        return getPathHolder().hasPath(path);
    }
    public PathData getPathData(Identifier path){
        return getPathHolder().getPath(path);
    }

    public Collection<Identifier> getPaths(){
        return getPathHolder().getPaths();
    }
    public Collection<Identifier> getPathOwners(Identifier path){
        return getPathHolder().getOwners(path);
    }
    //should be used if you changed a paths pathData
    //does not auto start/resolve process
    public void markPathDirty(Identifier path){
        long id = random.nextLong();
        startProcess("modified_path"+id);
        markDataSourceDirty(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());
        getPathHolder().markPathDirty(path);
        resolveProcess("modified_path"+id);

    }

    public boolean broadcastTechniqueAddedAttempt(Identifier technique, TechniqueData data){
        return true;
    }
    public void broadcastTechniqueAdded(Identifier technique, TechniqueData data){

    }
    public boolean broadcastTechniqueRemovedAttempt(Identifier technique, TechniqueData data){
        return true;
    }
    public void broadcastTechniqueRemoved(Identifier technique, TechniqueData data){

    }

    //──Skill────────────────────────────────────────────────────────

    /**
     * adds a skill, creating a new skillData instance
     * @param skill the skill we are adding
     * @param owner the source of this skill
     * @return true -> added, false -> not added
     */
    public boolean addSkill(Identifier skill,Identifier owner){
        if(skill == null) return false;
        Skill skillInstance = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,getRegistryAccess());
        if(skillInstance == null) return false;
        return addSkill(skill,skillInstance.newData(getRegistryAccess()),owner);

    }
    /**
     * adds a skill
     * @param skill the skill we are adding
     * @param data the data for this skill can be fresh or existing
     * @param owner the source of this skill
     * @return true -> added, false -> not added
     */
    public boolean addSkill(Identifier skill,SkillData data,Identifier owner){
        if(skill == null || data == null) return false;
        return getSkillHolder().addSkill(skill,data,owner);
    }

    /**
     * removes a skill only if the ownerIdMap is empty
     * @param skill the skill we want to remove
     * @param owner the source of the removal
     * @return true->removed, false -> not removed
     */
    public boolean removeSkill(Identifier skill,Identifier owner){
        return getSkillHolder().removeSkill(skill,owner);
    }

    public Collection<Identifier> getSkills(){return getSkillHolder().getSkills();}

    public boolean hasSkill(Identifier skill){
        return getSkillHolder().hasSkill(skill);
    }
    public SkillData getSkillData(Identifier skill){
        return getSkillHolder().getSkillData(skill);
    }


    public void markSkillDirty(Identifier skill){
        long id = random.nextLong();
        startProcess("modified_skill"+id);
        markDataSourceDirty(CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
        getSkillHolder().markSkillDirty(skill);
        resolveProcess("modified_skill"+id);
    }//should be used if you changed a skills skilLData

    //──Stat Sheet────────────────────────────────────────────────────────

    //NOTE im fully hiding the implementation here. i would do the same for pathData but i know i will have
    //implementation specific behaviour that i will want displayable on the client

    public void addStat(Stat stat, double val){
        statSheet.addStat(stat,val);
        updateStatSheet();
    }
    public void addStat(StatInstance instance){
        statSheet.addStat(instance);
        updateStatSheet();
    }

    public void removeStat(Stat stat, double val){
        addStat(stat,-val);
    }

    public void addStatModifier(Stat stat,ValueContainerModifier modifier){
        statSheet.addStat(stat,0); //makes sure the stat is present
        statSheet.getStatInstance(stat).addModifier(modifier);
        updateStatSheet();
    }
    public void removeStatModifier(Stat stat,Identifier identifier){
        if(statSheet.getStatInstance(stat) == null) return;
        statSheet.getStatInstance(stat).removeModifier(identifier);
        updateStatSheet();
    }

    public double getValue(Stat stat){
        return statSheet.getStatInstance(stat) != null ?
                statSheet.getStatInstance(stat).getValue() :
                0;
    }
    public double getBaseValue(Stat stat){
        return statSheet.getStatInstance(stat) != null ?
                statSheet.getStatInstance(stat).getBaseValue() :
                0;
    }


    public Collection<Stat> getAllStats(){
        return statSheet.asMap().keySet();
    }
    public void updateStatSheet(){
        Collection<LivingEntity> entities = AscensionCraft.getSourceHandler().getLoadedWatchers(this);
        for(LivingEntity entity : entities) NeoForge.EVENT_BUS.post(new StatsUpdatedEvent(entity,statSheet.getAllStats()));
    }
    public void updateAttributes(ZenithAttributeHolder holder){
        holder.update(new StatProvider() {
            @Override
            public Collection<Stat> getStats() {
                return List.of();
            }

            @Override
            public StatInstance getStatInstance(Stat stat) {
                return null;
            }

            @Override
            public double getStat(Stat stat) {
                return 0;
            }

            @Override
            public double getBaseStat(Stat stat) {
                return 0;
            }
        });//TODO fix
    }

    //──Affinity Holder────────────────────────────────────────────────────────
     //NOTES do smth similar to stat handler where i FULLY hide Implementation. then add a blank sync method
    //that is overridden by server source to sync for all watchers on a change
    //this will be done through a markDirty method that loops through all attached
    protected AffinityHolder getAffinityHolder(){return affinityHolder;}

    public void addAffinity(Identifier path,double val){
        getAffinityHolder().addAffinity(path,val);
    }
    public void removeAffinity(Identifier path,double val){
        addAffinity(path,-val);

    }
    public void addAffinityModifier(Identifier path,ValueContainerModifier modifier){
        getAffinityHolder().addAffinityModifier(path,modifier);
    }

    public void removeAffinityModifier(Identifier path,Identifier modifier){
        getAffinityHolder().removeAffinityModifier(path,modifier);
    }

    public double getAffinity(Identifier path){
        return getAffinityHolder().getAffinity(path);
    }
    public double getBaseAffinity(Identifier path){
        return getAffinityHolder().getBaseAffinity(path);
    }

    public boolean hasAffinity(Identifier path){return affinityHolder.hasAffinity(path);}

    public void addAffinity(Identifier category,Identifier path,double val){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) addAffinity(path,val);
        else affinityHolder.addAffinity(category,path,val);
    }
    public void removeAffinity(Identifier category,Identifier path,double val){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) removeAffinity(path,val);
        else affinityHolder.removeAffinity(category,path,val);

    }
    public void addAffinityModifier(Identifier category,Identifier path,ValueContainerModifier modifier){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) addAffinityModifier(path,modifier);
        affinityHolder.addAffinityModifier(category,path,modifier);
    }

    public void removeAffinityModifier(Identifier category,Identifier path,Identifier modifier){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) removeAffinityModifier(path,modifier);
        affinityHolder.removeAffinityModifier(category,path,modifier);
    }

    public double getAffinity(Identifier category,Identifier path){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) getAffinity(path);
        return affinityHolder.getAffinity(category,path);
    }
    public double getBaseAffinity(Identifier category,Identifier path){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) getBaseAffinity(path);
        return affinityHolder.getBaseAffinity(category,path);
    }
    public boolean hasAffinity(Identifier path,Identifier category){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) hasAffinity(path);
        return affinityHolder.hasAffinity(path,category);
    }

    public double getEffectiveAffinity(Identifier path){
        return getEffectiveAffinity(null,path);
    }
    public double getEffectiveAffinity(Identifier category,Identifier path){
        if(category.equals(PathEffectValueUtil.NO_CATEGORY)) return getEffectiveAffinity(path);
        return PathEffectValueUtil.getEffectValue(getAffinity(path),getAffinityHolder(),path,category);
    }
    public Collection<Identifier> getAllAffinity(){
        return affinityHolder.getPaths();
    }
    public Collection<Identifier> getAllAffinity(Identifier category){

        return category.equals(PathEffectValueUtil.NO_CATEGORY)? getAllAffinity() : affinityHolder.getPaths(category);
    }

    //──Data────────────────────────────────────────────────────────

    public void write(ValueOutput output){


    }



    /**
     * A lazy init method for SaveData, lets us hold the compoundTag to later be wrapped in TagValueInput
     * if we already have a ValueInput cached run that
     */
    public void load(){

        if(cachedCached != null) {

            if(registryAccess != null) {
                cached = TagValueInput.create(ProblemReporter.DISCARDING, registryAccess, cachedCached);
                cachedCached = null;
            }else return;
        }
        if(cached != null) {
            load(cached);
            cached = null;
        }
    }


    public void load(ValueInput input){







    }

    public void encode(RegistryFriendlyByteBuf buf) {
        /*
        SourceChangesSnapshot fullSnapshot = new SourceChangesSnapshot(
                physique,
                physiqueData,
                new HashMap<>(bloodlines),
                Set.of(),
                new HashMap<>(paths),
                Set.of(),
                new HashMap<>(skills),
                Set.of(),
                new HashMap<>(dataSources),
                Set.of(),
                getAllStatInstances(),
                new HashSet<>(affinityHolder.getAllAffinityContainers())
        );
        fullSnapshot.encode(buf);

         */
    }

/*
    private Set<StatInstance> getAllStatInstances() {

        Set<StatInstance> instances = new HashSet<>();
        for (Stat stat : getAllStats()) {
            StatInstance instance = getStatInstance(stat);
            if (instance != null) {
                instances.add(instance);
            }
        }
        return instances;


    }
   */

    public void decode(RegistryFriendlyByteBuf buf) {
         /*
        setRegistryAccess(buf.registryAccess());

        physique = null;
        physiqueData = null;
        bloodlines.clear();
        paths.clear();
        skills.clear();
        dataSources.clear();
        statSheet.asMap().clear();
        affinityHolder.clear();

        apply(SourceChangesSnapshot.decode(buf, buf.registryAccess()));
        */
    }




    /**
     * takes a snapshot and applies the changes
     *
     * if physique == null it means it was not changed
     * @param snapshot
     */
    public void apply(SourceChangesSnapshot snapshot){

    }
}
