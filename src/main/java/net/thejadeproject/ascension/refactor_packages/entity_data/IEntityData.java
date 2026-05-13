package net.thejadeproject.ascension.refactor_packages.entity_data;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.loading.FMLEnvironment;
import net.thejadeproject.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSource;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSourceContainer;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityForm;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.paths.PathBonusHandler;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;
import net.thejadeproject.ascension.refactor_packages.skill_casting.SkillCastHandler;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.stats.custom.ModStats;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.util.ModAttributes;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

/**
 * TODO set up a HeldEntityData,RemoteAccessEntityData and RemoteEntityData
 *
 *
 *
 * Stores all the data an entity would need that relates to cultivation and stats
 *
 * does not hold a direct reference to the entity holding it, only the id. this is because there may be scenarios where this data is not stored on the entity directly
 * or the entity is not loaded but data is. for this reason you either access the entity through EntityDataManger OR you get the entity reference from the method
 * being used
 */
public interface IEntityData {
    Entity getAttachedEntity();

    //========================== FORM DATA HANDLING ==========================

    IEntityFormData getActiveFormData();
    //calls all on removed stuff,so should not be used for tethered entities
    IEntityFormData removeEntityForm(ResourceLocation form);
    IEntityFormData getEntityFormData(ResourceLocation form);
    IEntityFormData getEntityFormData(IEntityForm form);

    Collection<IEntityFormData> getFormData();

    boolean hasForm(ResourceLocation form);

    void addEntityForm(ResourceLocation form);
    void addEntityForm(ResourceLocation form, IEntityFormData formData);

    void setActiveForm(ResourceLocation activeForm);

    //should only be used during sync
    void setFormData(ResourceLocation form,IEntityFormData formData);


    //============================ PHYSIQUE HANDLING =======================================
    boolean setPhysique(ResourceLocation physique);
    //calls a separate method so the physique can update things properly
    boolean setPhysique(ResourceLocation physique,IPhysiqueData existingData);
    boolean setPhysique(ResourceLocation physique, IPhysiqueData existingData,ResourceLocation form);
    IPhysiqueData getPhysiqueData();
    ResourceLocation getPhysiqueForm();
    IPhysiqueData removePhysique();
    IPhysique getPhysique();
    void movePhysique(ResourceLocation form);
    //============================ BLOODLINE HANDLING =======================================
    void setBloodline(ResourceLocation bloodline);
    void setBloodline(ResourceLocation bloodline, IBloodlineData existingData);
    IBloodlineData getBloodlineData();
    ResourceLocation getBloodlineForm();
    IBloodlineData removeBloodline();
    IBloodline getBloodline();
    void moveBloodline(ResourceLocation form);
    //============================ CULTIVATION DATA HANDLING ==================================

    boolean hasPath(ResourceLocation path);
    //only checks held forms, not tethered
    boolean isCultivating();
    //only checks held forms, not tethered
    boolean isCultivating(ResourceLocation path);
    ResourceLocation getTechnique(ResourceLocation path);
    ITechniqueData getTechniqueData(ResourceLocation path);
    PathData getPathData(ResourceLocation path);
    Collection<ResourceLocation> getPathDataForms(ResourceLocation path);
    Collection<PathData> getAllPathData();
    ITechniqueData removeTechnique(ResourceLocation path);
    void setPathForm(ResourceLocation path,ResourceLocation form);
    //techniques will check if the last used technique is compatible, if not will remove all cultivation
    //give some sort of warning beforehand?
    boolean setTechnique(ResourceLocation technique);
    boolean setTechnique(ResourceLocation technique,ITechniqueData techniqueData);
    void addPathData(ResourceLocation path,PathData pathData, ResourceLocation form);
    void addPathData(ResourceLocation path,PathData pathData);
    //a shortcut for removing cultivation, anything more complex must be done through the path data and path
    void removePath(ResourceLocation path);

    PathBonusHandler getPathBonusHandler();
    //============================ BREAKTHROUGH HANDLING ===================================
    boolean isBreakingThrough(ResourceLocation path);
    //============================ SKILL HANDLING ===================================
    void giveSkill(ResourceLocation skill,ResourceLocation form);
    void giveSkill(ResourceLocation skill, IPersistentSkillData skillData, ResourceLocation form);

    //only removes from that specific form.
    void removeSkill(ResourceLocation skill,ResourceLocation form);
    void removeSkill(ResourceLocation skill); // removes from all forms
    boolean hasSkill(ResourceLocation skill);
    IPersistentSkillData getSkillData(ResourceLocation skill);

    Set<ResourceLocation> getAllSkills();

