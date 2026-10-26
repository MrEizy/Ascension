package net.zic.ascension.mixins.client;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.zic.ascension.client.renderer.layer.QiCoatingLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the opaque arms of void-coated players; QiCoatingLayer draws translucent copies in their place.
 * setupAnim runs again when the deferred model is drawn, so this has to live here rather than in a render event.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelQiCoatingMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void ascension$hideVoidCoatedArms(AvatarRenderState state, CallbackInfo ci) {
        if (QiCoatingLayer.drawsGhostArms(state)) {
            PlayerModel model = (PlayerModel) (Object) this;
            model.rightArm.visible = false;
            model.leftArm.visible = false;
        }
    }
}
