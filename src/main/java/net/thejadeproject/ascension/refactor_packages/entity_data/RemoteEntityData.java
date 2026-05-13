package net.thejadeproject.ascension.refactor_packages.entity_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.thejadeproject.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSource;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSourceContainer;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityForm;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.paths.PathBonusHandler;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.SkillCastHandler;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
//TODO implement properly
public class RemoteEntityData implements IEntityData{
    private Entity attachedEntity;
    private UUID watchedEntityData;

    private ResourceLocation activeForm = ModForms.MORTAL_VESSEL.getId();
    private AscensionAttributeHolder ascensionAttributeHolder;
    private SkillCastHandler skillCastHandler=new SkillCastHandler();
    private double currentHealth;

    public RemoteEntityData(Entity attachedEntity,UUID watchedEntityData){
        this.attachedEntity = attachedEntity;
        this.watchedEntityData = watchedEntityData;

        currentHealth = getAscensionAttributeHolder().getAttribute(Attributes.MAX_HEALTH).getValue();
    }
    public RemoteEntityData(Entity attachedEntity,CompoundTag tag){
        watchedEntityData = tag.getUUID("watched_entity_data");
        activeForm = ResourceLocation.parse(tag.getString("active_form"));
        this.attachedEntity = attachedEntity;
        getSkillCastHandler().read(tag.getCompound("skill_cast_handler"));
        if(attachedEntity instanceof LivingEntity entity){
            ascensionAttributeHolder = new AscensionAttributeHolder(entity);
            addDefaultAttributes(entity);

        }
        getAscensionAttributeHolder().updateAttributes(this);


        currentHealth = tag.getDouble("current_health");
    }
    @Override
    public void write(CompoundTag tag) {
        tag.putUUID("watched_entity_data",watchedEntityData);
        tag.putString("active_form",activeForm.toString());
        tag.put("skill_cast_handler",getSkillCastHandler().write());
        tag.putDouble("current_health",currentHealth);

    }
    public UUID getWatchedEntityData(){
        return watchedEntityData;
    }
    @Override
    public Entity getAttachedEntity() {
        return attachedEntity;
    }


    @Override
    public IEntityFormData getActiveFormData() {
        return getEntityFormData(activeForm);
    }

