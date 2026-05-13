package net.zic.ascension.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MilkBucketEffectMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void onFinishUsing(ItemStack stack, Level level, LivingEntity entity,
                               CallbackInfoReturnable<ItemStack> cir) {
        if (!(entity instanceof Player player)) {
            return;
        }
        cir.cancel();
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (stack.getCount() == 1) {
                    cir.setReturnValue(emptyBucket);
                } else {
                    stack.shrink(1);
                    if (!player.getInventory().add(emptyBucket)) {
                        player.drop(emptyBucket, false);
                    }
                    cir.setReturnValue(stack);
                }
            } else {
                cir.setReturnValue(stack);
            }
        } else {
            if (player.getAbilities().instabuild) {
                cir.setReturnValue(stack);
            } else {
                cir.setReturnValue(stack.getCount() == 1 ? new ItemStack(Items.BUCKET) : stack);
            }
        }
    }
}