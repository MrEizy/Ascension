package net.thejadeproject.ascension.refactor_packages.attributes;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class AscensionAttributeHolder {

    private final HashMap<Holder<Attribute>,AttributeValueContainer> attributes = new HashMap<>();

    private LivingEntity attachedEntity;
    public AscensionAttributeHolder(LivingEntity attachedEntity){this.attachedEntity = attachedEntity;}

    public void setAttachedEntity(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
        for(AttributeValueContainer  container : attributes.values()) container.setAttachedEntity(attachedEntity);
    }

    public void addAttribute(Holder<Attribute> attributeHolder, Component displayName){
        attributes.put(attributeHolder,new AttributeValueContainer(attachedEntity,attributeHolder,displayName));
    }

    public void removeAttribute(Holder<Attribute> attributeHolder){
        attributes.remove(attributeHolder);
    }

    public AttributeValueContainer getAttribute(Holder<Attribute> attributeHolder){
        return attributes.get(attributeHolder);
    }

    public void updateAttributes(IEntityData entityData){
        for(AttributeValueContainer container:attributes.values()) container.updateValue(entityData);

    }
    public Collection<AttributeValueContainer> getContainers(){
        return List.copyOf(attributes.values());
    }
    public void log(){
//        //System.out.println("Ascension Attributes: ");
        for(AttributeValueContainer instance : attributes.values()){
            instance.log();
        }
    }
    public static void encode(RegistryFriendlyByteBuf buf,AscensionAttributeHolder holder){
        buf.writeInt(holder.attributes.size());
        for(AttributeValueContainer container : holder.attributes.values()){
            AttributeValueContainer.encode(buf,container);
        }
    }
    public static AscensionAttributeHolder decode(RegistryFriendlyByteBuf buf){
        AscensionAttributeHolder holder = new AscensionAttributeHolder(null);
        int numberOfAttributes = buf.readInt();
        for(int i =0;i<numberOfAttributes;i++){
            AttributeValueContainer container = AttributeValueContainer.decode(buf);
            holder.attributes.put(container.getAttributeHolder(),container);
        }
        return holder;
    }
}
