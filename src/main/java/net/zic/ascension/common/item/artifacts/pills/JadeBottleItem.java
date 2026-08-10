package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zic.ascension.common.gui.menus.jade_bottle.JadeBottleMenu;

public class JadeBottleItem extends Item {
    public JadeBottleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inv, p) -> new JadeBottleMenu(containerId, inv, stack),
                    stack.getHoverName()
            ), buf -> ItemStack.STREAM_CODEC.encode(buf, stack));
        }
        return InteractionResult.SUCCESS;
    }
}