    //============================= SKILL CASTING ====================================
    SkillCastHandler getSkillCastHandler();
    EntityQiContainer getQiContainer();
    default void tick() {}
    //============================= ENTITY DATA SOURCES ===============================
    //TODO need to update to include form stuff as well
    void addEntityDataSource(IEntityDataSourceContainer container);
    IEntityDataSourceContainer getSourceContainer(ResourceLocation identifier);
    IEntityDataSourceContainer removeEntitySource(ResourceLocation identifier);
    Collection<IEntityDataSourceContainer> getContainersOfType(IEntityDataSource source);
    //============================= ATTRIBUTES =======================================
    AscensionAttributeHolder getAscensionAttributeHolder();
    void setAscensionAttributeHolder(LivingEntity entity,AscensionAttributeHolder holder);
    default void addDefaultAttributes(LivingEntity entity){
        AscensionAttributeHolder holder = getAscensionAttributeHolder();
        //TODO set up some null handling
        holder.addAttribute(Attributes.MAX_HEALTH, Component.literal("Max Health"));
        holder.getAttribute(Attributes.MAX_HEALTH).addStatScaling(ModStats.VITALITY.get(),2); //200% of vitality


        holder.addAttribute(Attributes.ATTACK_DAMAGE,Component.literal("Attack Damage"));
        holder.getAttribute(Attributes.ATTACK_DAMAGE).addStatScaling(ModStats.STRENGTH.get(),1); //100% of strength

        holder.addAttribute(Attributes.JUMP_STRENGTH,Component.literal("Jump Strength"));
        holder.getAttribute(Attributes.JUMP_STRENGTH).addStatScaling(ModStats.STRENGTH.get(),0.005); //0.5% of strength

        //due to how speed scales in mc these need to be low
        //this will also be further suppressed while in combat
        holder.addAttribute(Attributes.MOVEMENT_SPEED,Component.literal("Movement Speed"));
        holder.getAttribute(Attributes.MOVEMENT_SPEED).addStatScaling(ModStats.STRENGTH.get(),0.00005); //0.005% of strength
        holder.getAttribute(Attributes.MOVEMENT_SPEED).addStatScaling(ModStats.AGILITY.get(),0.002); //0.05% of agility
//        holder.addAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY,Component.literal("Swim Speed"));
//        holder.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY).addStatScaling(ModStats.STRENGTH.get(),0.0001); //0.01% of strength
//        holder.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY).addStatScaling(ModStats.AGILITY.get(),0.5); //50% of agility

        holder.addAttribute(ModAttributes.MAX_QI,Component.literal("Max Qi"));
        holder.getAttribute(ModAttributes.MAX_QI).addStatScaling(ModStats.INTELLIGENCE.get(),10);
        holder.addAttribute(ModAttributes.QI_REGEN_RATE,Component.literal("Qi Regen Rate"));
        holder.getAttribute(ModAttributes.QI_REGEN_RATE).addStatScaling(ModStats.INTELLIGENCE.get(),0.01);
        holder.updateAttributes(this);

        holder.addAttribute(Attributes.STEP_HEIGHT, Component.literal("Step Height"));
        holder.getAttribute(Attributes.STEP_HEIGHT).addStatScaling(ModStats.AGILITY.get(),0.05);

        holder.addAttribute(Attributes.ARMOR_TOUGHNESS,Component.literal("Armor Toughness"));

        holder.addAttribute(Attributes.ARMOR,Component.literal("Armor"));


        holder.addAttribute(Attributes.ATTACK_SPEED, Component.literal("Attack Speed"));
        holder.getAttribute(Attributes.ATTACK_SPEED).addStatScaling(ModStats.STRENGTH.get(),0.001);
        holder.getAttribute(Attributes.ATTACK_SPEED).addStatScaling(ModStats.AGILITY.get(),0.001);

        holder.addAttribute(Attributes.LUCK,Component.literal("Luck"));


        holder.addAttribute(Attributes.MINING_EFFICIENCY,Component.literal("Mining Efficiency"));
        holder.getAttribute(Attributes.MINING_EFFICIENCY).addStatScaling(ModStats.STRENGTH.get(),0.001);

        holder.addAttribute(Attributes.SAFE_FALL_DISTANCE,Component.literal("Safe Fall Distance"));
        holder.getAttribute(Attributes.SAFE_FALL_DISTANCE).addStatScaling(ModStats.STRENGTH.get(),0.1);
        //if(entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
    }
    default AttributeValueContainer getAttribute(Holder<Attribute> attributeHolder){
        return getAscensionAttributeHolder().getAttribute(attributeHolder);
    }
    default double getAttributeBaseValue(Holder<Attribute> attributeHolder){
        return getAttribute(attributeHolder) == null? 0: getAttribute(attributeHolder).getBaseValue();
    }
    default double getAttributeValue(Holder<Attribute> attributeHolder){
        return getAttribute(attributeHolder) == null? 0: getAttribute(attributeHolder).getValue();
    }
    void setHealth(double newVal);
    void setHealth(double val, DamageSource source);
    double getHealth();
    //============================= DATA HANDLING ====================================
    void write(CompoundTag tag);

    //============================= EXTRA =========================================
    default boolean isClientSide(){
        return FMLEnvironment.dist.isClient();
    }

    default boolean isLoading() { return false; }



}
