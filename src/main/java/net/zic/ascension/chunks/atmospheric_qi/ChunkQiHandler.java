package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.resources.Identifier;
import net.minecraft.util.parsing.packrat.CachedParseState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.qi.QiHandler;
import net.zic.zenithlib.value_containers.typed.*;

public class ChunkQiHandler implements QiHandler {

    private long qi;
    private final ValueContainer<Long> CAPACITY;
    private final ValueContainer<Long> REGEN_RATE;


    public ChunkQiHandler(long qi){
        this(qi,0,0);

    }

    public ChunkQiHandler(long qi,long baseCapacity,long baseRegenRate) {
        qi = 0;
        CAPACITY = ValueContainerHelpers.longValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "capacity"), baseCapacity);
        REGEN_RATE = ValueContainerHelpers.longValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "regen_rate"), baseRegenRate);
    }
    //============= Container Handlers ==========

    void setBaseCapacity(long capacity){
        CAPACITY.setBaseValue(capacity);
    }
    void setBaseRegenRate(long regenRate){
        REGEN_RATE.setBaseValue(regenRate);
    }
    void addCapacityBaseValue(long capacity){
        CAPACITY.setBaseValue(CAPACITY.getBaseValue()+capacity);
    }
    void addRegenRateBaseValue(long regenRate){
        REGEN_RATE.setBaseValue(REGEN_RATE.getBaseValue()+regenRate);
    }

    public void addCapacityBonus(BonusModifier<Long> modifier){
        CAPACITY.addBonusModifier(modifier);
    }
    public void addCapacityMultiplier(MultiplierModifier modifier){
        CAPACITY.addMultiplierModifier(modifier);
    }
    public Modifier removeCapacityModifier(Identifier modifier){
        return CAPACITY.removeModifier(modifier);
    }


    public void addRegenRateBonus(BonusModifier<Long> modifier){
        REGEN_RATE.addBonusModifier(modifier);
    }
    public void addRegenRateMultiplier(MultiplierModifier modifier){
        REGEN_RATE.addMultiplierModifier(modifier);
    }
    public Modifier removeRegenRateMultiplier(Identifier modifier){
        return REGEN_RATE.removeModifier(modifier);
    }

    //============= Qi methods ============

    @Override
    public boolean tryConsume(int amount) {
        long newQi = Math.min( qi -amount,getCapacity());
        if(newQi < 0) return false;
        qi = newQi;
        return true;
    }

    @Override
    public int insertQi(int amount) {
        int change = getCapacity()-qi < amount ? Math.toIntExact(getCapacity() - qi) : amount;
        qi = qi + change;
        return change;
    }

    @Override
    public int extractQi(int amount) {
        int change = qi-amount < 0 ? Math.toIntExact(qi) : amount;
        qi = qi -change;
        return change;
    }
    public void regenQi(){
        qi = Math.min(getCapacity(),qi+getRegenRate());
    }

    @Override
    public long getQi() {
        return qi;
    }

    @Override
    public long getCapacity() {
        return CAPACITY.getValue();
    }
    public long getRegenRate(){
        return REGEN_RATE.getValue();
    }
    @Override
    public boolean isFull() {
        return CAPACITY.getValue() == qi;
    }

    public static class Provider implements IAttachmentSerializer<ChunkQiHandler> {


        @Override
        public ChunkQiHandler read(IAttachmentHolder holder, ValueInput input) {
            long qi = input.getLongOr("qi",0);
            return new ChunkQiHandler(qi);
        }

        @Override
        public boolean write(ChunkQiHandler attachment, ValueOutput output) {
            output.putLong("qiy",attachment.getQi());
            return true;
        }
    }
}
