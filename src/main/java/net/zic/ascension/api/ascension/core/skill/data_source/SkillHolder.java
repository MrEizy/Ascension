package net.zic.ascension.api.ascension.core.skill.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SkillHolder implements DataSourceInstance<SkillDataSource> {

    final HashMap<Identifier, SkillData> skills = new HashMap<>();
    final HashMap<Identifier, HashSet<Identifier>> skillOwners = new HashMap<>();

    final HashSet<Identifier> dirtySkills = new HashSet<>();
    final HashSet<Identifier> toRemoveSkills = new HashSet<>();

    final HashMap<Identifier,SkillData> cachedSkills =  new HashMap<>();

    /**
     * adds a skill
     * @param skill the skill we are adding
     * @param data the data for this skill can be fresh or existing
     * @param owner the source of this skill
     * @return true -> added, false -> not added
     */
    public boolean addSkill(Identifier skill,SkillData data,Identifier owner){
        if(skill == null) return false;
        if(hasCachedSkill(skill)) data = removeCachedSkill(skill);

        skills.put(skill,data);

        skillOwners.computeIfAbsent(skill,key->new HashSet<>());
        skillOwners.get(skill).add(owner);

        markSkillDirty(skill);

        return true;
    }
    public boolean addSkill(Identifier skill,SkillData data,Identifier owner, boolean overwrite){
        if((hasSkill(skill) && overwrite) || !hasSkill(skill)) {
            if(hasCachedSkill(skill)) data = removeCachedSkill(skill);
            skills.put(skill,data);
            skillOwners.computeIfAbsent(skill,key->new HashSet<>());
        }
        skillOwners.get(skill).add(owner);
        return true;
    }
    /**
     * removes a skill only if the ownerIdMap is empty
     * @param skill the skill we want to remove
     * @param owner the source of the removal
     * @return true->removed, false -> not removed
     */
    public boolean removeSkill(Identifier skill,Identifier owner){
        if(!skillOwners.containsKey(skill)) return false;
        skillOwners.get(skill).remove(owner);

        if(!skillOwners.get(skill).isEmpty()) return false;

        skills.remove(skill);
        skillOwners.remove(skill);

        toRemoveSkills.add(skill);
        dirtySkills.remove(skill);
        return true;
    }

    public Collection<Identifier> getSkills(){return skills.keySet();}

    public boolean hasSkill(Identifier skill){
        return skills.containsKey(skill);
    }
    public SkillData getSkillData(Identifier skill){
        return skills.get(skill);
    }
    public Skill getSkill(Identifier skill, RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,access);
    }
    protected Map<Identifier,SkillData> getAllSkills(){
        return skills;
    }


    public void markSkillDirty(Identifier skill){
        dirtySkills.add(skill);
    }//should be used if you changed a skills skilLData

    //──Cached Data────────────────────────────────────────────────────────
    public void addCachedSkill(Identifier skill,SkillData skillData){
        cachedSkills.put(skill,skillData);
    }
    public boolean hasCachedSkill(Identifier skill){
        return cachedSkills.containsKey(skill);
    }
    public SkillData removeCachedSkill(Identifier skill){
        return cachedSkills.remove(skill);
    }
    public void clearCache(){
        cachedSkills.clear();
    }


    //──Raw Manipulation────────────────────────────────────────────────────────
    public Map<Identifier,SkillData> getRawSkillData(){
        return Map.copyOf(skills);
    }
    public Map<Identifier,HashSet<Identifier>> getRawSkillOwnerData(){
        return Map.copyOf(skillOwners);
    }

    public void setRawData(Map<Identifier,SkillData> rawSkills,Map<Identifier,HashSet<Identifier>> rawOwnerData){
        skills.clear();
        skillOwners.clear();
        skills.putAll(rawSkills);
        skillOwners.putAll(rawOwnerData);
        dirtySkills.addAll(rawSkills.keySet());
    }
    public void clearContainer(){
        skills.clear();
        skillOwners.clear();
        dirtySkills.clear();
        toRemoveSkills.clear();
    }
    @Override
    public SkillDataSource getDataSource() {
        return CoreDataSources.SKILL_DATA_SOURCE.get();
    }
}
