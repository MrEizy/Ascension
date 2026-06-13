package net.zic.ascension.api.core.source;

import net.minecraft.client.Minecraft;
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
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.data_source.DataSource;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.path.affinity.AffinityCategoryHolder;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.path.affinity.AffinityHolder;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.ascension.api.event.EventReason;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatSheet;
import net.zic.zenithlib.stats.event.StatsUpdatedEvent;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import oshi.util.tuples.Pair;

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
public class OriginSource {

    private Identifier physique;
    private PhysiqueData physiqueData;

    private final HashMap<Identifier,BloodlineData> bloodlines = new HashMap<>();

    private final HashMap<Identifier, PathData> paths = new HashMap<>();

    //TODO since not all skills have data consider wrapping in a skillInstance type?
    private final HashMap<Identifier, SkillData> skills = new HashMap<>();

    //???? TODO once again consider a wrapper since not all will have an instance for extra storage
    private final HashMap<Identifier, DataSourceInstance> dataSources = new HashMap<>();

    private final StatSheet statSheet = new StatSheet();
    private final AffinityHolder affinityHolder = new AffinityHolder();

    //──Cached data────────────────────────────────────────────────────────
    protected HashMap<Identifier,PathData> cachedPathData = new HashMap<>();
    protected HashMap<Identifier,SkillData> cachedSkillData = new HashMap<>();


    private RegistryAccess registryAccess;
    private long revision;
    private CompoundTag cachedCached;
    private ValueInput cached;
    public OriginSource(RegistryAccess access){
        this.registryAccess = access;
    }
    public OriginSource(CompoundTag input){
        this.cachedCached = input;
    }
    public OriginSource(RegistryAccess access,ValueInput input){

        this.registryAccess = access;
        this.cached = input;
    }
    public boolean isLoaded(){ return cached != null;}

    public ValueInput getCached(){return cached;}

    public RegistryAccess getRegistryAccess(){
       return Minecraft.getInstance().getConnection() == null ? null : Minecraft.getInstance().getConnection().registryAccess();
    }
    public long getRevision() {
        return revision;
    }
    public void setRegistryAccess(RegistryAccess access){
        this.registryAccess = access;
    }

    //──Physique────────────────────────────────────────────────────────

