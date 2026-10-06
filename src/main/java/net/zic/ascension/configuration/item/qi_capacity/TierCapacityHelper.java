package net.zic.ascension.configuration.item.qi_capacity;

import net.minecraft.world.item.ItemStack;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.configuration.ConfigurationDataMaps;

public class TierCapacityHelper {
    public static long getCapacity(ItemStack itemStack){
        return getCapacity(itemStack.get(AscensionComponents.ITEM_TIER),getTierCapacityDefinition(itemStack));
    }
    public static long getCapacity(String tier,TierCapacityDefinition definition){
        return definition.capacity().getOrDefault(tier,0L);
    }

    public static TierCapacityDefinition getTierCapacityDefinition(ItemStack itemStack){
        return itemStack.getData(ConfigurationDataMaps.ITEM_TIER_QI_CAPACITY);
    }

}
