package net.thejadeproject.ascension.refactor_packages.physiques;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.IInformationContainer;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface IPhysique {


    void onPhysiqueAdded(IEntityData heldEntity,ResourceLocation oldPhysique,IPhysiqueData oldPhysiqueData);
    /*
        called when physique is replaced with a new one.
        if it is removed due to entity tether being broken it is called after all that removal logic
     */
    void onPhysiqueRemoved(IEntityData heldEntity,IPhysiqueData physiqueData,ResourceLocation newPhysique);




    /*
        handles when forms are added and removed.

        removed is only called if the physique is still on the entity aka not on the removed entity
     */
    void onFormAdded(IEntityData heldEntity, ResourceLocation form,IPhysiqueData physiqueData);
    void onFormRemoved(IEntityData heldEntity,ResourceLocation form,IPhysiqueData physiqueData);


    Component getDisplayTitle();
    Component getShortDescription();
    Component getDescription();

    @OnlyIn(Dist.CLIENT)
    //a bit hacky
    RenderableElement getInformationContainer(UIFrame frame);
    //the paths that this physique "unlocks"
    //without unlocking a path, even with a technique they cannot use it
    //although some paths let you unlock them by learning a technique. (but not all!!)
    Collection<ResourceLocation> paths();

    //what bonus does it give to each path (bonus is replacing efficiency)
    Map<ResourceLocation,Double> pathBonuses();


    IPhysiqueData freshPhysiqueData(IEntityData heldEntity);
    IPhysiqueData fromCompound(CompoundTag tag,IEntityData heldEntity);
    IPhysiqueData fromNetwork(RegistryFriendlyByteBuf buf);

    static IPhysiqueData getFromCompound(IEntityData entityData, IPhysique physique, CompoundTag tag){
        try {
            return physique.fromCompound(tag,entityData);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error trying to load physique data data data for physique: "+
                    AscensionRegistries.Physiques.PHSIQUES_REGISTRY.getKey(physique),e);
            return physique.freshPhysiqueData(entityData);
        }
    }
}
