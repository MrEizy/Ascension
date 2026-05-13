package net.thejadeproject.ascension.refactor_packages.util.value_modifiers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

import java.util.*;

/**
 * used to store values that have multipliers applied to them
 * is fully volatile, none of the multipliers are persistent after log out
 *
 * TODO add a dirty attribute to modifiers so i can sync only changes
 */
public class ValueContainer {
    private final ResourceLocation valueIdentifier;
    private final Component valueName;

    private double base;
    private final HashMap<ResourceLocation,ValueContainerModifier> addBase = new HashMap<>();
    private final HashMap<ResourceLocation,ValueContainerModifier> addFinal = new HashMap<>();

    private final HashMap<ResourceLocation,ValueContainerModifier> multiBase = new HashMap<>();
    private final HashMap<ResourceLocation, HashSet<ValueContainerModifier>> multiBaseByGroup = new HashMap<>();

    private final HashMap<ResourceLocation,ValueContainerModifier> multiFinal = new HashMap<>();
    private final HashMap<ResourceLocation, HashSet<ValueContainerModifier>> multiFinalByGroup = new HashMap<>();

    private double cachedVal;
    public ValueContainer(ResourceLocation valueIdentifier,Component valueName,double base){
        this.valueIdentifier = valueIdentifier;
        this.base = base;
        this.valueName = valueName;

        calculateCachedVal();
    }
    public Collection<ValueContainerModifier> getAllModifiers(){
        List<ValueContainerModifier> modifiers = new ArrayList<>();
        modifiers.addAll(addBase.values());
        modifiers.addAll(addFinal.values());
        modifiers.addAll(multiBase.values());
        modifiers.addAll(multiFinal.values());
        return modifiers;
    }
    public ResourceLocation getIdentifier(){return valueIdentifier;}
    public Component getDisplayName(){return valueName;}
    public void calculateCachedVal(){

        double finalBaseMultiplier = 1;
        for(HashSet<ValueContainerModifier> group : multiBaseByGroup.values()){
            double multiplier = 1;
            for(ValueContainerModifier modifier : group) multiplier += modifier.getVal();
            multiplier = Math.max(multiplier,0); //TODO might change to clamp while adding multipliers
            finalBaseMultiplier *= multiplier;
        }
        double finalVal = base*finalBaseMultiplier;


        for(ValueContainerModifier modifier : addBase.values()){
            finalVal += modifier.getVal();
        }


        double finalMultiplier = 1;
        for(HashSet<ValueContainerModifier> group : multiFinalByGroup.values()){
            double multiplier = 1;
            for(ValueContainerModifier modifier : group) multiplier += modifier.getVal();
            multiplier = Math.max(multiplier,0); //TODO might change to clamp while adding multipliers
            finalMultiplier *= multiplier;
        }
        finalVal *= finalMultiplier;

        for(ValueContainerModifier modifier : addFinal.values()){
            finalVal += modifier.getVal();
        }

        cachedVal = Math.max(0,finalVal);
    }
    public void setBaseValue(double base){
        this.base = Math.max(0,base);
        calculateCachedVal();
    }
    public void addModifier(ValueContainerModifier modifier){
        if(modifier == null) return;
        if(modifier.getOperation() == ModifierOperation.ADD_BASE)addBase.put(modifier.getIdentifier(),modifier);
        else if(modifier.getOperation() == ModifierOperation.ADD_FINAL) addFinal.put(modifier.getIdentifier(),modifier);
        else if (modifier.getOperation() == ModifierOperation.MULTIPLY_BASE) {
            if(multiBase.containsKey(modifier.getIdentifier())){
                ValueContainerModifier old = multiBase.remove(modifier.getIdentifier());
                multiBaseByGroup.get(old.getGroupIdentifier()).remove(old);
                if(multiBaseByGroup.get(old.getGroupIdentifier()).isEmpty()) multiBaseByGroup.remove(old.getGroupIdentifier());
            }
            multiBase.put(modifier.getIdentifier(),modifier);
            if(!multiBaseByGroup.containsKey(modifier.getGroupIdentifier())) multiBaseByGroup.put(modifier.getGroupIdentifier(),new HashSet<>());
            multiBaseByGroup.get(modifier.getGroupIdentifier()).add(modifier);
        }
        else if (modifier.getOperation() == ModifierOperation.MULTIPLY_FINAL) {
            if(multiFinal.containsKey(modifier.getIdentifier())){
                ValueContainerModifier old = multiFinal.remove(modifier.getIdentifier());
                multiFinalByGroup.get(old.getGroupIdentifier()).remove(old);
                if(multiFinalByGroup.get(old.getGroupIdentifier()).isEmpty()) multiFinalByGroup.remove(old.getGroupIdentifier());
            }
            multiFinal.put(modifier.getIdentifier(),modifier);
            if(!multiFinalByGroup.containsKey(modifier.getGroupIdentifier())) multiFinalByGroup.put(modifier.getGroupIdentifier(),new HashSet<>());
            multiFinalByGroup.get(modifier.getGroupIdentifier()).add(modifier);
        }
        calculateCachedVal();
    }
    public void removeModifier(ResourceLocation id){
        addFinal.remove(id);
        addBase.remove(id);
        if(multiFinal.containsKey(id)){
            ValueContainerModifier modifier = multiFinal.remove(id);
            ResourceLocation group = modifier.getGroupIdentifier();
            multiFinalByGroup.get(group).remove(modifier);
        }
        if(multiBase.containsKey(id)){
            ValueContainerModifier modifier = multiBase.remove(id);
            ResourceLocation group = modifier.getGroupIdentifier();
            multiBaseByGroup.get(group).remove(modifier);
        }
    }

    public double getValue(){return cachedVal;}
    public double getBaseValue(){return base;}


    public static void encode(RegistryFriendlyByteBuf buf,ValueContainer container){
        ByteBufUtil.encodeString(buf,container.getIdentifier().toString());
        ComponentSerialization.STREAM_CODEC.encode(buf,container.getDisplayName());
        buf.writeDouble(container.base);
        Collection<ValueContainerModifier> modifiers = container.getAllModifiers();
        buf.writeInt(modifiers.size());
        for(ValueContainerModifier modifier : modifiers){
            modifier.encode(buf);
        }

    }
    public static ValueContainer decode(RegistryFriendlyByteBuf buf){
        ResourceLocation identifier = ByteBufUtil.readResourceLocation(buf);
        Component displayName = ComponentSerialization.STREAM_CODEC.decode(buf);
        double base = buf.readDouble();
        int modifierNumber = buf.readInt();
        ValueContainer container = new ValueContainer(identifier,displayName,base);
        for(int i=0;i<modifierNumber;i++){
            container.addModifier(ValueContainerModifier.decode(buf));
        }
        return container;
    }
}
