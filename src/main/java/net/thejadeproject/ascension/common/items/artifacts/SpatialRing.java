package net.thejadeproject.ascension.common.items.artifacts;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.data_components.spatial_ring.SpatialRingComponent;
import net.thejadeproject.ascension.common.items.data_components.spatial_ring.SpatialRingItemStackHandler;
import net.thejadeproject.ascension.common.items.data_components.spatial_ring.SpatialRingMenuProvider;

public class SpatialRing extends Item {
    public SpatialRing(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(usedHand != InteractionHand.MAIN_HAND) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        ItemStack stack = player.getMainHandItem();
        if(!stack.has(ModDataComponents.SPIRIT_RING_DATA)) stack.set(ModDataComponents.SPIRIT_RING_DATA,
                new SpatialRingComponent(27, 18, 18));
        if(!player.isShiftKeyDown()){
                player.openMenu(new SpatialRingMenuProvider(SpatialRingItemStackHandler.Type.INVENTORY));
            }else{
                player.openMenu(new SpatialRingMenuProvider(SpatialRingItemStackHandler.Type.MODIFIERS));
            }
        return InteractionResultHolder.success(stack);


    }


}
