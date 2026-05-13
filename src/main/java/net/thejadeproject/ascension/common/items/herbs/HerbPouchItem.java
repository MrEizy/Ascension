package net.thejadeproject.ascension.common.items.herbs;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.data_components.herb_pouch.HerbPouchComponent;
import net.thejadeproject.ascension.common.items.data_components.herb_pouch.HerbPouchMenuProvider;


public class HerbPouchItem extends Item {

    public HerbPouchItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        ItemStack stack = player.getMainHandItem();

        if (!stack.has(ModDataComponents.HERB_POUCH_DATA.get())) {
            stack.set(ModDataComponents.HERB_POUCH_DATA.get(), new HerbPouchComponent(27));
        }

        if (!level.isClientSide) {
            player.openMenu(new HerbPouchMenuProvider());
        }

        return InteractionResultHolder.success(stack);
    }
}