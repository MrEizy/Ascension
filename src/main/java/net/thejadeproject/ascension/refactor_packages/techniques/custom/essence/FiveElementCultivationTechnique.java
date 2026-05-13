package net.thejadeproject.ascension.refactor_packages.techniques.custom.essence;

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
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.PathDataDisplayElement;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;
import net.thejadeproject.ascension.refactor_packages.techniques.stability.IStabilityHandler;
import net.thejadeproject.ascension.refactor_packages.techniques.stability.LnStabilityHandler;

import java.util.List;

public class FiveElementCultivationTechnique implements ITechnique {
    private final List<ResourceLocation> elements = List.of(
            ModPaths.FIRE.getId(),
            ModPaths.EARTH.getId(),
            ModPaths.METAL.getId(),
            ModPaths.WATER.getId(),
            ModPaths.WOOD.getId()
    );
    private final BasicStatChangeHandler statChangeHandler;
    private final IStabilityHandler stabilityHandler = new LnStabilityHandler();

    public FiveElementCultivationTechnique(BasicStatChangeHandler statChangeHandler) {
        this.statChangeHandler = statChangeHandler;
    }

    @Override
    public Component getDisplayTitle() {
        return Component.translatable("ascension.technique.five_element");
    }

    @Override
    public Component getShortDescription() {
        return Component.empty();
    }

    @Override
    public Component getDescription() {
        return Component.empty();
    }

    @Override
    public ResourceLocation getPath() {
        return ModPaths.ESSENCE.getId();
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        heldEntity.giveSkill(
                ModSkills.FIVE_ELEMENT_CIRCULATION.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );
        heldEntity.giveSkill(
                ModSkills.FIRE_SPRAY.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );
        elements.forEach(element->heldEntity.getPathBonusHandler().addPathBonus(element,1));
        heldEntity.getPathBonusHandler().addPathBonus(elements.getFirst(),2); //first element is first realm

    }

    @Override
    public void onTechniqueRemoved(IEntityData heldEntity, ITechniqueData techniqueData) {
        heldEntity.removeSkill(ModSkills.FIVE_ELEMENT_CIRCULATION.getId(),ModForms.MORTAL_VESSEL.getId());
        heldEntity.removeSkill(ModSkills.FIRE_SPRAY.getId(),ModForms.MORTAL_VESSEL.getId());
        elements.forEach(element->heldEntity.getPathBonusHandler().removePathBonus(element,1));
        heldEntity.getPathBonusHandler().removePathBonus(elements.getFirst(),2); //first element is first realm
    }
    public void applyElementalBonus(IEntityData entityData,int majorRealm){
        if(majorRealm == 0) return;
        if(majorRealm<elements.size()) entityData.getPathBonusHandler().addPathBonus(elements.get(majorRealm),0.1);
    }
    public void removeElementalBonus(IEntityData entityData,int majorRealm){
        if(majorRealm == 0) return;
        if(majorRealm<elements.size()) entityData.getPathBonusHandler().removePathBonus(elements.get(majorRealm),0.1);
    }

    @Override
    public void onRealmChange(IEntityData entityData, int oldMajorRealm, int oldMinorRealm, int newMajorRealm, int newMinorRealm) {
        statChangeHandler.applyChanges(entityData,this,oldMajorRealm,oldMinorRealm,newMajorRealm,newMinorRealm);
        if(oldMajorRealm<newMajorRealm){
            for(int i = oldMajorRealm+1;i<=newMajorRealm;i++){
                if(i < elements.size()){
                    entityData.getPathBonusHandler().addPathBonus(elements.get(i),2);
                }
                if(i-1 < elements.size() && i-1 >=0){
                    entityData.addPathData(elements.get(i-1), AscensionRegistries.Paths.PATHS_REGISTRY.get(elements.get(i-1)).freshPathData(entityData));
                }
            }
        }
        if(oldMajorRealm>newMajorRealm){
            for(int i = newMajorRealm+1;i<=oldMajorRealm;i++){
                if(i < elements.size()){
                    entityData.getPathBonusHandler().removePathBonus(elements.get(i),2);
                }
            }
        }
        if(newMajorRealm != oldMajorRealm) {
            if(newMajorRealm > oldMajorRealm){
                for(int i = oldMinorRealm+1; i <= getMaxMinorRealm(oldMajorRealm); i++){
                    applyElementalBonus(entityData, oldMajorRealm);
                }
                for(int i = oldMajorRealm+1; i < newMajorRealm; i++){
                    int minorRealmsForRealm = getMaxMinorRealm(i);
                    for(int j = 0; j <= minorRealmsForRealm; j++){
                        applyElementalBonus(entityData, i);
                    }
                }
                for(int i = 0; i <= newMinorRealm; i++){
                    applyElementalBonus(entityData, newMajorRealm);
                }
            }else{
                for(int i = oldMinorRealm; i >= 0; i--){
                    removeElementalBonus(entityData, oldMajorRealm);
                }
                for(int i = oldMajorRealm-1; i > newMajorRealm; i--){
                    int minorRealmsForRealm = getMaxMinorRealm(i);
                    for(int j = minorRealmsForRealm; j >= 0; j--){
                        removeElementalBonus(entityData, i);
                    }
                }
                int minorRealms = getMaxMinorRealm(newMajorRealm);
                for(int i = minorRealms; i > newMinorRealm; i--){
                    removeElementalBonus(entityData, newMajorRealm);
                }
            }
        }else if(newMinorRealm > oldMinorRealm){
            for(int i = oldMinorRealm+1; i <= newMinorRealm; i++){
                applyElementalBonus(entityData, newMajorRealm);
            }
        }else if(newMinorRealm < oldMinorRealm){
            for(int i = oldMinorRealm; i > newMinorRealm; i--){
                removeElementalBonus(entityData, newMajorRealm);
            }
        }
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

    @Override
    public void onFormRemoved(IEntityData heldEntity, IEntityFormData removedForm, PathData pathData) {

    }

    @Override
    public void onFormAdded(IEntityData heldEntity, IEntityFormData addedForm, PathData pathData) {

    }

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame, PathData pathData) {
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
        return null;
    }

    @Override
    public IBreakthroughInstance breakthroughInstanceFromCompound(CompoundTag tag, int majorRealm, int minorRealm, ITechniqueData techniqueData) {
        return null;
    }

    @Override
    public IBreakthroughInstance breakthroughInstanceFromNetwork(RegistryFriendlyByteBuf buf, int majorRealm, int minorRealm, ITechniqueData techniqueData) {
        return null;
    }
}
