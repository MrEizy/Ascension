package net.zic.ascension.refactor_packages.alchemy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPillEffect {
    Component getName();
    Component getDescription();
    boolean tryConsume(LivingEntity livingEntity, ItemStack itemStack, double purityScale, double realmMultiplier);
    default boolean shouldGoOnCooldown(){return true;}
    default InteractionResultHolder<ItemStack> onItemUse(Level level, Player player, InteractionHand hand){return null;}
}
