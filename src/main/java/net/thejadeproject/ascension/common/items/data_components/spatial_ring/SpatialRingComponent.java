package net.thejadeproject.ascension.common.items.data_components.spatial_ring;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;
import net.thejadeproject.ascension.refactor_packages.util.CodecUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpatialRingComponent {
    private final int minSlots;
    private int maxSlots;
    private final ArrayList<ItemStack> items = new ArrayList<>();
    private final ArrayList<ItemStack> modifiers = new ArrayList<>();
    private final ArrayList<ItemStack> upgrades = new ArrayList<>();

    public SpatialRingComponent(int initialSlots, int modifiers, int upgrades){
        this.minSlots = initialSlots;
        this.maxSlots = initialSlots;
        for(int i=0;i<initialSlots;i++){
            items.add(ItemStack.EMPTY);
        }
        for(int i=0;i<modifiers;i++){
            this.modifiers.add(ItemStack.EMPTY);
        }
        for(int i=0;i<upgrades;i++){
            this.upgrades.add(ItemStack.EMPTY);
        }
    }
    public SpatialRingComponent(int minSlots,List<ItemStack> items, List<ItemStack> modifiers, List<ItemStack> upgrades){
        //System.out.println("trying to create component");
        //System.out.println(minSlots);
        //System.out.println(items.size());
        //System.out.println(modifiers.size());
        //System.out.println(upgrades.size());
        this.minSlots = minSlots;
        this.maxSlots = items.size();
        this.items.addAll(items);
        this.modifiers.addAll(modifiers);
        this.upgrades.addAll(upgrades);
    }

    public void updateMaxItemSlots(){
        //TODO calculate new max slots then adjust
        int newMaxSlots = getInitialSlots();
        for(ItemStack stack : getModifiers()){
            if(stack.isEmpty()) continue;
            if(!stack.has(ModDataComponents.EXTRA_SLOTS) || stack.get(ModDataComponents.EXTRA_SLOTS) == null) continue;
            newMaxSlots += stack.get(ModDataComponents.EXTRA_SLOTS);
        }
        if(newMaxSlots < maxSlots){
            //remove
            for(int i = maxSlots;i>newMaxSlots;i--){
                items.removeLast();
            }
        }else{
            for(int i = maxSlots;i<newMaxSlots;i++){
                items.add(ItemStack.EMPTY);
            }
        }
        this.maxSlots = newMaxSlots;

    }

    public void addUpgradeItem(ItemStack stack,int slot){
        upgrades.set(slot,stack);
    }
    public void removeUpgradeItem(int slot){
        upgrades.set(slot,ItemStack.EMPTY);
    }
    public void addModifierItem(ItemStack stack,int slot){
        modifiers.set(slot,stack);
        updateMaxItemSlots();
    }
    public void removeModifierItem(int slot){
        modifiers.set(slot,ItemStack.EMPTY);
        updateMaxItemSlots();
    }

    public void addItem(ItemStack stack,int slot){
        items.set(slot,stack);
    }
    public void removeItem(int slot){
        items.set(slot,ItemStack.EMPTY);
    }
    public List<ItemStack> getItems(){return items;}
    public List<ItemStack> getUpgrades(){return upgrades;}
    public List<ItemStack> getModifiers(){return modifiers;}
    public int getMaxSlots(){return maxSlots;}
    public int getInitialSlots(){return minSlots;}
    public ItemStackHandler createItemHandler(ItemStack stack){
        NonNullList<ItemStack> itemStacks = NonNullList.create();
        itemStacks.addAll(items);
        return new SpatialRingItemStackHandler(stack, SpatialRingItemStackHandler.Type.INVENTORY,itemStacks);
    }
    public ItemStackHandler createUpgradeHandler(ItemStack stack){
        NonNullList<ItemStack> itemStacks = NonNullList.create();
        itemStacks.addAll(upgrades);
        return new SpatialRingItemStackHandler(stack, SpatialRingItemStackHandler.Type.UPGRADES,itemStacks);
    }
    public ItemStackHandler createModifierHandler(ItemStack stack) {
        NonNullList<ItemStack> itemStacks = NonNullList.create();
        itemStacks.addAll(modifiers);
        return new SpatialRingItemStackHandler(stack, SpatialRingItemStackHandler.Type.MODIFIERS,itemStacks);
    }



    public static final Codec<SpatialRingComponent> CODEC = RecordCodecBuilder.create(instance->
        instance.group(
                Codec.INT.fieldOf("minSlots").forGetter(SpatialRingComponent::getInitialSlots),
                ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(SpatialRingComponent::getItems),
                ItemStack.OPTIONAL_CODEC.listOf().fieldOf("modifiers").forGetter(SpatialRingComponent::getModifiers),
                ItemStack.OPTIONAL_CODEC.listOf().fieldOf("upgrades").forGetter(SpatialRingComponent::getUpgrades)
        ).apply(instance, SpatialRingComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SpatialRingComponent> STREAM_CODEC =
            StreamCodec.of(SpatialRingComponent::encode, SpatialRingComponent::decode);

    public static void encode(RegistryFriendlyByteBuf buf, SpatialRingComponent component){
        buf.writeInt(component.getInitialSlots());
        ByteBufUtil.ITEM_STACK_LIST.encode(buf,component.getItems());
        ByteBufUtil.ITEM_STACK_LIST.encode(buf,component.getModifiers());
        ByteBufUtil.ITEM_STACK_LIST.encode(buf,component.getUpgrades());
    }
    public static SpatialRingComponent decode(RegistryFriendlyByteBuf buf){
        int initialSlots = buf.readInt();
        List<ItemStack> items = ByteBufUtil.ITEM_STACK_LIST.decode(buf);
        List<ItemStack> modifiers = ByteBufUtil.ITEM_STACK_LIST.decode(buf);
        List<ItemStack> upgrades = ByteBufUtil.ITEM_STACK_LIST.decode(buf);

        return new SpatialRingComponent(initialSlots,items,modifiers,upgrades);
    }

    public SpatialRingComponent copy(){
        return new SpatialRingComponent(getInitialSlots(),getItems(),getModifiers(),getUpgrades());
    }
    @Override
    public boolean equals(Object o) {
        return this == o;
        /*
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpatialRingComponent that = (SpatialRingComponent) o;
        return maxSlots == that.maxSlots && Objects.equals(items, that.items) && Objects.equals(modifiers, that.modifiers) && Objects.equals(upgrades, that.upgrades);
         */

    }

    @Override
    public int hashCode() {
        return Objects.hash(maxSlots, items, modifiers, upgrades);
    }
}
