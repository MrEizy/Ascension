package net.zic.ascension.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.client.renderer.SwordQiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Third person: tells SwordQiRenderer whose held item is being submitted.
 */
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerSwordQiMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    private void ascension$beginSwordQi(ArmedEntityRenderState state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm,
                                        PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        // Only players carry an entity id on their render state, and only players can cast Sword Qi.
        if (state instanceof AvatarRenderState avatar) {
            SwordQiRenderer.begin(avatar.id, itemStack);
        }
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"))
    private void ascension$endSwordQi(ArmedEntityRenderState state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm,
                                      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        SwordQiRenderer.end();
    }
}
