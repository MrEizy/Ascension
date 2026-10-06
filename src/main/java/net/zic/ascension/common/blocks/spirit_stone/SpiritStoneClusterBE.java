package net.zic.ascension.common.blocks.spirit_stone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.qi.QiHandler;
import net.zic.ascension.common.blocks.ModBlockEntities;
import net.zic.ascension.common.util.ModTags;

public class SpiritStoneClusterBE extends BlockEntity {

    //only as big as the stored items
    private NonNullList<ItemStack> items = NonNullList.create();

    //TODO make this a configuration option?
    private final int CAPACITY = 8;


    public SpiritStoneClusterBE(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.SPIRIT_STONE_CLUSTER.get(), worldPosition, blockState);
        //ChestBlockEntity chestBlockEntity = new ChestBlockEntity(worldPosition, blockState);
        //ChestBlock
    }


    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.ValueOutputList itemList = output.childrenList("items");
        for(ItemStack itemStack : items){
            itemList.addChild().store("item",ItemStack.CODEC,itemStack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ValueInput.ValueInputList inputList = input.childrenListOrEmpty("items");
        for(ValueInput item : inputList){
            ItemStack stack = item.read("item",ItemStack.CODEC).orElse(ItemStack.EMPTY);
            addSpiritStoneItem(stack);
        }
    }

    public boolean isSpiritStone(ItemStack stack){
         return (stack.is(ModTags.Items.SPIRIT_STONE)
                 && stack.getCapability(CoreCapabilities.ITEM_QI_HANDLER) != null);

    }

    /**
     * Takes an item and attempts to add it, then returns whatever is left
     * @param stack
     * @return what is left of the item, if empty returns EMPTY itemstack otherwise returns with reduced count
     */
    public ItemStack addSpiritStoneItem(ItemStack stack) {
        if(!isSpiritStone(stack)) return stack;

        while(items.size() < CAPACITY && !stack.isEmpty()) {
            items.add(stack.split(1));
        }
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    public long getStoredQi(){
        long sum = 0;
        for(ItemStack stack : items) {
            QiHandler handler = stack.getCapability(CoreCapabilities.ITEM_QI_HANDLER);
            if(handler == null) continue;
            sum += handler.getQi();
        }
        return sum;
    }
    public long getCapacity(){
        long sum = 0;
        for(ItemStack stack : items) {
            QiHandler handler = stack.getCapability(CoreCapabilities.ITEM_QI_HANDLER);
            if(handler == null) continue;
            sum += handler.getCapacity();
        }
        return sum;
    }
    public ItemStack getNonEmptySpiritStone(){
        for(ItemStack stack : items){
            QiHandler handler = stack.getCapability(CoreCapabilities.ITEM_QI_HANDLER);
            if(handler == null || handler.getQi() == 0) continue;
            return stack;
        }
        return ItemStack.EMPTY;
    }
    public ItemStack getNonFullSpiritStone(){
        for(ItemStack stack : items){
            QiHandler handler = stack.getCapability(CoreCapabilities.ITEM_QI_HANDLER);
            if(handler == null || handler.isFull()) continue;
            return stack;
        }
        return ItemStack.EMPTY;
    }
    public int extractQi(int amount){
        int amountLeft = amount;
        while(amountLeft > 0 && !getNonEmptySpiritStone().isEmpty()){
            ItemStack stack = getNonEmptySpiritStone();
            QiHandler handler = stack.getCapability(CoreCapabilities.ITEM_QI_HANDLER);
            if(handler == null) break; //should NEVER happen, but escape just in case

            int extracted = handler.extractQi(amountLeft);
            amountLeft -= extracted;
        }
        return amount-amountLeft;
    }

    public int insertQi(int amount){
        int amountLeft = amount;
        while(amountLeft > 0 && !getNonFullSpiritStone().isEmpty()){
            ItemStack stack = getNonEmptySpiritStone();
            QiHandler handler = stack.getCapability(CoreCapabilities.ITEM_QI_HANDLER);
            if(handler == null) break; //should NEVER happen, but escape just in case

            int inserted = handler.insertQi(amountLeft);
            amountLeft -= inserted;
        }
        return amount - amountLeft;
    }

}
