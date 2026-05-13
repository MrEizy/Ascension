package net.thejadeproject.ascension.refactor_packages.forms.forms.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityForm;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.SyncHeldSkills;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.HeldSkills;

import java.nio.charset.Charset;

public class GenericForm implements IEntityForm {

    private final Component title;

    public GenericForm(Component title){
        this.title=title;
    }


    @Override
    public void enterForm(IEntityData heldEntity, ResourceLocation previousForm) {

    }

    @Override
    public void leaveForm(IEntityData heldEntity) {

    }

    @Override
    public void onAdded(IEntityData heldEntity) {

    }

    @Override
    public void onRemoved(IEntityData heldEntity, IEntityFormData formData) {

    }

    @Override
    public void onFormAdded(IEntityData heldEntity, IEntityFormData formData, IEntityFormData addedFormData) {
        //wrong form data type
        if(!(formData instanceof GenericFormData genericFormData)) return;

        //handle cultivation
        genericFormData.getAllPathData().forEach(pathData -> pathData.onFormAdded(heldEntity,addedFormData));

        //handle physique and bloodline
        if(genericFormData.getBloodlineData() != null){
            genericFormData.getBloodline().onFormAdded(heldEntity,addedFormData.getEntityFormId(), genericFormData.getBloodlineData());
        }
        if(genericFormData.getPhysiqueData() != null){
            genericFormData.getPhysique().onFormAdded(heldEntity,addedFormData.getEntityFormId(),genericFormData.getPhysiqueData());
        }

        //TODO go through skills


    }

    @Override
    public void onFormRemoved(IEntityData heldEntity, IEntityFormData formData, IEntityFormData removedFormData) {
        //wrong form data type
        if(!(formData instanceof GenericFormData genericFormData)) return;

        //handle cultivation
        genericFormData.getAllPathData().forEach(pathData -> pathData.onFormRemoved(heldEntity,removedFormData));

        //handle physique and bloodline
        if(genericFormData.getBloodlineData() != null){
            genericFormData.getBloodline().onFormRemoved(heldEntity,removedFormData.getEntityFormId(), genericFormData.getBloodlineData());
        }
        if(genericFormData.getPhysiqueData() != null){
            genericFormData.getPhysique().onFormRemoved(heldEntity,removedFormData.getEntityFormId(),genericFormData.getPhysiqueData());
        }

        //TODO go through skills


    }

    @Override
    public IEntityFormData freshEntityFormData(IEntityData heldEntity) {
        return new GenericFormData(AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.getKey(this));
    }

    @Override
    public IEntityFormData fromCompound(CompoundTag tag, IEntityData heldEntity) {


        return new GenericFormData(AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.getKey(this));
    }

    @Override
    public IEntityFormData fromNetwork(RegistryFriendlyByteBuf buf) {
        //TODO properly decode it
        GenericFormData formData = new GenericFormData(AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.getKey(this));
        formData.setHeldSkills(HeldSkills.decodeFull(buf));
        if(buf.readBoolean()){
            ResourceLocation physique = ResourceLocation.parse((String) buf.readCharSequence(buf.readInt(),Charset.defaultCharset()));
            IPhysiqueData data = null;
            if(buf.readBoolean()){
                data = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(physique).fromNetwork(buf);
            }
            formData.setPhysique(physique,data);
        }
        if(buf.readBoolean()){
            ResourceLocation bloodline = ResourceLocation.parse((String) buf.readCharSequence(buf.readInt(),Charset.defaultCharset()));
            IBloodlineData data = null;
            if(buf.readBoolean()){
                data = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.get(bloodline).fromNetwork(buf);
            }
            formData.setBloodline(bloodline,data);
        }
        int paths = buf.readInt();
        //System.out.println("reading " + paths + " paths" );
        for(int i =0;i<paths;i++){
            ResourceLocation path = ResourceLocation.parse((String) buf.readCharSequence(buf.readInt(),Charset.defaultCharset()));
            PathData pathData = AscensionRegistries.Paths.PATHS_REGISTRY.get(path).fromNetwork(buf);
            formData.addPathData(path,pathData);
        }
        return formData;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, IEntityFormData formData) {
        HeldSkills.encodeFull(buf,formData.getHeldSkills());
        buf.writeBoolean(formData.getPhysiqueKey() != null);
        if(formData.getPhysiqueKey() != null){
            buf.writeInt(formData.getPhysiqueKey().toString().length());
            buf.writeCharSequence(formData.getPhysiqueKey().toString(), Charset.defaultCharset());
            buf.writeBoolean(formData.getPhysiqueData() != null);
            if(formData.getPhysiqueData() != null) formData.getPhysiqueData().encode(buf);
        }

        buf.writeBoolean(formData.getBloodlineKey() != null);
        if(formData.getBloodlineKey() != null){
            buf.writeInt(formData.getBloodlineKey().toString().length());
            buf.writeCharSequence(formData.getBloodlineKey().toString(), Charset.defaultCharset());
            buf.writeBoolean(formData.getBloodlineData() != null);
            if(formData.getBloodlineData() != null) formData.getBloodlineData().encode(buf);
        }

        buf.writeInt(formData.getAllPathData().size());
        for(ResourceLocation path : formData.getPaths()){
            buf.writeInt(path.toString().length());
            buf.writeCharSequence(path.toString(),Charset.defaultCharset());
            formData.getPathData(path).encode(buf);
        }

    }
}
