package net.thejadeproject.ascension.refactor_packages.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;
import net.thejadeproject.ascension.refactor_packages.util.IDataInstance;

import java.util.Collection;
import java.util.HashMap;

public class AscensionModifier implements IDataInstance {

    private final Operator operator;
    private final ResourceLocation modifierId;
    private final ResourceLocation groupId;
    private final double value;
    public AscensionModifier(Operator operator, ResourceLocation modifierId, double value){
        this(operator,modifierId,ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"default"),value);
    }
    public AscensionModifier(Operator operator, ResourceLocation modifierId, ResourceLocation groupId, double value){
        this.operator = operator;
        this.modifierId = modifierId;
        this.groupId = groupId;
        this.value = value;
    }


    public enum Operator{
       TRUE_ADD_BASE,
       ADD_BASE,
       MULTIPLY_BASE,
       MULTIPLY_FINAL,
   }

    public Operator getOperator(){return operator;}
    public ResourceLocation getModifierId(){return modifierId;}
    public ResourceLocation getGroupId(){
        return groupId == null ? ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"default") : groupId;
    }
    public double getValue(){return value;}

    public static double calculateBaseValue(Collection<AscensionModifier> modifiers,double base){
        double flatBase = 0;
        for(AscensionModifier modifier : modifiers) {
            if (modifier.getOperator() == Operator.TRUE_ADD_BASE) flatBase += modifier.getValue();
        }
        return base+flatBase;
    }

    public static double calculateValue(Collection<AscensionModifier> modifiers,double baseVal){

        double flatBase = 0;
        HashMap<ResourceLocation,Double> groupedBaseMultipliers = new HashMap<>();
        HashMap<ResourceLocation,Double> groupedFinalMultipliers = new HashMap<>();
        double flatTrueBase = 0;

        for(AscensionModifier modifier : modifiers){
            if(modifier.getOperator() == Operator.ADD_BASE) flatBase += modifier.getValue();
            else if(modifier.getOperator() == Operator.TRUE_ADD_BASE) flatTrueBase += modifier.getValue();
            else if(modifier.getOperator() == Operator.MULTIPLY_BASE){
                groupedBaseMultipliers.put(modifier.getGroupId(),modifier.getValue());
            } else if (modifier.getOperator() == Operator.MULTIPLY_FINAL) {
                groupedFinalMultipliers.put(modifier.getGroupId(),modifier.getValue());
            }
        }
        double val = baseVal+flatTrueBase;
        double totalBaseMultiplier = 1;
        for(Double mul : groupedBaseMultipliers.values()){
            totalBaseMultiplier *= (1+mul);
        }
        double totalFinalMultiplier = 1;
        for(Double mul : groupedFinalMultipliers.values()){
            totalFinalMultiplier *= (1+mul);
        }
        return  (val*totalBaseMultiplier+flatBase)*totalFinalMultiplier;

    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id",modifierId.toString());
        tag.putString("group_id",groupId.toString());
        tag.putString("operator",operator.name());
        tag.putDouble("value",getValue());
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        ByteBufUtil.encodeString(buf,modifierId.toString());
        ByteBufUtil.encodeString(buf,groupId.toString());
        ByteBufUtil.encodeString(buf,operator.name());
        buf.writeDouble(getValue());
    }

    public static AscensionModifier read(CompoundTag tag){
        ResourceLocation id = ResourceLocation.bySeparator(tag.getString("id"),':');
        ResourceLocation groupId = ResourceLocation.bySeparator(tag.getString("groupId"),':');
        Operator operator = Operator.valueOf(tag.getString("operator"));
        double val = tag.getDouble("value");
        return new AscensionModifier(operator,id,groupId,val);
    }
    public static AscensionModifier decode(RegistryFriendlyByteBuf buf){
        ResourceLocation id = ByteBufUtil.readResourceLocation(buf);
        ResourceLocation groupId = ByteBufUtil.readResourceLocation(buf);
        Operator operator = Operator.valueOf(ByteBufUtil.readString(buf));
        double val = buf.readDouble();
        return new AscensionModifier(operator,id,groupId,val);
    }
}
