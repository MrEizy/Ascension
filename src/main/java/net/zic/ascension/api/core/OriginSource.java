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

    //add a fresh instance of a physique
    public void setPhysique(Identifier physique, RegistryAccess registryAccess){
        if(physique == null){
            setPhysique(null,(PhysiqueData) null);
        }else{
            setPhysique(physique,CoreRegistries.PHYSIQUE_REGISTRY.get(registryAccess).getValue(physique).newData());
        }
    }
    public void setPhysique(Identifier physique,PhysiqueData physiqueData){
        if(this.physique != null && !this.physique.equals(physique) || physique == null) physiqueData = null;
        this.physique = physique;
        this.physiqueData = physiqueData;
    }
    public Identifier getPhysique(){
        return physique;
    }
    public PhysiqueData getPhysiqueData(){
        return physiqueData;
    }

    //──Bloodline────────────────────────────────────────────────────────

    //add a fresh instance of a bloodline
    public void addBloodline(Identifier bloodline,RegistryAccess registryAccess){
        if(bloodline == null)return;

        addBloodline(bloodline,CoreRegistries.BLOODLINE_REGISTRY.get(registryAccess).getValue(bloodline).newData());
    }
    public void addBloodline(Identifier bloodline,BloodlineData data){
        if(bloodline == null) return;
        this.bloodlines.put(bloodline,data);
    }
    public void removeBloodline(Identifier bloodline){
        bloodlines.remove(bloodline);
    }
    public Collection<Identifier> getBloodlines(){
        return bloodlines.keySet();
    }
    public BloodlineData getBloodlineData(Identifier bloodline){
        return bloodlines.get(bloodline);
    }

    //──Path────────────────────────────────────────────────────────

    public void addPath(Identifier path, RegistryAccess registryAccess){
        if(path == null) return;
        addPath(path,CoreRegistries.PATH_REGISTRY.get(registryAccess).getValue(path).newData());
    }
    //used when adding an existing path to a source
    public void addPath(Identifier path,PathData existingData){
        if(path == null || existingData == null) return;
        if(paths.containsKey(path)) return;

        //TODO simulate realm change if existing data realms are greater than 0,0 and technique != null

        paths.put(path,existingData);

    }
    public PathData removePath(Identifier path){
        return paths.remove(path);
    }
    public boolean hasPath(Identifier path){
        return paths.containsKey(path);
    }
    public PathData getPathData(Identifier path){
        return paths.get(path);
    }

    //──Skill────────────────────────────────────────────────────────

    public void addSkill(Identifier skill,RegistryAccess registryAccess){
        if(skill == null) return;
        addSkill(skill,CoreRegistries.SKILL_REGISTRY.get(registryAccess).getValue(skill).newData());

    }
    public void addSkill(Identifier skill,SkillData data){
        if(skill == null) return;
        skills.put(skill,data);
    }
    public boolean hasSkill(Identifier skill){
        return skills.containsKey(skill);
    }
    public SkillData getSkillData(Identifier skill){
        return skills.get(skill);
    }

    //──Data Source────────────────────────────────────────────────────────

    public void addDataSource(Identifier source,RegistryAccess registryAccess){
        if(source == null) return;
        addDataSource(source,CoreRegistries.DATA_SOURCE_REGISTRY.get(registryAccess).getValue(source).newInstance());
    }
    public void addDataSource(Identifier source,DataSourceInstance instance){
        dataSources.put(source,instance);
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
