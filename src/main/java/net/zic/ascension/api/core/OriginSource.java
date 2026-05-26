package net.zic.ascension.api.core;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.event.EventReason;
import net.zic.zenithlib.stats.StatSheet;

import java.util.Collection;
import java.util.HashMap;

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


    //──Physique────────────────────────────────────────────────────────

    //add a fresh instance of a physique, cannot be null
    public boolean setPhysique(Identifier physique, RegistryAccess registryAccess){
        if(physique == null){
            return false;
        }
        return setPhysique(physique,CoreRegistries.PHYSIQUE_REGISTRY.get(registryAccess).getValue(physique).newData(),registryAccess);

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

    //──Bloodline────────────────────────────────────────────────────────

    //TODO add merge logic here?
    //add a fresh instance of a bloodline
    public boolean addBloodline(Identifier bloodline,RegistryAccess registryAccess){
        if(bloodline == null)return false;
        if(hasBloodline(bloodline)) return false;
        return addBloodline(bloodline,CoreRegistries.BLOODLINE_REGISTRY.get(registryAccess).getValue(bloodline).newData(),registryAccess);
    }
    public boolean addBloodline(Identifier bloodline,BloodlineData data,RegistryAccess access){
        return addBloodline(bloodline,data, access,null);
    }
    public boolean addBloodline(Identifier bloodline,BloodlineData data,RegistryAccess access,EventReason reason){
        if(bloodline == null) return false;
        this.bloodlines.put(bloodline,data);
        return false;
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

    //──Path────────────────────────────────────────────────────────

    public boolean addPath(Identifier path, RegistryAccess registryAccess){
        if(path == null) return false;
        return addPath(path,CoreRegistries.PATH_REGISTRY.get(registryAccess).getValue(path).newData(),registryAccess);
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

    //──Skill────────────────────────────────────────────────────────

    public boolean addSkill(Identifier skill,RegistryAccess registryAccess){
        if(skill == null) return false;
        return addSkill(skill,CoreRegistries.SKILL_REGISTRY.get(registryAccess).getValue(skill).newData());

    }
    public boolean addSkill(Identifier skill,SkillData data){
        if(skill == null) return false;
        skills.put(skill,data);
        return true;
    }
    public boolean hasSkill(Identifier skill){
        return skills.containsKey(skill);
    }
    public SkillData getSkillData(Identifier skill){
        return skills.get(skill);
    }

    //──Data Source────────────────────────────────────────────────────────

    public boolean addDataSource(Identifier source,RegistryAccess registryAccess){
        if(source == null) return false;
        return addDataSource(source,CoreRegistries.DATA_SOURCE_REGISTRY.get(registryAccess).getValue(source).newInstance());
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

    //──Stat Sheet────────────────────────────────────────────────────────

    StatSheet getStatSheet(){
        return statSheet;
    }
}
