package net.thejadeproject.ascension.common.items.tools.hidden_weapons;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.tools.data.needle.ModNeedleEffects;
import net.thejadeproject.ascension.entity.custom.NeedleProjectile;

import java.util.List;

public class NeedleItem extends Item {
    public NeedleItem(Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            NeedleProjectile needle = new NeedleProjectile(level, player, stack);

            needle.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.5f, 0.5f);
            level.addFreshEntity(needle);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String effectId = stack.get(ModDataComponents.NEEDLE_EFFECT.get());
        if (effectId != null) {
            ModNeedleEffects.get(effectId).ifPresentOrElse(
                    effect -> tooltip.add(
                            Component.translatable("tooltip.ascension.needle_tipped",
                                            Component.translatable("needle_effect.ascension." + effect.getId().getPath()))
                                    .withStyle(ChatFormatting.DARK_PURPLE)
                    ),
                    () -> tooltip.add(Component.literal("Unknown effect: " + effectId).withStyle(ChatFormatting.GRAY))
            );
        } else {
            tooltip.add(Component.translatable("tooltip.ascension.needle_plain").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
