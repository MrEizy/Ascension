package net.zic.ascension.mixins.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.zic.ascension.client.renderer.AfterShaderPackDraws;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Starts each frame's AfterShaderPackDraws afresh, and draws the first-person hand's once the level and the hand are done.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererAfterShaderPackMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void ascension$clearAfterShaderPack(DeltaTracker deltaTracker, CallbackInfo ci) {
        AfterShaderPackDraws.clear();
    }

    // Right after renderLevel returns (before the screenshot and the GUI): Iris wraps that call and draws the hand on its way
    // out, so drawing the coating any earlier left its depth in the hand's way, and the hand went missing under it.
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;tryTakeScreenshotIfNeeded()V"))
    private void ascension$drawHandAfterShaderPack(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        AfterShaderPackDraws.drawHand();
    }
}
