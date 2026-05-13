package net.thejadeproject.ascension.refactor_packages.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.stats.Stat;
import net.thejadeproject.ascension.refactor_packages.stats.StatInstance;
import net.thejadeproject.ascension.refactor_packages.stats.StatSheet;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainer;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

import java.util.HashMap;

/**
 * need to be careful when handling remote entity data. this should only be held on Loaded entities
 * so when i store the entity data remotely ensure i remove this and add it to the remote instance
 *<br>
 * the remote entity data should listen to stat changed event and then "refresh"
 *<br>
 * TODO remember to save the suppression values
 */
public class AttributeValueContainer extends ValueContainer {
    private final Holder<Attribute> attributeHolder;
    private  LivingEntity attachedEntity;
    public AttributeValueContainer(LivingEntity attachedEntity,Holder<Attribute> attributeHolder, Component valueName) {
        super(attributeHolder.getKey().location(), valueName, 0);
        this.attributeHolder = attributeHolder;
        this.attachedEntity =attachedEntity;
        calculateCachedVal();
    }
    private final HashMap<Stat,ValueContainer> statMultipliers = new HashMap<>();
    double cachedBaseStat;
    double cachedAttributeValue;
    double suppressedValue;
    boolean suppressed;
    @Override
    public void calculateCachedVal() {
        if(cachedAttributeValue+cachedBaseStat != getBaseValue()) setBaseValue(cachedAttributeValue+cachedBaseStat);
        super.calculateCachedVal();
    }
    public Holder<Attribute> getAttributeHolder(){return attributeHolder;}
    public void setAttachedEntity(LivingEntity entity){this.attachedEntity = entity;}
    public void addStatScaling(Stat stat, double scaling){
        if(!statMultipliers.containsKey(stat)) statMultipliers.put(stat,new ValueContainer(
                AscensionRegistries.Stats.STATS_REGISTRY.getKey(stat),
                stat.getDisplayName(),
                0
        ));
        statMultipliers.get(stat).setBaseValue(Math.max(0,statMultipliers.get(stat).getBaseValue()+scaling));
    }
    public void removeStatScaling(Stat stat,double scaling){
        if(!statMultipliers.containsKey(stat)) return;

        addStatScaling(stat,-scaling);
    }
    public void addStatScalingModifier(Stat stat,ValueContainerModifier modifier){
        if(!statMultipliers.containsKey(stat)) statMultipliers.put(stat,new ValueContainer(
                AscensionRegistries.Stats.STATS_REGISTRY.getKey(stat),
                stat.getDisplayName(),
                0
        ));
        statMultipliers.get(stat).addModifier(modifier);

    }

    public void updateValue(IEntityData entityData){
        //used to retrieve the stat sheet for calculations
        StatSheet statSheet = entityData.getActiveFormData().getStatSheet();

        double baseVal = 0;
        for (Stat stat : statMultipliers.keySet()){
            StatInstance instance = statSheet.getStatInstance(stat);
            baseVal += instance.getValue()*statMultipliers.get(stat).getValue();
        }
        cachedBaseStat = baseVal;

        calculateCachedVal();
    }
    public void validateAttributeValue(){
        if(attachedEntity == null) return;
        var inst = attachedEntity.getAttribute(attributeHolder);
        double target = (inst != null) ? inst.getValue() : attributeHolder.value().getDefaultValue();
        if(target != cachedAttributeValue) {
            cachedAttributeValue = target;
            calculateCachedVal();
        }
    }
    @Override
    public double getValue() {
        validateAttributeValue();
        return isSuppressed()?getSuppressedValue():super.getValue();
    }
    public double getUnsuppressedValue(){
        return super.getValue();
    }
    @Override
    public double getBaseValue() {
        validateAttributeValue();
        return super.getBaseValue();
    }

    public double getSuppressedValue(){
        return suppressedValue;
    }
    public void setSuppressedValue(double value){
        this.suppressedValue = value;
        setSuppressed(getUnsuppressedValue()>getSuppressedValue() && getSuppressedValue() > 0);
    }
    public void setSuppressed(boolean suppressed){
        this.suppressed = suppressed;
    }
    public boolean isSuppressed(){
        if(super.getValue()<=getSuppressedValue() || getSuppressedValue() <= 0) {
            setSuppressed(false);
            setSuppressedValue(0);
        }else if(getSuppressedValue() > 0) setSuppressed(true);

        return suppressed;
    }


    public static void encode(RegistryFriendlyByteBuf buf, AttributeValueContainer container){
        ValueContainer.encode(buf,container);
        buf.writeInt(container.statMultipliers.size());
        for(ValueContainer statContainer : container.statMultipliers.values()){
            ValueContainer.encode(buf,statContainer);
        }
        buf.writeBoolean(container.suppressed);
        buf.writeDouble(container.suppressedValue);

    }
    public static AttributeValueContainer decode(RegistryFriendlyByteBuf buf){
        ResourceLocation identifier = ByteBufUtil.readResourceLocation(buf);
        Component displayName = ComponentSerialization.STREAM_CODEC.decode(buf);
        double base = buf.readDouble();
        int modifierNumber = buf.readInt();
        AttributeValueContainer container = new AttributeValueContainer(null, BuiltInRegistries.ATTRIBUTE.wrapAsHolder(
                BuiltInRegistries.ATTRIBUTE.get(identifier)
        ),displayName);
        for(int i=0;i<modifierNumber;i++){
            container.addModifier(ValueContainerModifier.decode(buf));
        }
        int statContainers = buf.readInt();
        for(int i =0;i<statContainers;i++){
            ValueContainer statContainer = ValueContainer.decode(buf);
            container.statMultipliers.put(AscensionRegistries.Stats.STATS_REGISTRY.get(statContainer.getIdentifier()),statContainer);
        }

        container.suppressed = buf.readBoolean();
        container.suppressedValue=buf.readDouble();

        return container;
    }
    public void log(){
        //System.out.print(getDisplayName().getString() +" : ");
        //System.out.print((isSuppressed() ? getSuppressedValue() : getValue()));
        //if(isSuppressed()) //System.out.print(" ("+getUnsuppressedValue()+")");
        //System.out.println(" base : "+getBaseValue());
        //System.out.println("suppressed value : "+getSuppressedValue());
    }
}
