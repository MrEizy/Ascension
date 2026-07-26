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
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
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
    protected PathBonusHolder getPathBonusHolder(){
        return (PathBonusHolder) getOrCreate(CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.getId());
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

    //TODO add something similar to stats and statProvider, but for bonuses
    //TODO create a record for category+path, that is tracked and passed to an event or handler
    //──Path Bonus Holder────────────────────────────────────────────────────────

    public void addBonus(Identifier category,Identifier path,double val){
        getPathBonusHolder().addBonus(category,path,val);
        markPathBonusHolderDirty();
    }
    public void addBonusModifier(Identifier category, Identifier path, ValueContainerModifier modifier){
        getPathBonusHolder().addBonusModifier(category,path,modifier);
        markPathBonusHolderDirty();
    }

    public void removeBonus(Identifier category,Identifier path,double val){
        getPathBonusHolder().removeBonus(category,path,val);
        markPathBonusHolderDirty();
    }

    public void removeBonusModifier(Identifier category,Identifier path,Identifier modifier){
        getPathBonusHolder().removeBonusModifier(category,path,modifier);
        markPathBonusHolderDirty();
    }

    public double getBonus(Identifier category,Identifier path){

        return getPathBonusHolder().getBonus(category,path);
    }

    protected void markPathBonusHolderDirty(){
        long id = random.nextLong();
        startProcess("modified_path_bonus"+id);
        markDataSourceDirty(CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.getId());
        resolveProcess("modified_path_bonus"+id);
    }
    //──Affinity Bonus Holder────────────────────────────────────────────────────────
    private static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");
    public void addAffinity(Identifier path,double val){
        addBonus(AFFINITY_CATEGORY,path,val);
    }
    public void addAffinityModifier(Identifier path, ValueContainerModifier modifier){
        addBonusModifier(AFFINITY_CATEGORY,path,modifier);
    }

    public void removeAffinity(Identifier path,double val){
        removeBonus(AFFINITY_CATEGORY,path,val);
    }

    public void removeAffinityModifier(Identifier path,Identifier modifier){
        removeBonusModifier(AFFINITY_CATEGORY,path,modifier);
    }

    public double getAffinity(Identifier path){
        return getBonus(AFFINITY_CATEGORY,path);
    }


    //──Data────────────────────────────────────────────────────────

    //TODO setup lazy loading



}
