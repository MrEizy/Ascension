package net.zic.ascension.mixins.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.zic.ascension.client.renderer.layer.QiCoatingLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fades the player's own model while a coating effect such as Lightning Step asks for it (QiCoatingLayer.bodyAlpha). The
 * player model already draws translucent, so lowering the tint's alpha is enough.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererQiCoatingMixin {
    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void ascension$fadeLightningStep(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (state instanceof AvatarRenderState avatar) {
            float alpha = QiCoatingLayer.bodyAlpha(avatar);
            if (alpha < 1.0F) {
                cir.setReturnValue(ARGB.multiplyAlpha(cir.getReturnValue(), alpha));
            }
        }
    }
}
