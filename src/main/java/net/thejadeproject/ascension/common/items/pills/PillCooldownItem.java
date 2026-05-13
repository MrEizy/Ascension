package net.thejadeproject.ascension.common.items.pills;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PillCooldownItem extends Item {
    public int cooldownTimeValue = 50;
    public onItemUse consumer;

    public PillCooldownItem(Properties properties, Integer value) {
        super(properties);
        this.cooldownTimeValue = value;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide() && livingEntity instanceof Player player) {
            player.getCooldowns().addCooldown(this, cooldownTimeValue);
            if(consumer != null) consumer.accept(stack, level, livingEntity);
        }

        return resultStack;
    }

    public PillCooldownItem addOnUse(onItemUse onItemUse){
        this.consumer = onItemUse;
        return this;
    }

    public interface onItemUse{
        void accept(ItemStack stack, Level level, LivingEntity livingEntity);
    }
}