    //add a fresh instance of a physique, cannot be null
    public boolean setPhysique(Identifier physique, RegistryAccess registryAccess){
        if(physique == null){
            return false;
        }
        Physique physiqueInstance = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,physique,getRegistryAccess());
        if(physiqueInstance == null) return false;
        return setPhysique(physique, physiqueInstance.newData(),registryAccess);

    }
    //Sets the current physique, cannot be null
    public boolean setPhysique(Identifier physique,PhysiqueData physiqueData,RegistryAccess registryAccess){
        return setPhysique(physique,physiqueData,registryAccess,null);
    }
    public boolean setPhysique(Identifier physique, PhysiqueData physiqueData,RegistryAccess registryAccess, EventReason reason){
        if(physique == null) return false;
        if(physique.equals(this.physique)) return false;

        this.physique = physique;
        this.physiqueData = physiqueData;

        return true;
    }
    public Identifier getPhysique(){
        return physique;
    }
    public PhysiqueData getPhysiqueData(){
        return physiqueData;
    }

    public void markPhysiqueDirty(){} //should be used if you modified physiqueData
    //──Bloodline────────────────────────────────────────────────────────

    //TODO add merge logic here?
    //add a fresh instance of a bloodline
    public boolean addBloodline(Identifier bloodline,RegistryAccess registryAccess){
        if(bloodline == null)return false;
        Bloodline bloodlineInstance = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodline,getRegistryAccess());
        if(bloodlineInstance == null) return false;
        return addBloodline(bloodline,bloodlineInstance.newData(),registryAccess);
    }
    public void mergeBloodline(Identifier bloodline,BloodlineData data){

        if(this instanceof ServerOriginSource source) source.startProcess(ProcessType.MODIFY_BLOODLINE);
        CoreRegistries.BLOODLINE_REGISTRY.get(getRegistryAccess()).getValue(bloodline).handlePurityChange(
                this,
                getBloodlineData(bloodline),
                getBloodlineData(bloodline).getPurity()+data.getPurity()
        );

        markBloodlineDirty(bloodline);

    }
    public boolean addBloodline(Identifier bloodline,BloodlineData data,RegistryAccess access){
        return addBloodline(bloodline,data, access,null);
    }
    public boolean addBloodline(Identifier bloodline,BloodlineData data,RegistryAccess access,EventReason reason){
        if(bloodline == null) return false;
        if(hasBloodline(bloodline)){
            mergeBloodline(bloodline,data);
            return true;
        }
        this.bloodlines.put(bloodline,data);

        return true;
    }

    public boolean removeBloodline(Identifier bloodline,RegistryAccess access){
        return removeBloodline(bloodline,access,null);
    }
    public boolean removeBloodline(Identifier bloodline,RegistryAccess access,EventReason reason){

        return !(bloodlines.remove(bloodline) == null);
    }

    public boolean hasBloodline(Identifier bloodline){
        return bloodlines.containsKey(bloodline);
    }

    public Collection<Identifier> getBloodlines(){
        return bloodlines.keySet();
    }
    public BloodlineData getBloodlineData(Identifier bloodline){
        return bloodlines.get(bloodline);
    }

    public void markBloodlineDirty(Identifier bloodline){}//should be used if you modified a bloodlines data
    //──Path────────────────────────────────────────────────────────

    public boolean addPath(Identifier path, RegistryAccess registryAccess){
        if(path == null) return false;
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,getRegistryAccess());
        if(pathInstance == null) return false;
        return addPath(path,pathInstance.newData(registryAccess),registryAccess);
    }
    //used when adding an existing path to a source
    public boolean addPath(Identifier path,PathData existingData,RegistryAccess registryAccess){
        return addPath(path,existingData,registryAccess,null);
    }
    public boolean addPath(Identifier path,PathData existingData,RegistryAccess registryAccess,EventReason reason){
        if(path == null || existingData == null) return false;
        if(paths.containsKey(path)) return false;

        //TODO simulate realm change if existing data realms are greater than 0,0 and technique != null

        paths.put(path,existingData);
        return true;
    }

    public Collection<Identifier> getPaths(){
        return paths.keySet();
    }
    public boolean removePath(Identifier path,RegistryAccess access){return removePath(path,access,null);}
    public boolean removePath(Identifier path,RegistryAccess access,EventReason reason){
        if(!paths.containsKey(path)) return false;

        paths.remove(path);
        return true;
    }

    public boolean hasPath(Identifier path){
        return paths.containsKey(path);
    }
    public PathData getPathData(Identifier path){
        return paths.get(path);
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
    public void markPathDirty(Identifier path){}//should be used if you changed a paths pathData

    //──Skill────────────────────────────────────────────────────────

    public boolean addSkill(Identifier skill,RegistryAccess registryAccess){
        if(skill == null) return false;
        Skill skillInstance = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,getRegistryAccess());
        if(skillInstance == null) return false;
        return addSkill(skill,skillInstance.newData());

    }
    public boolean addSkill(Identifier skill,SkillData data){
        if(skill == null) return false;
        skills.put(skill,data);
        return true;
    }

    public boolean removeSkill(Identifier skill,RegistryAccess registryAccess){
        skills.remove(skill);
        return true;
    }

    public Collection<Identifier> getSkills(){return skills.keySet();}

    public boolean hasSkill(Identifier skill){
        return skills.containsKey(skill);
    }
    public SkillData getSkillData(Identifier skill){
        return skills.get(skill);
    }

    public void markSkillDirty(Identifier skill){}//should be used if you changed a skills skilLData
    //──Data Source────────────────────────────────────────────────────────

    public boolean addDataSource(Identifier source,RegistryAccess registryAccess){
        if(source == null) return false;
        DataSource dataSource = CoreRegistries.safeAccess(CoreRegistries.DATA_SOURCE_REGISTRY,source,getRegistryAccess());
        if(dataSource == null) return false;
        return addDataSource(source,dataSource.newInstance());
    }
    public boolean addDataSource(Identifier source,DataSourceInstance instance){
        if(source == null) return false;
        dataSources.put(source,instance);
        return true;
    }

    public DataSourceInstance getDataSourceInstance(Identifier source){
        return dataSources.get(source);
    }

    public boolean hasDataSource(Identifier source){
        return dataSources.containsKey(source);
    }

    public DataSourceInstance removeDataSource(Identifier source){
        return dataSources.remove(source);
    }


    public void markDataSourceDirty(Identifier source){}//should be used if you changed a data sources instance
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
    protected StatInstance getStatInstance(Stat stat){
        return statSheet.getStatInstance(stat);
    }

    public Collection<Stat> getAllStats(){
        return statSheet.asMap().keySet();
    }
    public void updateStatSheet(){
        Collection<LivingEntity> entities = AscensionCraft.getSourceHandler().getLoadedWatchers(this);
        for(LivingEntity entity : entities) NeoForge.EVENT_BUS.post(new StatsUpdatedEvent(entity,statSheet));
    }
    public void updateAttributes(ZenithAttributeHolder holder){
        holder.update(statSheet.asMap());
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

    public void addAffinity(Identifier category,Identifier path,double val){
        affinityHolder.addAffinity(category,path,val);
    }
    public void removeAffinity(Identifier category,Identifier path,double val){
        affinityHolder.removeAffinity(category,path,val);

    }
    public void addAffinityModifier(Identifier category,Identifier path,ValueContainerModifier modifier){
        affinityHolder.addAffinityModifier(category,path,modifier);
    }

    public void removeAffinityModifier(Identifier category,Identifier path,Identifier modifier){
        affinityHolder.removeAffinityModifier(category,path,modifier);
    }

    public double getAffinity(Identifier category,Identifier path){

        return affinityHolder.getAffinity(category,path);
    }
    public double getBaseAffinity(Identifier category,Identifier path){
        return affinityHolder.getBaseAffinity(category,path);
    }

    //──Data────────────────────────────────────────────────────────

    public void write(ValueOutput output){


        AscensionCraft.LOGGER.debug("Saving Physique");
        try{
            ValueOutput physiqueOutput = output.child("physique");
            NbtHelpers.writeIdentifier(physiqueOutput,"id",getPhysique());
            ValueOutput data = physiqueOutput.child("data");
            if(getPhysiqueData() != null) getPhysiqueData().write(data);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error writing physique {}",getPhysique());
            AscensionCraft.LOGGER.error("stacktrace: ",e);
        }
        AscensionCraft.LOGGER.debug("Finished Saving Physique");
        AscensionCraft.LOGGER.debug("Saving Bloodlines");

        ValueOutput.ValueOutputList bloodlines = output.childrenList("bloodlines");
        for(Identifier bloodline : getBloodlines()){
            AscensionCraft.LOGGER.debug("Saving Bloodline {}",bloodline);
           try{
               ValueOutput bloodlineOutput = bloodlines.addChild();
               NbtHelpers.writeIdentifier(bloodlineOutput,"id",bloodline);
               ValueOutput dataOutput = bloodlineOutput.child("data");
               getBloodlineData(bloodline).write(dataOutput);
           } catch (Exception e){
               AscensionCraft.LOGGER.error("error writing bloodline {}",bloodline);
               AscensionCraft.LOGGER.error("stacktrace: ",e);
           }

        }

        AscensionCraft.LOGGER.debug("Finished Saving Bloodlines");

        AscensionCraft.LOGGER.debug("Saving Skill Data");

        ValueOutput.ValueOutputList skills = output.childrenList("skills");
        for(Identifier skill : getSkills()){
            AscensionCraft.LOGGER.debug("Saving Skill {}",skill);
            try {
                ValueOutput skillOutput = skills.addChild();
                NbtHelpers.writeIdentifier(skillOutput,"skill",skill);
                ValueOutput skillData = skillOutput.child("data");
                if(getSkillData(skill) != null) getSkillData(skill).write(skillData);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("error writing skill {}",skill);
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
        AscensionCraft.LOGGER.debug("Finished Saving Skill Data");
        AscensionCraft.LOGGER.debug("Saving Skill Path Data");

        ValueOutput.ValueOutputList paths = output.childrenList("paths");
        for(Identifier path : getPaths()){
            AscensionCraft.LOGGER.debug("Saving Path {}",path);
            try {
                ValueOutput pathOutput = paths.addChild();
                NbtHelpers.writeIdentifier(pathOutput,"path",path);
                ValueOutput pathData = pathOutput.child("data");
                if(getPathData(path) != null) getPathData(path).write(pathData);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("error writing path {}",path);
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
        AscensionCraft.LOGGER.debug("Finished Saving Skill Path Data");
    }

    public void load(RegistryAccess access){
        setRegistryAccess(access);
        load();
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
        AscensionCraft.LOGGER.debug("Reading Skill Data");
        cachedSkillData.clear();
        ValueInput.ValueInputList skillsInput = input.childrenListOrEmpty("skills");

        for(ValueInput skillInput : skillsInput){

            try {
                Identifier skillId = NbtHelpers.readIdentifier(skillInput,"skill");
                ValueInput skillData = skillInput.childOrEmpty("data");

                Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skillId,getRegistryAccess());
                if(skill == null) continue;

                SkillData data = skill.loadData(skillData);
                cachedSkillData.put(skillId,data);

            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error loading skill");
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }
        }

        AscensionCraft.LOGGER.debug("Reading Path Data");
        cachedPathData.clear();
        ValueInput.ValueInputList pathsInput = input.childrenListOrEmpty("paths");
        for(ValueInput pathInput : pathsInput){
            try {
                Identifier pathId = NbtHelpers.readIdentifier(pathInput,"path");
                ValueInput pathData = pathInput.childOrEmpty("data");

                Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,getRegistryAccess());
                if(path == null) continue;

                PathData data = path.loadData(pathData,getRegistryAccess());
                cachedPathData.put(pathId,data);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error loading path");
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }
        }



        AscensionCraft.LOGGER.debug("Reading Physique");
        try{
            ValueInput physiqueInput = input.child("physique").get();
            Identifier id = NbtHelpers.readIdentifier(physiqueInput,"id");


            Optional<ValueInput> data = physiqueInput.child("data");
            PhysiqueData physiqueData = data.map(valueInput -> CoreRegistries.PHYSIQUE_REGISTRY.get(getRegistryAccess()).getValue(id).loadData(valueInput)).orElse(CoreRegistries.PHYSIQUE_REGISTRY.get(getRegistryAccess()).getValue(id).newData());

            setPhysique(id,physiqueData, getRegistryAccess());

            AscensionCraft.LOGGER.info("Loaded physique {}",id);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error loading physique");
            AscensionCraft.LOGGER.error("stacktrace : ",e);
            //TODO set technique to default
        }
        AscensionCraft.LOGGER.debug("Finished Reading Physique");
        AscensionCraft.LOGGER.debug("Reading Bloodlines");
        try {
            ValueInput.ValueInputList bloodlinesInput = input.childrenListOrEmpty("bloodlines");

            for(ValueInput bloodlineInput : bloodlinesInput.stream().toList()){
                try {
                    Identifier id = NbtHelpers.readIdentifier(bloodlineInput,"id");
                    AscensionCraft.LOGGER.debug("Reading Bloodline {}",id);
                    Optional<ValueInput> data = bloodlineInput.child("data");
                    Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,id,getRegistryAccess());
                    if(data.isEmpty()) addBloodline(id,registryAccess);
                    else addBloodline(id,bloodline.loadData(data.get()),registryAccess);

                    AscensionCraft.LOGGER.info("Loaded bloodline {} with purity {}",id,getBloodlineData(id).getPurity());
                } catch (Throwable throwable){
                    AscensionCraft.LOGGER.error("error loading bloodline");
                    AscensionCraft.LOGGER.error("stacktrace : ",throwable);
                }
                AscensionCraft.LOGGER.debug("Finished Reading Bloodline");
            }
        } catch (Exception e){
            AscensionCraft.LOGGER.error("error loading all bloodlines");
            AscensionCraft.LOGGER.error("stacktrace : ",e);
        }
        AscensionCraft.LOGGER.debug("Finished Reading Bloodlines");




        cachedPathData.clear();
        cachedSkillData.clear();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
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
    }


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

    public void decode(RegistryFriendlyByteBuf buf) {
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
    }

    /**
     * takes a snapshot and applies the changes
     *
     * if physique == null it means it was not changed
     * @param snapshot
     */
    public void apply(SourceChangesSnapshot snapshot){
        AscensionCraft.LOGGER.debug("Applying source update");
        if(snapshot.physique != null){
            this.physique = snapshot.physique;
            this.physiqueData = snapshot.physiqueData;
        }

        for(Pair<Identifier,BloodlineData> bloodline : snapshot.toAddBloodlines) bloodlines.put(bloodline.getA(),bloodline.getB());

        for(Identifier toRemove : snapshot.toRemoveBloodline) bloodlines.remove(toRemove);

        for(Pair<Identifier,PathData> path : snapshot.toAddPaths) paths.put(path.getA(),path.getB());

        for(Identifier toRemove :snapshot.toRemovePaths) paths.remove(toRemove);

        for(Pair<Identifier,SkillData> skill : snapshot.toAddSkills) skills.put(skill.getA(),skill.getB());

        for(Identifier toRemove : snapshot.toRemoveSkills) skills.remove(toRemove);

        for(Pair<Identifier,DataSourceInstance> dataSource : snapshot.toAddDataSources) dataSources.put(dataSource.getA(),dataSource.getB());

        for(Identifier toRemove : snapshot.toRemoveDataSources) dataSources.remove(toRemove);


        for(StatInstance stat : snapshot.dirtyStats) statSheet.setStat(stat);

        for(ValueContainer affinity : snapshot.dirtyAffinity) affinityHolder.setAffinity(affinity);
        for(Identifier category : snapshot.dirtyCategorizedAffinity.keySet()){

            for (ValueContainer container : snapshot.dirtyCategorizedAffinity.get(category)) affinityHolder.setAffinity(category,container);
        }
        revision++;
        AscensionCraft.LOGGER.debug("Finished applying source update at revision {}", revision);



    }
}
