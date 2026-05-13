package net.thejadeproject.ascension.refactor_packages.techniques.custom;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.NineHeavenlyTribulations;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.PathDataDisplayElement;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.skill_data.GenericCultivationSkillData;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueSkillHelper;
import net.thejadeproject.ascension.refactor_packages.techniques.stability.IStabilityHandler;
import net.thejadeproject.ascension.refactor_packages.techniques.stability.LnStabilityHandler;

import java.util.Set;

public class GenericTechnique implements ITechnique {
    private ResourceLocation path;
    private Component title;
    private Component shortDescription;
    private Component description;
    private double baseRate;
    private Set<ResourceLocation> secondaryPaths;
    private final IStabilityHandler stabilityHandler = new LnStabilityHandler();
    private  BasicStatChangeHandler statChangeHandler = new BasicStatChangeHandler();
    public GenericTechnique(ResourceLocation path,Component title,double baseRate,Set<ResourceLocation> secondaryPaths){
        this.path = path;
        this.title = title;
        this.baseRate = baseRate;
        this.secondaryPaths = secondaryPaths;
    }

    public GenericTechnique setStatChangeHandler(BasicStatChangeHandler statChangeHandler){
        this.statChangeHandler = statChangeHandler;
        return this;
    }

    public double getBaseRate() { return baseRate; }

    @Override
    public Component getDisplayTitle() {
        return title;
    }

    @Override
    public Component getShortDescription() {
        return shortDescription;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public ResourceLocation getPath() {
        return path;
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        if(getPath().equals(ModPaths.ESSENCE.getId())){
            heldEntity.giveSkill(ModSkills.BASIC_CULTIVATION_SKILL.getId(),new GenericCultivationSkillData(baseRate, secondaryPaths), ModForms.MORTAL_VESSEL.getId());
        }

        refreshUniversalTechniqueSkills(heldEntity);
    }

    @Override
    public void onTechniqueRemoved(IEntityData heldEntity, ITechniqueData techniqueData) {
        heldEntity.getPathData(getPath()).handleRealmChange(heldEntity.getPathData(getPath()).getMajorRealm(),0,heldEntity);
        if(getPath().equals(ModPaths.ESSENCE.getId())){
            heldEntity.removeSkill(ModSkills.BASIC_CULTIVATION_SKILL.getId(), ModForms.MORTAL_VESSEL.getId());
        }

        refreshUniversalTechniqueSkills(heldEntity);
    }

    @Override
    public void onRealmChange(IEntityData entityData, int oldMajorRealm, int oldMinorRealm, int newMajorRealm, int newMinorRealm) {
        //System.out.println("technique: "+AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.getKey(this).toString());
        //System.out.println("realm: ("+oldMajorRealm+","+oldMinorRealm+") -> ("+newMajorRealm+","+newMinorRealm+")");
        statChangeHandler.applyChanges(entityData,this,oldMajorRealm,oldMinorRealm,newMajorRealm,newMinorRealm);

        TechniqueSkillHelper.refreshUniversal(entityData, newMajorRealm);

        entityData.getActiveFormData().getStatSheet().log();
        entityData.getAscensionAttributeHolder().log();

        if(entityData.isLoading()) return;
        if(entityData.getAttachedEntity().level().isClientSide()) return;
        if(!(entityData.getAttachedEntity() instanceof  ServerPlayer serverPlayer)) return;
        if(serverPlayer.connection == null) return;
        //System.out.println("sending out sync packets");
        PacketDistributor.sendToPlayer(serverPlayer,new SyncAttributeHolder(entityData.getAscensionAttributeHolder()));
        for (IEntityFormData formData : entityData.getFormData()){
            formData.getStatSheet().sync(serverPlayer,formData.getEntityFormId());
        }
    }

    protected void refreshUniversalTechniqueSkills(IEntityData entityData) {
        PathData pathData = entityData.getPathData(getPath());

        TechniqueSkillHelper.refreshUniversal(
                entityData,
                pathData == null ? 0 : pathData.getMajorRealm()
        );
    }

    protected void clearUniversalTechniqueSkills(IEntityData entityData) {
        TechniqueSkillHelper.refreshUniversal(entityData, -1);
    }

    @Override
    public void onFormRemoved(IEntityData heldEntity, IEntityFormData removedForm, PathData pathData) {

    }

    @Override
    public void onFormAdded(IEntityData heldEntity, IEntityFormData addedForm, PathData pathData) {

    }

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        return AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique) instanceof GenericTechnique;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame,PathData pathData) {
        return new PathDataDisplayElement(frame,
                getMajorRealmName(pathData.getMajorRealm()),
                getMinorRealmName(pathData.getMajorRealm(),pathData.getMinorRealm()),
                getDescription());
    }

    @Override
    public IStabilityHandler getStabilityHandler() {
        return stabilityHandler;
    }

    @Override
    public ITechniqueData freshTechniqueData(IEntityData heldEntity) {
        return null;
    }

    @Override
    public ITechniqueData fromCompound(CompoundTag tag) {
        return null;
    }

    @Override
    public ITechniqueData fromNetwork(RegistryFriendlyByteBuf buf) {
        return null;
    }

    @Override
    public IBreakthroughInstance freshBreakthroughData(IEntityData heldEntity) {
        return new NineHeavenlyTribulations(1);
    }

    @Override
    public IBreakthroughInstance breakthroughInstanceFromCompound(
            CompoundTag tag, int majorRealm, int minorRealm, ITechniqueData data) {
        return new NineHeavenlyTribulations(1);
    }

    @Override
    public IBreakthroughInstance breakthroughInstanceFromNetwork(
            RegistryFriendlyByteBuf buf, int majorRealm, int minorRealm, ITechniqueData data) {
        return new NineHeavenlyTribulations(1);
    }

}
