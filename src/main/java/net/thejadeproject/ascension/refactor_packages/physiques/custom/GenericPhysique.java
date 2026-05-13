package net.thejadeproject.ascension.refactor_packages.physiques.custom;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.server.level.ServerPlayer;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.IInformationContainer;
import net.thejadeproject.ascension.refactor_packages.paths.custom.GenericPath;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;

import java.util.*;

public class GenericPhysique implements IPhysique {

    private final Component title;
    private Component description;
    private Component shortDescription;

    private final HashSet<ResourceLocation> paths = new HashSet<>();
    private final HashMap<ResourceLocation,Double> pathBonuses = new HashMap<>();

    public GenericPhysique(Component title){
        this.title = title;



    }
    public GenericPhysique setDescription(Component description){
        this.description = description;
        return this;
    }
    public GenericPhysique setShortDescription(Component shortDescription){
        this.shortDescription =shortDescription;
        return this;
    }
    public GenericPhysique addPath(ResourceLocation path){
        paths.add(path);
        return this;
    }
    public GenericPhysique addPathBonus(ResourceLocation path, Double val){
        pathBonuses.put(path,val);
        return this;
    }




    @Override
    public void onPhysiqueAdded(IEntityData heldEntity, ResourceLocation oldPhysique,IPhysiqueData oldPhysiqueData) {
        //do not need to apply applied path bonuses, since this is handled by the entity data
        //System.out.println("player has been given physique");
    }

    @Override
    public void onPhysiqueRemoved(IEntityData heldEntity, IPhysiqueData physiqueData, ResourceLocation newPhysique) {
        //do not need to remove applied path bonuses, since this is handled by the entity data
    }

    @Override
    public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {

    }

    @Override
    public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {

    }

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame,getDisplayTitle(),getDescription());
    }

    @Override
    public Collection<ResourceLocation> paths() {
        return paths;
    }

    @Override
    public Map<ResourceLocation, Double> pathBonuses() {
        return pathBonuses;
    }

    @Override
    public IPhysiqueData freshPhysiqueData(IEntityData heldEntity) {
        return null;
    }

    @Override
    public IPhysiqueData fromCompound(CompoundTag tag, IEntityData heldEntity) {
        return null;
    }

    @Override
    public IPhysiqueData fromNetwork(RegistryFriendlyByteBuf buf) {
        return null;
    }

    protected void broadcastRareAcquired(IEntityData heldEntity, String translationKey) {
        if (heldEntity.isLoading()) return;
        if (!(heldEntity.getAttachedEntity() instanceof ServerPlayer player)) return;

        Component message = Component.translatable(
                translationKey,
                player.getDisplayName().copy().withStyle(ChatFormatting.WHITE),
                getDisplayTitle().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
        ).withStyle(ChatFormatting.GOLD);

        player.server.getPlayerList().broadcastSystemMessage(message, false);
    }


}
