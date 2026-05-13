package net.thejadeproject.ascension.refactor_packages.bloodlines;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.UUID;

public interface IBloodline {
    IBloodlineData onBloodlineAdded(IEntityData heldEntity,ResourceLocation newBloodline);
    /*
        called when bloodline is replaced with a new one.
        if it is removed due to entity tether being broken it is called after all that removal logic
     */
    void onBloodlineAdded(IEntityData heldEntity,IBloodlineData bloodlineData,ResourceLocation newBloodline);

    /*
        onEntityTethered is called when an entities data is tethered to another existing entity.
        not called when,for example, a fresh tethered vessel is created. Since in that instance data is only being
        moved around. nothing new is being added or removed

        same with Untethered. called when there is some sort of data being added or removed rather than just moved around
     */
    void onEntityTethered(IEntityData heldEntity, IEntityData tetheredEntity,IBloodlineData bloodlineData);
    void onEntityUntethered(IEntityData heldEntity,IEntityData oldTetheredEntity,IBloodlineData bloodlineData);



    /*
        handles when forms are added and removed. is also called if an untethered entity held forms
        used when you want to apply data to specific forms
     */
    void onFormAdded(IEntityData heldEntity, ResourceLocation form,IBloodlineData bloodlineData);
    void onFormRemoved(IEntityData heldEntity,ResourceLocation form,IBloodlineData bloodlineData);


    IBloodlineData freshBloodlineData(IEntityData heldEntity);
    IBloodlineData fromCompound(CompoundTag tag, IEntityData heldEntity);
    IBloodlineData fromNetwork(RegistryFriendlyByteBuf buf);

    static IBloodlineData getFromCompound(IEntityData entityData, IBloodline bloodline, CompoundTag tag){
        try {
            return bloodline.fromCompound(tag,entityData);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error trying to load bloodline data data data for bloodline: "+
                    AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.getKey(bloodline),e);
            return bloodline.freshBloodlineData(entityData);
        }
    }

    Component getDisplayTitle();

    Component getShortDescription();

    Component getDescription();

    @OnlyIn(Dist.CLIENT)
    RenderableElement getInformationContainer(UIFrame frame);
}
