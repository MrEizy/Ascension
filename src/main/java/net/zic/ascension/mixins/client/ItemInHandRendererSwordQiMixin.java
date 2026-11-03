package net.zic.ascension.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.client.renderer.SwordQiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First person: tells SwordQiRenderer whose held item is being submitted.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererSwordQiMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void ascension$beginSwordQi(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack,
                                        SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        SwordQiRenderer.begin(mob.getId(), itemStack);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void ascension$endSwordQi(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack,
                                      SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        SwordQiRenderer.end();
    }
}
