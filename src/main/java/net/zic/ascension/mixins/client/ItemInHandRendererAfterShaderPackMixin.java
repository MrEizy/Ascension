package net.zic.ascension.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.zic.ascension.client.renderer.AfterShaderPackDraws;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks the first-person hands' submits, so AfterShaderPackDraws draws them with the hand rather than the level.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererAfterShaderPackMixin {
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void ascension$beginHand(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player,
                                     int lightCoords, CallbackInfo ci) {
        AfterShaderPackDraws.beginHand();
    }

    @Inject(method = "renderHandsWithItems", at = @At("RETURN"))
    private void ascension$endHand(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player,
                                   int lightCoords, CallbackInfo ci) {
        AfterShaderPackDraws.endHand();
    }
}
