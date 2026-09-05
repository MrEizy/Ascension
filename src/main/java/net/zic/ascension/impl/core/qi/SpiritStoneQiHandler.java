package net.zic.ascension.impl.core.qi;

import net.minecraft.world.item.ItemStack;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.configuration.item.qi_capacity.TierCapacityDefinition;
import net.zic.ascension.configuration.item.qi_capacity.TierCapacityHelper;

public class SpiritStoneQiHandler extends SimpleItemQiHandler{
    public SpiritStoneQiHandler(ItemStack itemStack) {
        super(itemStack, TierCapacityHelper.getCapacity(itemStack));
    }
}
