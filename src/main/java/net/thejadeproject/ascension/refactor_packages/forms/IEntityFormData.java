package net.thejadeproject.ascension.refactor_packages.forms;

import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;

import net.thejadeproject.ascension.refactor_packages.skills.HeldSkills;
import net.thejadeproject.ascension.refactor_packages.stats.StatSheet;
import net.thejadeproject.ascension.refactor_packages.util.IDataInstance;

import java.util.Collection;
import java.util.UUID;

public interface IEntityFormData extends IDataInstance {

    UUID getAttachedEntity();
    void setAttachedEntity(UUID entity);
    ResourceLocation getEntityFormId();
    IEntityForm getEntityForm();




    void addPathData(ResourceLocation path,PathData pathData);
    PathData getPathData(ResourceLocation path);
    void removePathData(ResourceLocation path);
    Collection<PathData> getAllPathData();
    Collection<ResourceLocation> getPaths();
    boolean hasPathData(ResourceLocation path);

    HeldSkills getHeldSkills();
    void setHeldSkills(HeldSkills heldSkills); //mainly for syncing purposes

    // i want to be able to sync stuff separate for more efficient network traffic
    ResourceLocation getPhysiqueKey();
    IPhysique getPhysique();
    IPhysiqueData getPhysiqueData();
    void setPhysique(ResourceLocation physique);
    void setPhysique(ResourceLocation physique,IPhysiqueData physiqueData);

    ResourceLocation getBloodlineKey();
    IBloodline getBloodline();
    IBloodlineData getBloodlineData();
    void setBloodline(ResourceLocation bloodline);
    void setBloodline(ResourceLocation bloodline,IBloodlineData bloodlineData);



    StatSheet getStatSheet();



}
