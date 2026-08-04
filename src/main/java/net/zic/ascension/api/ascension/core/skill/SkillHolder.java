package net.zic.ascension.api.ascension.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SkillHolder implements DataSourceInstance {


    private final HashMap<Identifier, SkillData> skills = new HashMap<>();
    private final HashMap<Identifier, HashSet<Identifier>> skillOwners = new HashMap<>();

    private final HashSet<Identifier> dirtySkills = new HashSet<>();
    private final HashSet<Identifier> toRemoveSkills = new HashSet<>();

    private final HashMap<Identifier,SkillData> cachedSkills =  new HashMap<>();

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
    public Skill getSkill(Identifier skill,RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,access);
    }
    protected Map<Identifier,SkillData> getAllSkills(){
        return skills;
    }


    public void markSkillDirty(Identifier skill){
        dirtySkills.add(skill);
    }//should be used if you changed a skills skilLData


    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.SKILL_HOLDER_PROVIDER.get();
    }


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
    //──Data────────────────────────────────────────────────────────


    public void write(ValueOutput output, RegistryAccess access){

        ValueOutput.ValueOutputList skillOutputList = output.childrenList("skills");
        for(Identifier skill : getSkills()){
            AscensionCraft.LOGGER.debug("Saving Skill {}",skill);
            try {
                ValueOutput skillOutput = skillOutputList.addChild();

                NbtHelpers.writeIdentifier(skillOutput,"skill",skill);

                ValueOutput skillData = skillOutput.child("data");
                if(getSkillData(skill) != null) getSkillData(skill).write(skillData);
                else throw new Exception("no skill data for skill "+skillData);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing Skill {}",skill);
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
    }
    public void read(ValueInput input, RegistryAccess access){

        clearCache();
        ValueInput.ValueInputList skillsInput = input.childrenListOrEmpty("skills");
        for(ValueInput skillInput : skillsInput){
            try {
                Identifier skillId = NbtHelpers.readIdentifier(skillInput,"skill");
                AscensionCraft.LOGGER.debug("Reading Skill {}",skillId);
                ValueInput rawSkillData = skillInput.childOrEmpty("data");

                Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skillId,access);
                if(skill == null) continue;

                SkillData data = skill.loadData(rawSkillData,access);
                addCachedSkill(skillId,data);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error loading skill");
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }
        }
    }

    public void encode(ByteBuf buf, RegistryAccess access,boolean fullPatch){

        buf.writeBoolean(fullPatch);
        if(fullPatch) encodeFullPatch(buf,access);
        else encodePartialPatch(buf,access);

        dirtySkills.clear();
        toRemoveSkills.clear();
    }
    protected void encodeFullPatch(ByteBuf buf, RegistryAccess access){
        buf.writeInt(skills.size());
        for(Identifier skill : skills.keySet()){
            ByteBufHelpers.encodeIdentifier(skill,buf);
            getSkillData(skill).encode(buf);
        }
    }
    protected void encodePartialPatch(ByteBuf buf, RegistryAccess access){
        buf.writeInt(dirtySkills.size());
        for(Identifier dirtySkill : dirtySkills){
            ByteBufHelpers.encodeIdentifier(dirtySkill,buf);
            getSkillData(dirtySkill).encode(buf);
        }
        ByteBufHelpers.encodeCollection(toRemoveSkills,buf,ByteBufHelpers::encodeIdentifier);
    }
    public void decode(ByteBuf buf,RegistryAccess access){

        if(buf.readBoolean()) decodeFullPatch(buf,access);
        else decodePartialPatch(buf,access);

        dirtySkills.clear();
        toRemoveSkills.clear();
    }
    private void decodeFullPatch(ByteBuf buf,RegistryAccess access){
        skills.clear();
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            Identifier skillId = ByteBufHelpers.decodeIdentifier(buf);
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skillId,access);
            SkillData data = skill.loadData(buf);
            skills.put(skillId,data);
        }
    }

    protected void decodePartialPatch(ByteBuf buf,RegistryAccess access){
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            Identifier skillId = ByteBufHelpers.decodeIdentifier(buf);
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skillId,access);
            SkillData data = skill.loadData(buf);
            skills.put(skillId,data);
        }
        ByteBufHelpers.decodeArray(buf,ByteBufHelpers::decodeIdentifier).forEach(skills::remove);


    }
}