    @Override
    public IEntityFormData removeEntityForm(ResourceLocation form) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).removeEntityForm(form);
    }

    @Override
    public IEntityFormData getEntityFormData(ResourceLocation form) {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getEntityFormData(form);
    }

    @Override
    public IEntityFormData getEntityFormData(IEntityForm form) {
        return getEntityFormData(AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.getKey(form));
    }

    @Override
    public Collection<IEntityFormData> getFormData() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getFormData();
    }

    @Override
    public boolean hasForm(ResourceLocation form) {
        return false;
    }

    @Override
    public void addEntityForm(ResourceLocation form) {
        EntityDataManager.getEntityData(getWatchedEntityData()).addEntityForm(form);
    }

    @Override
    public void addEntityForm(ResourceLocation form, IEntityFormData formData) {
        EntityDataManager.getEntityData(getWatchedEntityData()).addEntityForm(form,formData);
    }

    @Override
    public void setActiveForm(ResourceLocation activeForm) {
        ResourceLocation oldForm = this.activeForm;
        this.activeForm = activeForm;
        AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(oldForm).leaveForm(this);
        AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(activeForm).enterForm(this,oldForm);

        //TODO call some kind of event that things can listen to?



    }

    @Override
    public void setFormData(ResourceLocation form, IEntityFormData formData) {
        EntityDataManager.getEntityData(getWatchedEntityData()).setFormData(form,formData);
    }

    @Override
    public boolean setPhysique(ResourceLocation physique) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).setPhysique(physique);
    }

    @Override
    public boolean setPhysique(ResourceLocation physique, IPhysiqueData existingData) {

        return EntityDataManager.getEntityData(getWatchedEntityData()).setPhysique(physique,existingData);
    }

    @Override
    public boolean setPhysique(ResourceLocation physique, IPhysiqueData existingData, ResourceLocation form) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).setPhysique(physique,existingData);
    }


    @Override
    public IPhysiqueData getPhysiqueData() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getPhysiqueData();
    }

    @Override
    public ResourceLocation getPhysiqueForm() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getPhysiqueForm();
    }

    @Override
    public IPhysiqueData removePhysique() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).removePhysique();
    }

    @Override
    public IPhysique getPhysique() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getPhysique();
    }

    @Override
    public void movePhysique(ResourceLocation form) {
        EntityDataManager.getEntityData(getWatchedEntityData()).movePhysique(form);
    }

    @Override
    public void setBloodline(ResourceLocation bloodline) {
        EntityDataManager.getEntityData(getWatchedEntityData()).setBloodline(bloodline);
    }

    @Override
    public void setBloodline(ResourceLocation bloodline, IBloodlineData existingData) {
        EntityDataManager.getEntityData(getWatchedEntityData()).setBloodline(bloodline,existingData);
    }


    @Override
    public IBloodlineData getBloodlineData() {
        return EntityDataManager.getEntityData(getWatchedEntityData()).getBloodlineData();
    }

    @Override
    public ResourceLocation getBloodlineForm() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getBloodlineForm();
    }

    @Override
    public IBloodlineData removeBloodline() {
        return EntityDataManager.getEntityData(getWatchedEntityData()).removeBloodline();
    }

    @Override
    public IBloodline getBloodline() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getBloodline();
    }

    @Override
    public void moveBloodline(ResourceLocation form) {
        EntityDataManager.getEntityData(getWatchedEntityData()).moveBloodline(form);
    }

    @Override
    public boolean hasPath(ResourceLocation path) {

        return EntityDataManager.getEntityData(getWatchedEntityData()).hasPath(path);
    }

    @Override
    public boolean isCultivating() {

        return EntityDataManager.getEntityData(getWatchedEntityData()).isCultivating();
    }

    @Override
    public boolean isCultivating(ResourceLocation path) {

        return EntityDataManager.getEntityData(getWatchedEntityData()).isCultivating(path);
    }

    @Override
    public ResourceLocation getTechnique(ResourceLocation path) {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getTechnique(path);
    }

    @Override
    public ITechniqueData getTechniqueData(ResourceLocation path) {

        return getTechniqueData(path);
    }

    @Override
    public PathData getPathData(ResourceLocation path) {

        return EntityDataManager.getEntityData(getWatchedEntityData()).getPathData(path);
    }

    @Override
    public Collection<ResourceLocation> getPathDataForms(ResourceLocation path) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).getPathDataForms(path);
    }

    @Override
    public Collection<PathData> getAllPathData() {
        return EntityDataManager.getEntityData(getWatchedEntityData()).getAllPathData();
    }

    @Override
    public ITechniqueData removeTechnique(ResourceLocation path) {
        return null;
    }

    @Override
    public void setPathForm(ResourceLocation path, ResourceLocation form) {

    }

    @Override
    public boolean setTechnique(ResourceLocation path) {
       return EntityDataManager.getEntityData(getWatchedEntityData()).setTechnique(path);
    }

    @Override
    public boolean setTechnique(ResourceLocation technique, ITechniqueData techniqueData) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).setTechnique(technique,techniqueData);
    }

    @Override
    public void addPathData(ResourceLocation path, PathData pathData, ResourceLocation form) {

    }

    @Override
    public void addPathData(ResourceLocation path, PathData pathData) {
        EntityDataManager.getEntityData(getWatchedEntityData()).addPathData(path,pathData);
    }

    @Override
    public void removePath(ResourceLocation path) {
        EntityDataManager.getEntityData(getWatchedEntityData()).removePath(path);
    }


    @Override
    public PathBonusHandler getPathBonusHandler() {
        return EntityDataManager.getEntityData(getWatchedEntityData()).getPathBonusHandler();
    }

    @Override
    public boolean isBreakingThrough(ResourceLocation path) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).isBreakingThrough(path);
    }

    @Override
    public void giveSkill(ResourceLocation skill, ResourceLocation form) {
        EntityDataManager.getEntityData(getWatchedEntityData()).giveSkill(skill,form);
    }

    @Override
    public void giveSkill(ResourceLocation skill, IPersistentSkillData skillData, ResourceLocation form) {
        EntityDataManager.getEntityData(getWatchedEntityData()).giveSkill(skill,skillData,form);
    }

    @Override
    public void removeSkill(ResourceLocation skill, ResourceLocation form) {
        EntityDataManager.getEntityData(getWatchedEntityData()).removeSkill(skill,form);
    }

    @Override
    public void removeSkill(ResourceLocation skill) {

    }

    @Override
    public boolean hasSkill(ResourceLocation skill) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).hasSkill(skill);
    }

    @Override
    public IPersistentSkillData getSkillData(ResourceLocation skill) {
        return EntityDataManager.getEntityData(getWatchedEntityData()).getSkillData(skill);
    }

    @Override
    public Set<ResourceLocation> getAllSkills() {
        return EntityDataManager.getEntityData(getWatchedEntityData()).getAllSkills();
    }

    @Override
    public SkillCastHandler getSkillCastHandler() {
        return skillCastHandler;
    }

    @Override
    public EntityQiContainer getQiContainer() {
        return EntityDataManager.getEntityData(getWatchedEntityData()).getQiContainer();
    }

    @Override
    public void addEntityDataSource(IEntityDataSourceContainer container) {

    }



    @Override
    public IEntityDataSourceContainer getSourceContainer(ResourceLocation identifier) {
        return null;
    }

    @Override
    public IEntityDataSourceContainer removeEntitySource(ResourceLocation identifier) {
        return null;
    }

    @Override
    public Collection<IEntityDataSourceContainer> getContainersOfType(IEntityDataSource source) {
        return List.of();
    }

    @Override
    public AscensionAttributeHolder getAscensionAttributeHolder() {
        return ascensionAttributeHolder;
    }

    @Override
    public void setAscensionAttributeHolder(LivingEntity entity, AscensionAttributeHolder holder) {
        this.ascensionAttributeHolder = holder;
        ascensionAttributeHolder.updateAttributes(this);
    }

    @Override
    public void setHealth(double newVal) {
        //TODO
    }

    @Override
    public void setHealth(double val, DamageSource source) {
        //TODO
    }

    @Override
    public double getHealth() {
        return currentHealth;
    }


}
