package net.zic.ascension.api.ascension.core.source;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineHolder;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.physique.PhysiqueHolder;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillHolder;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.event.bloodline.BloodlineAddedEvent;
import net.zic.ascension.api.ascension.event.bloodline.BloodlineRemovedEvent;
import net.zic.ascension.api.ascension.event.path.PathAddedEvent;
import net.zic.ascension.api.ascension.event.path.PathRemovedEvent;
import net.zic.ascension.api.ascension.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.api.ascension.event.skill.SkillAddedEvent;
import net.zic.ascension.api.ascension.event.skill.SkillRemovedEvent;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Contains methods to interact with an origin source.
 * These methods are specific to Ascension DataSources
 * includes a combination of getters and setters
 */
public class AscensionOriginSourceHelper {

    private static Random random=  new Random();

    //──Holder Access────────────────────────────────────────────────────────

    /**
     * attempts to get a DataSource instance, creating a new one if it is not present
     * @param source the source we want to get the instance of
     * @return either an existing instance or a fresh one
     */
    protected static DataSourceInstance getOrCreate(OriginSource source,Identifier dataSource){
        if(source.hasDataSource(dataSource)) return source.getDataSource(dataSource);
        DataSourceInstance instance = RPGEngineRegistries.DATA_SOURCE_REGISTRY.getValue(dataSource).newInstance(source.getRegistryAccess());
        return source.addDataSource(dataSource,instance)? instance:null;
    }
    protected static PhysiqueHolder getPhysiqueHolder(OriginSource source){
        return (PhysiqueHolder) getOrCreate(source,CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.getId());
    }
    protected static BloodlineHolder getBloodlineHolder(OriginSource source){
        return (BloodlineHolder)  getOrCreate(source,CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());
    }
    protected static PathHolder getPathHolder(OriginSource source){
        return (PathHolder) getOrCreate(source,CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());
    }
    protected static SkillHolder getSkillHolder(OriginSource source){
        return (SkillHolder) getOrCreate(source,CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
    }
    protected static PathBonusHolder getPathBonusHolder(OriginSource source){
        return (PathBonusHolder) getOrCreate(source,CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.getId());
    }

    //TODO add the methods that dont take in data and create a fresh instance instead

    //──Physique Access────────────────────────────────────────────────────────

    public static Identifier getPhysiqueId(OriginSource source){
        return getPhysiqueHolder(source).getPhysique();
    }
    public static Physique getPhysique(OriginSource source){
        return getPhysiqueHolder(source).getPhysique(source.getRegistryAccess());
    }
    public static PhysiqueData getPhysiqueData(OriginSource source){
        return getPhysiqueHolder(source).getData();
    }
    public static boolean setPhysique(OriginSource source,Identifier physique){
        if(physique == null){
            return false;
        }
        Physique physiqueInstance = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,physique,source.getRegistryAccess());
        if(physiqueInstance == null) return false;
        return setPhysique(source,physique, physiqueInstance.newData(source.getRegistryAccess()));
    }
    public static boolean setPhysique(OriginSource source, Identifier physique, PhysiqueData physiqueData) {
        if(physique == null) return false;

        PhysiqueHolder holder = getPhysiqueHolder(source);

        Identifier oldPhysique = holder.getPhysique();
        PhysiqueData oldPhysiqueData = holder.getData();

        PhysiqueChangedEvent.Pre pre = new PhysiqueChangedEvent.Pre(oldPhysique,oldPhysiqueData,physique,physiqueData,source);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;
        if(pre.getNewPhysiqueIdentifier() == null) return false;



        boolean result = holder.setPhysique(pre.getNewPhysiqueIdentifier(),pre.getNewPhysiqueData());
        if(!result) return false;

        source.markDataSourceDirty(CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.getId());

        source.startProcess("set_physique");

        Collection<Identifier> toRemove = oldPhysique == null ? List.of() : pre.getPhysique(source.getRegistryAccess()).onRemoved(source,oldPhysiqueData);
        if(pre.getPhysique(source.getRegistryAccess()) != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                pre.getPhysique(source.getRegistryAccess()).removeFromEntity(entity,oldPhysiqueData);
            }
        }
        Collection<Identifier> toAdd = pre.getNewPhysique(source.getRegistryAccess()).onAdded(source, pre.getNewPhysiqueData());
        if(pre.getNewPhysique(source.getRegistryAccess()) != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                pre.getNewPhysique(source.getRegistryAccess()).applyToEntity(entity,pre.getNewPhysiqueData());
            }
        }
        PhysiqueChangedEvent.Post post = new PhysiqueChangedEvent.Post(oldPhysique,oldPhysiqueData,pre.getNewPhysiqueIdentifier(),pre.getNewPhysiqueData(),source);
        NeoForge.EVENT_BUS.post(post);


        //first add all new paths with the new physique as owner, this ensures that if there is path overlap there is owners >1

        for(Identifier path : toAdd){
            if(toRemove.contains(path)) continue;
            addPath(source,path,post.getNewPhysiqueIdentifier());
        }
        for(Identifier path : toRemove){
            if(toAdd.contains(path)) continue;
            removePath(source,path,oldPhysique);
        }
        resolveProcess(source,"set_physique");
        return true;
    }

    //should be used if you modified a physique data
    public static void markPhysiqueDirty(OriginSource source,Identifier bloodline){
        long id = random.nextLong();
        source.startProcess("modified_physique"+id);
        source.markDataSourceDirty(CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.getId());
        resolveProcess(source,"modified_bloodline"+id);
    }
    //──Bloodline Access────────────────────────────────────────────────────────


    public static Collection<Identifier> getBloodlines(OriginSource source){
       return getBloodlineHolder(source).getBloodlines();
    }
    public static Bloodline getBloodline(OriginSource source,Identifier bloodline){
        return getBloodlineHolder(source).getBloodline(bloodline,source.getRegistryAccess());
    }
    public static BloodlineData getBloodlineData(OriginSource source,Identifier bloodline){
        return getBloodlineHolder(source).getBloodline(bloodline);
    }
    public static boolean hasBloodline(OriginSource source,Identifier bloodline){
        return getBloodlineHolder(source).hasBloodline(bloodline);
    }
    public static boolean addBloodline(OriginSource source,Identifier bloodline){
        if(bloodline == null)return false;
        Bloodline bloodlineInstance = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodline,source.getRegistryAccess());
        if(bloodlineInstance == null) return false;
        return addBloodline(source,bloodline,bloodlineInstance.newData(source.getRegistryAccess()));
    }
    public static void mergeBloodline(OriginSource source,Identifier bloodline,BloodlineData data){

        source.startProcess("merge_bloodline");
        CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getValue(bloodline).handlePurityChange(
                source,
                getBloodlineData(source,bloodline),
                getBloodlineData(source,bloodline).getPurity()+data.getPurity()
        );

        getBloodlineHolder(source).markBloodlineDirty(bloodline);
        source.markDataSourceDirty(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());
        resolveProcess(source,"merge_bloodline");
    }
    //TODO consider creating a replace bloodline event as well
    public static boolean addBloodline(OriginSource source,Identifier bloodline, BloodlineData data ) {
        if(bloodline == null) return false;
        if(getBloodlineHolder(source).hasBloodline(bloodline)) {
            source.startProcess("merge_bloodline");
            mergeBloodline(source,bloodline,data);
            resolveProcess(source,"merge_bloodline");
            return true;
        };

        BloodlineAddedEvent.Pre pre = new BloodlineAddedEvent.Pre(bloodline,data,source);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = getBloodlineHolder(source).addBloodline(bloodline, data);
        if(!result) return false;

        source.startProcess("add_bloodline");
        int purity = data.getPurity();
        data.setPurity(1);
        Collection<Identifier> toAdd = pre.getBloodline(source.getRegistryAccess()).onAdded(source,pre.getBloodlineData());

        if(pre.getBloodline(source.getRegistryAccess()) != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                pre.getBloodline(source.getRegistryAccess()).applyToEntity(entity,pre.getBloodlineData());
            }
        }
        pre.getBloodline(source.getRegistryAccess()).handlePurityChange(source,data,purity);

        for(Identifier path : toAdd){
            addPath(source,path,pre.getBloodlineIdentifier());
        }



        BloodlineAddedEvent.Post post= new BloodlineAddedEvent.Post(bloodline,data,source);
        NeoForge.EVENT_BUS.post(post);
        source.markDataSourceDirty(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());

        resolveProcess(source,"add_bloodline");
        return true;

    }
    public static boolean removeBloodline(OriginSource source,Identifier bloodline) {
        if(bloodline == null) return false;
        if(!getBloodlineHolder(source).hasBloodline(bloodline)) return false;

        BloodlineData data = getBloodlineHolder(source).getBloodline(bloodline);
        BloodlineRemovedEvent.Pre pre = new BloodlineRemovedEvent.Pre(bloodline,data,source);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;



        boolean result =  getBloodlineHolder(source).removeBloodline(bloodline);
        if(!result) return false;

        source.startProcess("remove_bloodline");
        pre.getBloodline(source.getRegistryAccess()).handlePurityChange(source,data,1);

        Collection<Identifier> toRemove = pre.getBloodline(source.getRegistryAccess()).onRemoved(source,pre.getBloodlineData());
        if(pre.getBloodline(source.getRegistryAccess()) != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                pre.getBloodline(source.getRegistryAccess()).removeFromEntity(entity,pre.getBloodlineData());
            }
        }
        for(Identifier path : toRemove){
            removePath(source,path,pre.getBloodlineIdentifier());
        }

        BloodlineRemovedEvent.Post post= new BloodlineRemovedEvent.Post(bloodline,data,source);
        NeoForge.EVENT_BUS.post(post);

        source.markDataSourceDirty(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());

        resolveProcess(source,"remove_bloodline");
        return true;
    }

    //should be used if you modified a bloodlines data
    public static void markBloodlineDirty(OriginSource source,Identifier bloodline){
        long id = random.nextLong();
        source.startProcess("modified_bloodline"+id);
        source.markDataSourceDirty(CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.getId());
        getBloodlineHolder(source).markBloodlineDirty(bloodline);
        resolveProcess(source,"modified_bloodline"+id);
    }
    //──Path Access────────────────────────────────────────────────────────

    //TODO add accessor methods
    public static boolean hasPath(OriginSource source,Identifier path){
        return getPathHolder(source).hasPath(path);
    }
    public static PathData getPathData(OriginSource source,Identifier path){
        return getPathHolder(source).getPath(path);
    }

    public static Collection<Identifier> getPaths(OriginSource source){
        return getPathHolder(source).getPaths();
    }
    public static Collection<Identifier> getPathOwners(OriginSource source,Identifier path){
        return getPathHolder(source).getOwners(path);
    }

    /**
     * Adds a path, creating a fresh pathData instance
     * @param path the path to add
     * @param owner the source of this addition
     * @return true-> added, false -> not added
     */
    public static boolean addPath(OriginSource source,Identifier path,Identifier owner){
        if(path == null) return false;
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,source.getRegistryAccess());
        if(pathInstance == null) return false;
        return addPath(source,path,pathInstance.newData(source.getRegistryAccess()),owner);
    }

    public static boolean addPath(OriginSource source,Identifier path, PathData existingData, Identifier owner) {

        if(path == null || existingData == null) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).containsKey(path)) return false;
        if(getPathHolder(source).hasCachedPath(path)) existingData = getPathHolder(source).removeCachedPath(path);
        PathAddedEvent.Pre pre = new PathAddedEvent.Pre(path,existingData,source);

        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = getPathHolder(source).addPath(path, existingData,owner);
        if(!result) return false;

        source.startProcess("add_path");

        existingData.simulateProgression(source);
        PathAddedEvent.Post post = new PathAddedEvent.Post(path,existingData,source);
        NeoForge.EVENT_BUS.post(post);

        source.markDataSourceDirty(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());

        resolveProcess(source,"add_path");
        return true;
    }


    public static boolean removePath(OriginSource source,Identifier path,Identifier owner) {
        if(path == null || !getPathHolder(source).hasPath(path)) return false;
        if(!CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).containsKey(path)) return false;
        PathData data = getPathHolder(source).getPath(path);
        PathRemovedEvent.Pre pre = new PathRemovedEvent.Pre(path,data,source);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = getPathHolder(source).removePath(path,owner);
        if(!result) return false;

        source.startProcess("remove_path");
        data.removeFromSource(source);
        PathRemovedEvent.Post post = new PathRemovedEvent.Post(path,data,source);
        NeoForge.EVENT_BUS.post(post);

        source.markDataSourceDirty(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());


        resolveProcess(source,"remove_path");
        return true;
    }

    //should be used if you changed a paths pathData
    public static void markPathDirty(OriginSource source,Identifier path){
        long id = random.nextLong();
        source.startProcess("modified_path"+id);
        source.markDataSourceDirty(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId());
        getPathHolder(source).markPathDirty(path);
        resolveProcess(source,"modified_path"+id);

    }

    //──Skill Access────────────────────────────────────────────────────────

    public static Collection<Identifier> getSkills(OriginSource source){return getSkillHolder(source).getSkills();}

    public static boolean hasSkill(OriginSource source,Identifier skill){
        return getSkillHolder(source).hasSkill(skill);
    }
    public static SkillData getSkillData(OriginSource source,Identifier skill){
        return getSkillHolder(source).getSkillData(skill);
    }

    /**
     * adds a skill, creating a new skillData instance
     * @param skill the skill we are adding
     * @param owner the source of this skill
     * @return true -> added, false -> not added
     */
    public static boolean addSkill(OriginSource source,Identifier skill,Identifier owner){
        if(skill == null) return false;
        Skill skillInstance = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,source.getRegistryAccess());
        if(skillInstance == null) return false;
        return addSkill(source,skill,skillInstance.newData(source.getRegistryAccess()),owner);

    }
    public static boolean addSkill(OriginSource source,Identifier skill, SkillData data, Identifier owner) {

        if(skill == null) return false;
        if(!CoreRegistries.SKILL_REGISTRY.get(source.getRegistryAccess()).containsKey(skill)) return false;
        if(getSkillHolder(source).hasCachedSkill(skill))  data = getSkillHolder(source).removeCachedSkill(skill);

        SkillAddedEvent.Pre pre = new SkillAddedEvent.Pre(source,skill,data);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = getSkillHolder(source).addSkill(skill, data,owner);
        if(!result) return false;

        source.startProcess("add_skill");
        if(pre.getSkill(source.getRegistryAccess()) != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                pre.getSkill(source.getRegistryAccess()).applyToEntity(entity,pre.getSkillData());
            }
        }
        pre.getSkill(source.getRegistryAccess()).onAdded(source,data);

        SkillAddedEvent.Post post = new SkillAddedEvent.Post(source,skill,data);

        NeoForge.EVENT_BUS.post(post);

        source.markDataSourceDirty(CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
        resolveProcess(source,"add_skill");
        return true;
    }

    public static boolean removeSkill(OriginSource source,Identifier skill,Identifier owner) {
        if(skill == null || !getSkillHolder(source).hasSkill(skill)) return false;
        if(!CoreRegistries.SKILL_REGISTRY.get(source.getRegistryAccess()).containsKey(skill)) return false;
        SkillData data = getSkillHolder(source).getSkillData(skill);


        SkillRemovedEvent.Pre pre = new SkillRemovedEvent.Pre(source,skill,data);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = getSkillHolder(source).removeSkill(skill,owner);
        if(!result) return false;

        source.startProcess("remove_skill");
        pre.getSkill(source.getRegistryAccess()).onRemoved(source,data);
        if(pre.getSkill(source.getRegistryAccess()) != null){
            for(LivingEntity entity : source.getAttachedEntities()){
                pre.getSkill(source.getRegistryAccess()).removeFromEntity(entity,pre.getSkillData());
            }
        }
        SkillRemovedEvent.Post post = new SkillRemovedEvent.Post(source,skill,data);
        NeoForge.EVENT_BUS.post(post);

        source.markDataSourceDirty(CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
        resolveProcess(source,"remove_skill");
        return true;
    }

    public static void markSkillDirty(OriginSource source,Identifier skill){
        long id = random.nextLong();
        source.startProcess("modified_skill"+id);
        source.markDataSourceDirty(CoreHolderProviders.SKILL_HOLDER_PROVIDER.getId());
        getSkillHolder(source).markSkillDirty(skill);
        resolveProcess(source,"modified_skill"+id);
    }//should be used if you changed a skills skilLData

    //──Path Bonus Access────────────────────────────────────────────────────────

    public static void addBonus(OriginSource source,Identifier category,Identifier path,double val){
        getPathBonusHolder(source).addBonus(category,path,val);
        markPathBonusHolderDirty(source);
    }
    public static void addBonusModifier(OriginSource source,Identifier category, Identifier path, ValueContainerModifier modifier){
        getPathBonusHolder(source).addBonusModifier(category,path,modifier);
        markPathBonusHolderDirty(source);
    }

    public static void removeBonus(OriginSource source,Identifier category,Identifier path,double val){
        getPathBonusHolder(source).removeBonus(category,path,val);
        markPathBonusHolderDirty(source);
    }

    public static void removeBonusModifier(OriginSource source,Identifier category,Identifier path,Identifier modifier){
        getPathBonusHolder(source).removeBonusModifier(category,path,modifier);
        markPathBonusHolderDirty(source);
    }



    public static ValueContainer getPathBonusContainer(OriginSource source,Identifier category, Identifier path) {
        return getPathBonusHolder(source).getPathBonusContainer(category,path);
    }


    public static double getPathBonus(OriginSource source,Identifier category, Identifier path) {
        return getPathBonusHolder(source).getBonus(category,path);
    }


    public static Collection<PathBonus> getAllPathBonuses(OriginSource source) {
        return getPathBonusHolder(source).getAllPathBonuses();
    }

    //TODO handle technique methods

    public static boolean broadcastTechniqueAddedAttempt(OriginSource source,Identifier technique, TechniqueData data){
        return true;
    }
    public static void broadcastTechniqueAdded(OriginSource source,Identifier technique, TechniqueData data){

    }
    public static boolean broadcastTechniqueRemovedAttempt(OriginSource source,Identifier technique, TechniqueData data){
        return true;
    }
    public static void broadcastTechniqueRemoved(OriginSource source,Identifier technique, TechniqueData data){

    }

    public static void markPathBonusHolderDirty(OriginSource source){
        long id = random.nextLong();
        source.startProcess("modified_path_bonus"+id);
        source.markDataSourceDirty(CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.getId());
        resolveProcess(source,"modified_path_bonus"+id);
    }

    //──Affinity Quick Access────────────────────────────────────────────────────────
    public static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");

    public static void addAffinity(OriginSource source,Identifier path,double val){
        addBonus(source,AFFINITY_CATEGORY,path,val);
    }
    public static void addAffinityModifier(OriginSource source,Identifier path, ValueContainerModifier modifier){
        addBonusModifier(source,AFFINITY_CATEGORY,path,modifier);
    }

    public static void removeAffinity(OriginSource source,Identifier path,double val){
        removeBonus(source,AFFINITY_CATEGORY,path,val);
    }

    public static void removeAffinityModifier(OriginSource source,Identifier path,Identifier modifier){
        removeBonusModifier(source,AFFINITY_CATEGORY,path,modifier);
    }

    public static double getAffinity(OriginSource source,Identifier path){
        return getPathBonus(source,AFFINITY_CATEGORY,path);
    }

    //──Sync────────────────────────────────────────────────────────



    public static void resolveProcess(OriginSource source,String process){
        if(source.resolveProcess("set_physique")) initializeSync(source);
    }
    public static void initializeSync(OriginSource source){
        OriginSourcePatch patch = source.resolvePatch();

        for(LivingEntity entity : source.getAttachedEntities()){
            AscensionEntityDataProvider holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            holder.getData(entity).markDirty(patch);
        }

    }
}
